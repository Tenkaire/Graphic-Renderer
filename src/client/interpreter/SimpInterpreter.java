package client.interpreter;

import java.util.ArrayList;
import java.util.Stack;

import geometry.*;
import line.LineRenderer;
import client.Clipper;
import client.RendererTrio;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.drawable.PixelClippingDrawable;
import windowing.drawable.Z_buffering;
import windowing.graphics.Color;
import windowing.graphics.Dimensions;

public class SimpInterpreter {
	private static final int NUM_TOKENS_FOR_POINT = 3;
	private static final int NUM_TOKENS_FOR_COMMAND = 1;
	private static final int NUM_TOKENS_FOR_COLORED_VERTEX = 6;
	private static final int NUM_TOKENS_FOR_UNCOLORED_VERTEX = 3;
	private static final char COMMENT_CHAR = '#';

	private static double VIEW_LOW_X;
	private static double VIEW_HIGH_X;
	private static double VIEW_LOW_Y;
	private static double VIEW_HIGH_Y;
	private static double VIEW_HITHER;
	private static double VIEW_YON;

	private static int camHeight;
	private static int camWidth;

	private double near;
	private double far;
	private Color depthColor;

	private LineBasedReader reader;
	private Stack<LineBasedReader> readerStack;
	private Stack<double[][]> xStack;

	private Color defaultColor;
	private static Color ambientLight;

	private static Color kd; // surface color
	private Color lightingCalcColor;

	private Drawable drawable;
	private Drawable depthCueingDrawable;
	private Drawable pixelClippingDrawable;

	private static double[][] CTM;
	private static double[][] inversedCTM;
	private static double[][] perspectiveMatrix;
	private static double[][] viewToScreen;
	private static double[][] pageHM;
	private RenderStyle renderStyle;

	public static LineRenderer lineRenderer;
	public static PolygonRenderer filledRenderer;
	public static PolygonRenderer wireframeRenderer;
	private Clipper clipper;
	private static boolean filled = true;
	public static Z_buffering zBuffering;

	public enum ShadingStyle{FLAT, GOURAUD, PHONG}
	private static ShadingStyle shadingStyle ;

	public static ArrayList<Color> getLightColor() {
		return lightColor;
	}

	public static ArrayList<Double> getAttA() {
		return attA;
	}

	public static ArrayList<Double> getAttB() {
		return attB;
	}

	public static double getKs() {
		return ks;
	}

	public static double getP() {
		return p;
	}

	public static ArrayList<Point3D> getLightLocation() {
		return lightLocation;
	}

	public static Color getAmbientLight() {
		return ambientLight;
	}

	public static Color getKd() {
		return kd;
	}

	private static ArrayList<Color> lightColor;
	private static ArrayList<Double> attA;
	private static ArrayList<Double> attB;

	private static double ks;
	private static double p;
	private static ArrayList<Point3D> lightLocation;

	private static ArrayList<Vertex3D> verticeList;
	private static boolean hasNormal;

	public static double getHither() {
		return VIEW_HITHER;
	}

	public static double getYon() {
		return VIEW_YON;
	}

	public static double[][] get_non_square_matrix() {
		return pageHM;
	}

	public static boolean isHasNormal() {
		return hasNormal;
	}

	public enum RenderStyle {
		FILLED,
		WIREFRAME
	}
	public SimpInterpreter(String filename,
			Drawable drawable,
			RendererTrio renderers) {
		this.drawable = drawable;
		this.depthCueingDrawable = drawable;
		this.lineRenderer = renderers.getLineRenderer();
		this.filledRenderer = renderers.getFilledRenderer();
		this.wireframeRenderer = renderers.getWireframeRenderer();
		this.defaultColor = Color.fromARGB(depthCueingDrawable.getPixel(0, 0));

		CTM = new double[4][4];
		inversedCTM = new double[4][4];
		viewToScreen = new double[4][4];
		perspectiveMatrix = new double[4][4];
		pageHM = new double[4][4];
		CTM = MatrixCalculation.identity();
		inversedCTM = MatrixCalculation.identity();
		viewToScreen = MatrixCalculation.identity();
		perspectiveMatrix = MatrixCalculation.identity();
		pageHM = MatrixCalculation.identity();
		perspectiveMatrix[3][3] = 0;
		perspectiveMatrix[3][2] = -1;

		camHeight = drawable.getHeight();
		camWidth = drawable.getWidth();

		near = -Double.MAX_VALUE;
		far = -Double.MAX_VALUE;

		filled = true;
		reader = new LineBasedReader(filename);
		readerStack = new Stack<>();
		renderStyle = RenderStyle.FILLED;

		xStack = new Stack<>();
		renderStyle = RenderStyle.FILLED;
		zBuffering = new Z_buffering();
		for(int i = 0; i < 650; i++){
			for(int j = 0; j < 650; j++){
				zBuffering.changeValue(i, j, -200);
			}
		}
        defaultColor = Color.WHITE;
        ambientLight = Color.BLACK;
        kd = Color.WHITE;
        depthColor = Color.BLACK;
        VIEW_HIGH_Y = 0;
        VIEW_HIGH_X = 0;
        VIEW_LOW_Y = 0;
        VIEW_LOW_X = 0;
        VIEW_HITHER = 0;
        VIEW_YON = 0;

		shadingStyle = ShadingStyle.PHONG;
		ks = 0.3;
		p = 8;
		lightColor = new ArrayList<>();
		attA = new ArrayList<>();
		attB = new ArrayList<>();
		lightLocation = new ArrayList<>();
		hasNormal = false;

	}


	private void makeViewToScreenTransform(Dimensions dimensions) {
		double width;
		double height;
		double deltaY = VIEW_HIGH_Y - VIEW_LOW_Y;
		double deltaX = VIEW_HIGH_X - VIEW_LOW_X;
		double deno = deltaX >= deltaY ? deltaX : deltaY;
		double ratio = deltaX / deltaY;
		width = dimensions.getWidth() / deno;
		height = dimensions.getHeight() / deno;

		if(ratio >= 1){
			camHeight = ((int)Math.round(dimensions.getHeight() / ratio));
		} else{
			camWidth = ((int)Math.round(dimensions.getWidth() / ratio));
		}

		createRectangleFrame();

		viewToScreen[0][0] = width;
		viewToScreen[1][1] = height;
		Transformation trans = Transformation.Identity();
		trans.alterValue(0, 3, 1);
		trans.alterValue(1, 3, 1);
		viewToScreen = MathCalculation.multiply(viewToScreen, trans);
	}

	private static void createRectangleFrame(){
		double bottom;
		double right;
		if(camHeight >= camWidth){
			right = (650 - camWidth) / 2;
			pageHM[0][3] = (int)right;
		}else{
			bottom = (650 - camHeight) / 2;
			pageHM[1][3] = (int)bottom;
		}
	}

	public void interpret() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretLine(line);
			while(!reader.hasNext()) {
				if(readerStack.isEmpty()) {
					return;
				}
				else {
					reader = readerStack.pop();
				}
			}
		}
	}
	public void interpretLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretCommand(tokens);
			}
		}
	}
	private void interpretCommand(String[] tokens) {
		switch(tokens[0]) {
		case "{" :      push();   break;
		case "}" :      pop();    break;
		case "wire" :   wire();   break;
		case "filled" : filled(); break;

		case "file" :		interpretFile(tokens);		break;
		case "scale" :		interpretScale(tokens);		break;
		case "translate" :	interpretTranslate(tokens);	break;
		case "rotate" :		interpretRotate(tokens);	break;
		case "line" :		interpretLine(tokens);		break;
		case "polygon" :	interpretPolygon(tokens);	break;
		case "camera" :		interpretCamera(tokens);	break;
		case "surface" :	interpretSurface(tokens);	break;
		case "ambient" :	interpretAmbient(tokens);	break;
		case "depth" :		interpretDepth(tokens);		break;
		case "obj" :		interpretObj(tokens);		break;
		case "light" :		interpretLight(tokens);		break;
		case "flat" : 		interpretFlat();			break;
		case "gouraud" :	interpretGouraud();			break;
		case "phong" :		interpretPhong();			break;

		default :
			System.err.println("bad input line: " + tokens);
			break;
		}
	}

	private void push() {
		xStack.push(CTM);
	}
	private void pop() {
		CTM = xStack.pop();
	}
	private void wire() {
		renderStyle = RenderStyle.WIREFRAME;
		filled = false;
	}
	private void filled() {
		renderStyle = RenderStyle.FILLED;
		filled = true;
	}

	private void interpretFile(String[] tokens) {
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"';
		String filename = quotedFilename.substring(1, length-1);
		file(filename + ".simp");
	}
	private void file(String filename) {
		readerStack.push(reader);
		reader = new LineBasedReader(filename);
	}

	private void interpretScale(String[] tokens) {
		double sx = cleanNumber(tokens[1]);
		double sy = cleanNumber(tokens[2]);
		double sz = cleanNumber(tokens[3]);
		Transformation sk = Transformation.Identity();
		sk.alterValue(0,0, sx);
		sk.alterValue(1,1, sy);
		sk.alterValue(2,2, sz);
		CTM = MathCalculation.multiply(CTM, sk);
	}

	private void interpretTranslate(String[] tokens) {
		double tx = cleanNumber(tokens[1]);
		double ty = cleanNumber(tokens[2]);
		double tz = cleanNumber(tokens[3]);
		Transformation t = Transformation.Identity();
		t.alterValue(0, 3, tx);
		t.alterValue(1, 3, ty);
		t.alterValue(2, 3, tz);
		CTM = MathCalculation.multiply(CTM, t);
	}
	private void interpretRotate(String[] tokens) {
		String axisString = tokens[1];
		double angleInDegrees = cleanNumber(tokens[2]);

		Transformation tran = Transformation.Identity();
		if(axisString.equals("X")){
			tran.alterValue(1, 1, Math.cos(Math.toRadians(angleInDegrees)));
			tran.alterValue(1, 2, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			tran.alterValue(2, 1, Math.sin(Math.toRadians(angleInDegrees)));
			tran.alterValue(2, 2, Math.cos(Math.toRadians(angleInDegrees)));
		} else if(axisString.equals("Y")){
			tran.alterValue(0, 0, Math.cos(Math.toRadians(angleInDegrees)));
			tran.alterValue(0, 2, Math.sin(Math.toRadians(angleInDegrees)));
			tran.alterValue(2, 0, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			tran.alterValue(2, 2, Math.cos(Math.toRadians(angleInDegrees)));
		} else if(axisString.equals("Z")) {
			tran.alterValue(0, 0, Math.cos(Math.toRadians(angleInDegrees)));
			tran.alterValue(0, 1, (-1) * Math.sin(Math.toRadians(angleInDegrees)));
			tran.alterValue(1, 0, Math.sin(Math.toRadians(angleInDegrees)));
			tran.alterValue(1, 1, Math.cos(Math.toRadians(angleInDegrees)));
		}
		CTM = MathCalculation.multiply(CTM, tran);
	}


	private void interpretObj(String[] tokens) {
		hasNormal = true;
		String quotedFilename = tokens[1];
		int length = quotedFilename.length();
		assert quotedFilename.charAt(0) == '"' && quotedFilename.charAt(length-1) == '"';
		String filename = quotedFilename.substring(1, length-1);
		filename = filename + ".obj";
		objFile(filename);
	}

	private void interpretLight(String[] tokens){
		double light_R = cleanNumber(tokens[1])*255;
		double light_G = cleanNumber(tokens[2])*255;
		double light_B = cleanNumber(tokens[3])*255;
		attA.add(cleanNumber(tokens[4]));
		attB.add(cleanNumber(tokens[5]));
		lightColor.add(Color.fromARGB(Color.makeARGB((int)Math.round(light_R),(int)Math.round(light_G), (int)Math.round(light_B))));

		double[] currentPoint = {0, 0, 0, 1};
		double[] ans = new double[4];
		double[][] tempCTM = CTM;
        for(int i = 0; i < 4; i++) {
            double lineTotal = 0;
            for(int j = 0; j < 4; j++) {
                lineTotal += tempCTM[i][j] * currentPoint[j];
            }
            ans[i] = lineTotal;
        }
        currentPoint = ans;
        for(int i = 0; i < 4; i++) {
            double lineTotal = 0;
            for(int j = 0; j < 4; j++) {
                lineTotal += inversedCTM[i][j] * currentPoint[j];
            }
            ans[i] = lineTotal;
        }
		currentPoint = ans;
		lightLocation.add(new Point3D(currentPoint));
	}

	private void interpretPhong(){ shadingStyle = ShadingStyle.PHONG; }

	private void interpretGouraud(){ shadingStyle = ShadingStyle.GOURAUD; }

	private void interpretFlat(){ shadingStyle = ShadingStyle.FLAT; }

	private void objFile(String filename) {
		ObjReader objReader = new ObjReader(filename, defaultColor);
		objReader.read();
		objReader.render(defaultColor, drawable);
	}

	private static double cleanNumber(String string) {
		return Double.parseDouble(string);
	}

	private enum VertexColors {
		COLORED(NUM_TOKENS_FOR_COLORED_VERTEX),
		UNCOLORED(NUM_TOKENS_FOR_UNCOLORED_VERTEX);

		private int numTokensPerVertex;

		private VertexColors(int numTokensPerVertex) {
			this.numTokensPerVertex = numTokensPerVertex;
		}
		public int numTokensPerVertex() {
			return numTokensPerVertex;
		}
	}
	private void interpretLine(String[] tokens) {
		Vertex3D[] vertices = interpretVertices(tokens, 2, 1);
		lineRenderer.drawLine(vertices[0], vertices[1], drawable);
	}

	private Vertex3D applyColor(Vertex3D new_point) {
		Color new_color = defaultColor;
		if (new_point.getZ() >= near) {
			new_color = ambientLight.multiply(kd);
		} else if (new_point.getZ() <= far) {
			new_color = depthColor;
		} else if (near >= new_point.getZ() && new_point.getZ() >= far) {
			lightingCalcColor = ambientLight.multiply(kd);
			double r_slope = (depthColor.getR() - lightingCalcColor.getR()) / Math.abs((far - near));
			double g_slope = (depthColor.getG() - lightingCalcColor.getG()) / Math.abs((far - near));
			double b_slope = (depthColor.getB() - lightingCalcColor.getB()) / Math.abs((far - near));
			double deltaZ = Math.abs(new_point.getZ()) - Math.abs(near);
			double new_R = (lightingCalcColor.getR() + r_slope * deltaZ) * 255;
			double new_G = (lightingCalcColor.getG() + g_slope * deltaZ) * 255;
			double new_B = (lightingCalcColor.getB() + b_slope * deltaZ) * 255;
			new_color = Color.fromARGB(Color.makeARGB((int) Math.round(new_R), (int) Math.round(new_G),
					(int) Math.round(new_B)));
		}
		return new Vertex3D(new_point.getX(),new_point.getY(),new_point.getZ(), new_color);
	}

	private void interpretCamera(String[] tokens){
		inversedCTM = MatrixCalculation.inverseMatrix(CTM);

		VIEW_LOW_X = cleanNumber(tokens[1]);
		VIEW_LOW_Y = cleanNumber(tokens[2]);
		VIEW_HIGH_X = cleanNumber(tokens[3]);
		VIEW_HIGH_Y = cleanNumber(tokens[4]);
		VIEW_HITHER = cleanNumber(tokens[5]);
		VIEW_YON = cleanNumber(tokens[6]);

		clipper = new Clipper(VIEW_LOW_X, VIEW_HIGH_X, VIEW_LOW_Y, VIEW_HIGH_Y, VIEW_HITHER, VIEW_YON);
		makeViewToScreenTransform(drawable.getDimensions());
		pixelClippingDrawable = new PixelClippingDrawable(drawable, 0, 650, 0, 650, VIEW_HITHER, VIEW_YON);
	}

	private void interpretAmbient(String[] tokens){
		double color_R = cleanNumber(tokens[1])*255;
		double color_G = cleanNumber(tokens[2])*255;
		double color_B = cleanNumber(tokens[3])*255;
		ambientLight = Color.fromARGB(Color.makeARGB((int)Math.round(color_R),(int)Math.round(color_G), (int)Math.round(color_B)));
	}

	private void interpretSurface(String[] tokens){
		double color_R = cleanNumber(tokens[1])*255;
		double color_G = cleanNumber(tokens[2])*255;
		double color_B = cleanNumber(tokens[3])*255;
		ks = cleanNumber(tokens[4]);
		p = cleanNumber(tokens[5]);
		kd = Color.fromARGB(Color.makeARGB((int)Math.round(color_R),(int)Math.round(color_G), (int)Math.round(color_B)));
	}

	private void interpretDepth(String[] tokens){
		near = cleanNumber(tokens[1]);
		far = cleanNumber(tokens[2]);
		double depth_R = cleanNumber(tokens[3])*255;
		double depth_G = cleanNumber(tokens[4])*255;
		double depth_B = cleanNumber(tokens[5])*255;
		depthColor = Color.fromARGB(Color.makeARGB((int)Math.round(depth_R),(int)Math.round(depth_G), (int)Math.round(depth_B)));
	}

	private void interpretPolygon(String[] tokens) {
		Vertex3D[] vertices = interpretVertices(tokens, 3, 1);
		Polygon polygon = Polygon.make(vertices[0],vertices[1], vertices[2]);
		Clipper.Clipped<Polygon> clipped = clipper.clipPolygon(polygon);
		if (clipped.objectIsValid()){
			polygon = clipped.getClippedPolygon();
			Vertex3D[] list = new Vertex3D[polygon.length()];
			for(int i = 0 ; i < polygon.length(); i++){
				Vertex3D temp = polygon.get(i);
				list[i] = applyColor(temp);
			}
			polygon = Polygon.make(list);
            int current = 1;
            Vertex3D p1 = polygon.get(0);
            Vertex3D p2 = polygon.get(current);
            Vertex3D p3 = polygon.get(current + 1);

			polygon = TransformationPerspectiveMatrix.toPerspective(polygon);
			polygon = TransformationViewToScreen.viewToScreenMultiply(polygon);
			polygon = TransformationRectangleFrame.toRectangleFrame(polygon);
			int size_of_polygon = polygon.length() - 2;
			while(current <= size_of_polygon){
				if(filled) {
					verticeList = new ArrayList<>();
					verticeList.add(p1);
					verticeList.add(p2);
					verticeList.add(p3);
                    Vertex3D p1_screen = polygon.get(0); // screen space
                    Vertex3D p2_screen = polygon.get(current);
                    Vertex3D p3_screen = polygon.get(current + 1);
                    Polygon triangle = Polygon.makeEnsuringClockwise(p3_screen, p1_screen, p2_screen);
					filledRenderer.drawPolygon(triangle, pixelClippingDrawable);
				} else {
					Polygon triangle = Polygon.make(polygon.get(current + 1), polygon.get(0), polygon.get(current));
					wireframeRenderer.drawPolygon(triangle, pixelClippingDrawable);
				}
				current++;
			}
		}
	}

	public Vertex3D[] interpretVertices(String[] tokens, int numVertices, int startingIndex) {
		VertexColors vertexColors = verticesAreColored(tokens, numVertices);
		Vertex3D vertices[] = new Vertex3D[numVertices];

		for(int index = 0; index < numVertices; index++) {
			vertices[index] = interpretVertex(tokens, startingIndex + index * vertexColors.numTokensPerVertex(), vertexColors);
		}
		return vertices;
	}
	public VertexColors verticesAreColored(String[] tokens, int numVertices) {
		return hasColoredVertices(tokens, numVertices) ? VertexColors.COLORED :
														 VertexColors.UNCOLORED;
	}
	public boolean hasColoredVertices(String[] tokens, int numVertices) {
		return tokens.length == numTokensForCommandWithNVertices(numVertices);
	}
	public int numTokensForCommandWithNVertices(int numVertices) {
		return NUM_TOKENS_FOR_COMMAND + numVertices*(NUM_TOKENS_FOR_COLORED_VERTEX);
	}

	private Vertex3D interpretVertex(String[] tokens, int startingIndex, VertexColors colored) {
		Point3DH point = interpretPoint(tokens, startingIndex);
		Color color = defaultColor;
		if(colored == VertexColors.COLORED) {
			color = interpretColor(tokens, startingIndex + NUM_TOKENS_FOR_POINT);
		}
		Color new_color = color;

		double current_point[] = new double[4];
		double temp[] = new double[4];
		current_point[0] = point.getX();
		current_point[1] = point.getY();
		current_point[2] = point.getZ();
		current_point[3] = point.getW();
		for(int i = 0; i < 4; i++) {
			double lineTotal = 0;
			for(int j = 0; j < 4; j++) {
				lineTotal += inversedCTM[i][j] * current_point[j];
			}
			temp[i] = lineTotal;
		}
		current_point = temp; // in cam space

		Point3DH new_point = new Point3DH(current_point[0], current_point[1], current_point[2], current_point[3]).euclidean();

		return new Vertex3D(new_point.getX(), new_point.getY(), new_point.getZ(), new_color);
	}

	public static Point3DH interpretPoint(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);

		double currentPoint[] = new double[4];
		double temp[] = new double[4];
		currentPoint[0] = x;
		currentPoint[1] = y;
		currentPoint[2] = z;
		currentPoint[3] = 1.0;
		for(int i = 0; i < 4; i++) {
			double lineTotal = 0;
			for(int j = 0; j < 4; j++) {
				lineTotal += CTM[i][j] * currentPoint[j];
			}
			temp[i] = lineTotal;
		}
		currentPoint = temp;
		return new Point3DH(currentPoint[0], currentPoint[1], currentPoint[2], currentPoint[3]).euclidean();
	}
	public static Color interpretColor(String[] tokens, int startingIndex) {
		double r = cleanNumber(tokens[startingIndex]);
		double g = cleanNumber(tokens[startingIndex + 1]);
		double b = cleanNumber(tokens[startingIndex + 2]);
		return Color.fromARGB(Color.makeARGB((int)Math.round(r), (int)Math.round(g), (int)Math.round(b)));
	}


	public static Point3DH interpretPointWithW(String[] tokens, int startingIndex) {
		double x = cleanNumber(tokens[startingIndex]);
		double y = cleanNumber(tokens[startingIndex + 1]);
		double z = cleanNumber(tokens[startingIndex + 2]);
		double w = cleanNumber(tokens[startingIndex + 3]);
		Point3DH point = new Point3DH(x, y, z, w);
		return point;
	}

	public static int getHeight(){
		return camHeight;
	}
	public static int getWidth(){
		return camWidth;
	}
	public static ArrayList<Vertex3D> getVerticeList() {
		return verticeList;
	}
	public static ShadingStyle getShadingStyle(){return shadingStyle;}
	public static boolean getFilled() {return filled;}
	public static double[][] getInversedCTM() {return inversedCTM;}
	public static double[][] getViewToScreen() {
		return viewToScreen;
	}
	public static double[][] getPerspectiveMatrix() {
		return perspectiveMatrix;
	}
}