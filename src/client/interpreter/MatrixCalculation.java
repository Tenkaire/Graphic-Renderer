package client.interpreter;

public class MatrixCalculation {
	
	private static double[][] matrix;
	private static int n;
	private static double det;
	
	public MatrixCalculation(int n) {
		this.n = n;
		det = 0;
		matrix = new double[n][n];
		for(int i = 0 ; i < n; i++){
			for(int j = 0 ; j < n; j++){
					matrix[i][j] = 0;
			}
		}
	}
	
	
	public static double determinant(double[][] matrix) {
//		if (matrix.length != matrix[0].length)
//			throw new IllegalStateException("invalid dimensions");
		double determinant = 0;

		if (matrix.length == 2)
			return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
		if (matrix.length == 3) {
				double num1 = matrix[0][0] * matrix[1][1] * matrix[2][2];
				double num2 = matrix[0][1] * matrix[1][2] * matrix[2][0];
				double num3 = matrix[0][2] * matrix[1][0] * matrix[2][1];
				double num4 = matrix[2][0] * matrix[1][1] * matrix[0][2];
				double num5 = matrix[2][1] * matrix[1][2] * matrix[0][0];
				double num6 = matrix[2][2] * matrix[1][0] * matrix[0][1];
				return num1 + num2 + num3 - num4 - num5 - num6;
			}
		if (matrix.length == 4) {
			for(int row = 0; row < 4; row++){
				int col = 0;
				double[][] temp = new double[3][3];
				double[] arrayMat = new double[9];
				int counter = 0;
				for(int i = 0; i < 4; i++){
					for(int j = 0; j < 4; j++){
						if(i != row && j != col){
							arrayMat[counter] = matrix[i][j];
							counter++;
						}
					}
				}
				counter = 0;
				for(int i = 0; i < 3; i++){
					for(int j = 0; j < 3; j++){
						temp[i][j] = arrayMat[counter];
						counter++;
					}
				}
				determinant += matrix[row][col] * determinant(temp);
			}
			
		}
		return determinant;
	}
	
	public void Input(double num, int row, int column) {
          matrix[row][column] = num;
	}
	
	public double getDet() {
		return determinant(matrix);
	}
	
	public void InputManyNum (double...nums) {
		int count = 0;
		int i = 0;
		int j = 0;
		for(double num: nums){
			if(i < n) {
				if(j < n ) {
					Input(num,i,j);
					j++;
				}else if(j == n) {
					i++;
					j=0;
					Input(num,i,j);
					j++;					
				}
			}
			count++;
		}
	}

	public static double[][] inverseMatrix(double[][] matrix) {
		double[][] cofactor = new double[4][4];

		for(int row = 0; row < 4; row++){
			for(int col = 0; col < 4; col++){
				double[][] temp = new double[3][3];
				double[] arrayMat = new double[9];
				int counter = 0;
				for(int i = 0; i < 4; i++){
					for(int j = 0; j < 4; j++){
						if(i != row && j != col){
							arrayMat[counter] = matrix[i][j];
							counter++;
						}
					}
				}
				counter = 0;
				for(int i = 0; i < 3; i++){
					for(int j = 0; j < 3; j++){
						temp[i][j] = arrayMat[counter];
						counter++;
					}
				}
				cofactor[row][col] = MatrixCalculation.determinant(temp);
			}
		}

		int n = -1;
		int exp = 2;
		for(int i = 0; i < 4; i++){
			for(int j = 0; j < 4; j++){
				cofactor[i][j] *= Math.pow(n, exp);
				exp++;
			}
			exp += 1;
		}

		double[][] cofactor_inverse = new double[4][4];
		for(int i = 0; i < 4; i++){
			for(int j = 0; j < 4; j++) {
				cofactor_inverse[i][j] = cofactor[j][i];
			}
		}

		double determinant = 0;
		for(int row = 0; row < 4; row++){
			int col = 0;
			double[][] temp = new double[3][3];
			double[] arrayMat = new double[9];
			int counter = 0;
			for(int i = 0; i < 4; i++){
				for(int j = 0; j < 4; j++){
					if(i != row && j != col){
						arrayMat[counter] = matrix[i][j];
						counter++;
					}
				}
			}
			counter = 0;
			for(int i = 0; i < 3; i++){
				for(int j = 0; j < 3; j++){
					temp[i][j] = arrayMat[counter];
					counter++;
				}
			}
			determinant += matrix[row][col] * MatrixCalculation.determinant(temp);
		}

		for(int row = 0; row < 4; row++){
			for(int col = 0; col < 4; col++) {
				cofactor_inverse[row][col] /= determinant;
			}
		}
		return cofactor_inverse;
	}

	public static double[][] identity() {
		double[][] temp = new double[4][4];
		for(int i = 0 ; i < 4; i++){
			for(int j = 0 ; j < 4; j++){
				if(i == j)
					temp[i][j] = 1;
				else
					temp[i][j] = 0;
			}
		}
		return temp;
	}

}
