package client.interpreter;

import java.util.ArrayList;
import java.util.List;

import geometry.Point3D;
import geometry.Point3DH;
import geometry.Vector;
import geometry.Vertex3D;
import polygon.Polygon;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class ObjReader {
	private static final char COMMENT_CHAR = '#';
	private static final int NOT_SPECIFIED = -1;
//	private static LightingCalculationPackage lightingCalculationPackage;

	private class ObjVertex {
		private int vertex_index;
		private int normal_index;

		private ObjVertex(int index, int normal_index) {
			this.vertex_index = index;
			this.normal_index = normal_index;
		}

	}
	private class ObjFace extends ArrayList<ObjVertex> {
		private static final long serialVersionUID = -4130668677651098160L;
	}
	private LineBasedReader reader;

	private List<Vertex3D> objVertices;
	private List<Vertex3D> transformedVertices;
	private List<Point3DH> vertices;
	private static ArrayList<Vertex3D> polygon_vertices;
	private List<Vector> objNormals;
	private List<ObjFace> objFaces;
	private static ArrayList<Vector> normal_list;

	private Color defaultColor;

	public ObjReader(String filename, Color defaultColor) {
		objVertices = new ArrayList<Vertex3D>();
		transformedVertices = new ArrayList<Vertex3D>();
		vertices = new ArrayList<>();
		objNormals = new ArrayList<Vector>();
		objFaces = new ArrayList<ObjFace>();
		reader = new LineBasedReader(filename);
		normal_list = new ArrayList<>();
	}

	public static ArrayList<Vector> getNormalList() {
		return normal_list;
	}

	public void render(Color defaultColor, Drawable drawable) {
		ArrayList<Color> light_color = SimpInterpreter.getLightColor();
		ArrayList<Double> A = SimpInterpreter.getAttA();
		ArrayList<Double> B = SimpInterpreter.getAttB();
		Color ambientLight = SimpInterpreter.getAmbientLight();
		Color kd = SimpInterpreter.getKd();
		ArrayList<Point3D> light_location = SimpInterpreter.getLightLocation();
		double ks = SimpInterpreter.getKs();
		double p = SimpInterpreter.getP();
		for (ObjFace face : objFaces) {
			//			System.out.println("+++++++++++++++++++++++++++++++");
//			System.out.println("face size is" + face.size());
			if (face.size() >= 3) {
				for (int j = 1; j < face.size() - 1; j++) {
					polygon_vertices = new ArrayList<>();
					int first_index = face.get(0).vertex_index;
					int second_index = face.get(j).vertex_index;
					int third_index = face.get(j + 1).vertex_index;
					int first_normal_index = face.get(0).normal_index;
					int second_normal_index = face.get(j).normal_index;
					int third_normal_index = face.get(j + 1).normal_index;
					Point3DH point1 = new Point3DH(vertices.get(first_index).getX(), vertices.get(first_index).getY(),
							vertices.get(first_index).getZ());
					Vertex3D first_point = new Vertex3D(point1, defaultColor);
					Point3DH vertex1_screen = getVertexInScreenSpace(point1);
					Vertex3D vertex1 = new Vertex3D(vertex1_screen, defaultColor);

					Point3DH point2 = new Point3DH(vertices.get(second_index).getX(), vertices.get(second_index).getY(),
							vertices.get(second_index).getZ());
					Vertex3D second_point = new Vertex3D(point2, defaultColor);
					Point3DH vertex2_screen = getVertexInScreenSpace(point2);
					Vertex3D vertex2 = new Vertex3D(vertex2_screen, defaultColor);

					Point3DH point3 = new Point3DH(vertices.get(third_index).getX(), vertices.get(third_index).getY(),
							vertices.get(third_index).getZ());
					Vertex3D third_point = new Vertex3D(point3, defaultColor);
					Point3DH vertex3_point = getVertexInScreenSpace(point3);
					Vertex3D vertex3 = new Vertex3D(vertex3_point, defaultColor);

					Vector normal_first = objNormals.get(first_normal_index);
					Vector normal_second = objNormals.get(second_normal_index);
					Vector normal_third = objNormals.get(third_normal_index);
					first_point = first_point.setNormal(normal_first);
					second_point = second_point.setNormal(normal_second);
					third_point = third_point.setNormal(normal_third);

					polygon_vertices.add(first_point);
					polygon_vertices.add(second_point);
					polygon_vertices.add(third_point);
					//===============================================
					Vector normal = (normal_first.add(normal_second)).add(normal_third);
					normal = new Vector(normal.getX() / 3, normal.getY() / 3, normal.getZ() / 3).normalize();
//						Vector normal = normal_first.add(normal_second).add(normal_third).scale(1/3);
					normal_list.add(normal);

					Polygon dummy_face = Polygon.make(vertex1, vertex2, vertex3);
					SimpInterpreter.filledRenderer.drawPolygon(dummy_face, drawable);

				}

			}
		}
	}

	public static ArrayList<Vertex3D> getList(){
		return polygon_vertices;
	}


	public void read() {
		while(reader.hasNext() ) {
			String line = reader.next().trim();
			interpretObjLine(line);
		}
	}
	private void interpretObjLine(String line) {
		if(!line.isEmpty() && line.charAt(0) != COMMENT_CHAR) {
			String[] tokens = line.split("[ \t,()]+");
			if(tokens.length != 0) {
				interpretObjCommand(tokens);
			}
		}
	}

	private void interpretObjCommand(String[] tokens) {
		switch(tokens[0]) {
			case "v" :
			case "V" :
				interpretObjVertex(tokens);
				break;
			case "vn":
			case "VN":
				interpretObjNormal(tokens);
				break;
			case "f":
			case "F":
				interpretObjFace(tokens);
				break;
			default:
				break;
		}
	}
	private void interpretObjFace(String[] tokens) {
		ObjFace face = new ObjFace();
		for(int i = 1; i<tokens.length; i++) {
			String token = tokens[i];
			String[] subtokens = token.split("/");
			int vertexIndex  = objIndex(subtokens, 0, objVertices.size());
			int textureIndex = objIndex(subtokens, 1, 0);
			int normalIndex  = objIndex(subtokens, 2, objNormals.size());

			ObjVertex face_vertex = new ObjVertex(vertexIndex, normalIndex);
			face.add(face_vertex);
//			face.add(face_vertex);
		}
		objFaces.add(face);
	}


	private int objIndex(String[] subtokens, int tokenIndex, int baseForNegativeIndices) {
		int index = 0;
		if(!subtokens[tokenIndex].equals("")) {
			index = Integer.parseInt(subtokens[tokenIndex]) - 1;
			if(index < 0) {
				index = baseForNegativeIndices - Math.abs(index);
			}
		}
		return index;
	}

	private void interpretObjNormal(String[] tokens) {
		int numArgs = tokens.length - 1;
		if(numArgs != 3) {
			throw new BadObjFileException("vertex normal with wrong number of arguments : " + numArgs + ": " + tokens);
		}
		Point3DH normal = SimpInterpreter.interpretPoint(tokens, 1);
		Vector normal_vector = new Vector(normal.getX(), normal.getY(), normal.getZ());
		objNormals.add(normal_vector);
	}

	private Point3DH getVertexInScreenSpace(Point3DH point){
		double[] current = new double[4];
		double[] result = new double[4];
		current[0] = point.getX();
		current[1] = point.getY();
		current[2] = point.getZ();
		current[3] = point.getW();
		double[][] perspective_matrix = SimpInterpreter.getPerspectiveMatrix();
		double[][] toScreen = SimpInterpreter.getViewToScreen();
		for(int i = 0; i < 4; i++) {
			double lineTotal = 0;
			for(int j = 0; j < 4; j++) {
				lineTotal += perspective_matrix[i][j] * current[j];
			}
			result[i] = lineTotal;
		}
		current = result;
		for(int i = 0; i < 4; i++) {
			double lineTotal = 0;
			for(int j = 0; j < 4; j++) {
				lineTotal += toScreen[i][j] * current[j];
			}
			result[i] = lineTotal;
		}
		current = result;
		return new Point3DH(current).euclidean();
	}

	private void interpretObjVertex(String[] tokens) {
		int numArgs = tokens.length - 1;
		Point3DH point = objVertexPoint(tokens, numArgs);
		Color color = objVertexColor(tokens, numArgs);

		Vertex3D obj_vertex = new Vertex3D(point, color);
		objVertices.add(obj_vertex);

		Point3DH trans_vertex = new Point3DH(obj_vertex.getPoint3D().getX(), obj_vertex.getPoint3D().getY(),
				obj_vertex.getPoint3D().getZ());

		double[][] CTM_inverse = SimpInterpreter.getInversedCTM();
		double current_point[] = new double[4];
		double temp[] = new double[4];
		current_point[0] = trans_vertex.getX();
		current_point[1] = trans_vertex.getY();
		current_point[2] = trans_vertex.getZ();
		current_point[3] = trans_vertex.getW();
		for(int i = 0; i < 4; i++) {
			double lineTotal = 0;
			for(int j = 0; j < 4; j++) {
				lineTotal += CTM_inverse[i][j] * current_point[j];
			}
			temp[i] = lineTotal;
		}
		current_point = temp;
		//if(current_point[3] == 0){ current_point[3] = 1;}
		trans_vertex = new Point3DH(current_point[0], current_point[1], current_point[2], current_point[3]).euclidean();
		//Vertex3D temp_vertex = new Vertex3D(trans_vertex.getX(), trans_vertex.getY(), trans_vertex.getZ(), color);
//		System.out.println(temp_vertex.getX() + " , " + temp_vertex.getY() + " , " + temp_vertex.getZ());
		vertices.add(trans_vertex);
	}

	private Color objVertexColor(String[] tokens, int numArgs) {
		if(numArgs == 6) {
			return SimpInterpreter.interpretColor(tokens, 4);
		}
		if(numArgs == 7) {
			return SimpInterpreter.interpretColor(tokens, 5);
		}
		return defaultColor;
	}

	private Point3DH objVertexPoint(String[] tokens, int numArgs) {
		if(numArgs == 3 || numArgs == 6) {
			return SimpInterpreter.interpretPoint(tokens, 1);
		}
		else if(numArgs == 4 || numArgs == 7) {
			return SimpInterpreter.interpretPointWithW(tokens, 1);
		}
		throw new BadObjFileException("vertex with wrong number of arguments : " + numArgs + ": " + tokens);
	}
}