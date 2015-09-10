import java.io.File;
//import java.io.FileReader;
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.InputMismatchException;

public class CvtUtm {
	private final static int SEMI_MAJOR_AXIS = 6378137; //楕円の長半径
	private final static double OBLATENESS = 1 / 298.257222101; //扁平率
	private final static int CENTRAL_MEDIAN = 315; //中央子午線（ゾーン番号によって異なる）
	private final static double K_0 = 0.9996; //オフセット値
	private final static int E_0 = 500000;
	private final static int N_0 = 0; //北半球ならば0->N_0，南半球ならば10000000->N_0
	
	public static void readLatLonFile(){
		String url = "map_only_latlon.txt";
		try{
			File file = new File(url);
			Scanner scan = new Scanner(file);
			scan.useDelimiter("\\s"); //何の文字で区切るかの設定：”\\s”→半角スペース
			
			/*
			 * 空白区切りで各数値をString型で読み込む
			 * （double型）で読み込もうとしたらエラーが出た為．．．
			 */
			int counter = 0;
			double lon = 0;
			double lat = 0;
			while(scan.hasNext()){
				String buf1 = null;
				String buf2 = null;
				while(scan.hasNext()){
					buf1 = scan.next();
					if(buf1.length() != 0){
						counter = 0;
						break;
					}	
					else{
						counter++;
					}
					
					if(counter == 2)
						System.out.print("\n");
				}
				//System.out.println("a");
				while(scan.hasNext()){
					buf2 = scan.next();
					if(buf2.length() != 0)
						break;
				}
				//System.out.println("b");
				//System.out.println(buf1+buf2);
				if(buf1 != null && buf2 != null){
					lon = Double.parseDouble(buf1);
					lat = Double.parseDouble(buf2);
				}
					//cvtLonLatToXy(lon, lat);
					//System.out.println("c");
					//System.out.println(lon+" "+lat);
				
			}
		}catch(InputMismatchException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		
	}
	
	
	public void cvtLonLatToXy(double lon, double lat){
		//変換対象の経度が東経の場合は180足す．
		lat += 180.0;
		
		double alpha[];
		alpha = new double[4];
		
		
		/* 初期値の計算 */
		double n = OBLATENESS / (2 - OBLATENESS);
		double A = SEMI_MAJOR_AXIS / (1+n) * (1+Math.pow(n,2)/4 + Math.pow(n, 4) / 64.0);
		
		getAlpha(alpha, n);		

		double t = getT(lon, n);
		double xi = getXi(t, lat, CENTRAL_MEDIAN);
		double eta = getEta(t, lat, CENTRAL_MEDIAN);
		
		double x = getXcoord(alpha, A, eta, xi);
		double y = getYcoord(alpha, A, eta, xi);
		
		System.out.println(lon+" "+lat+" : "+x+" "+y);
		
	}
	
	public double cvtLontoYcoord(double lon, double lat){
		//変換対象の経度が東経の場合は180足す．
		lat += 180.0;
		double alpha[];
		alpha = new double[4];
		/* 初期値の計算 */
		double n = OBLATENESS / (2 - OBLATENESS);
		double A = SEMI_MAJOR_AXIS / (1+n) * (1+Math.pow(n,2)/4 + Math.pow(n, 4) / 64.0);
		
		getAlpha(alpha, n);		

		double t = getT(lon, n);
		double xi = getXi(t, lat, CENTRAL_MEDIAN);
		double eta = getEta(t, lat, CENTRAL_MEDIAN);
		double y = getYcoord(alpha, A, eta, xi);
		
		return y;
	}

	public double cvtLattoXcoord(double lon, double lat){
		//変換対象の経度が東経の場合は180足す．
		lat += 180.0;
		double alpha[];
		alpha = new double[4];
		/* 初期値の計算 */
		double n = OBLATENESS / (2 - OBLATENESS);
		double A = SEMI_MAJOR_AXIS / (1+n) * (1+Math.pow(n,2)/4 + Math.pow(n, 4) / 64.0);
		
		getAlpha(alpha, n);		

		double t = getT(lon, n);
		double xi = getXi(t, lat, CENTRAL_MEDIAN);
		double eta = getEta(t, lat, CENTRAL_MEDIAN);
		double x = getXcoord(alpha, A, eta, xi);
		
		return x;
	}
	
	private static void getAlpha(double[] alpha, double n){
		alpha[1] = 1.0/2.0*n - 2.0/3.0*Math.pow(n, 2) + 5.0/16.0*Math.pow(n, 3);
		alpha[2] = 13.0/48.0*Math.pow(n, 2) - 3.0/5.0*Math.pow(n, 3);
		alpha[3] = 61.0/240.0*Math.pow(n, 3);
	}
	
	/* 変数tを求めるメソッド */
	private static double getT(double phi, double n){
		double phi_rad = Math.toRadians(phi);
		CalcUtm calcUtm = new CalcUtm();
		return Math.sinh(calcUtm.atanh(Math.sin(phi_rad)) - 2*Math.sqrt(n)/(1+n) * calcUtm.atanh(2*Math.sqrt(n)/(1+n)*Math.sin(phi_rad)));
	}
	/* 変数xiを求めるメソッド */
	private static double getXi(double t, double lambda, double lambda_0){
		return Math.atan(t/Math.cos(Math.toRadians(lambda-lambda_0)));
	}
	/* 変数etaを求めるメソッド */
	private static double getEta(double t, double lambda, double lambda_0){
		CalcUtm calcUtm = new CalcUtm();
		return calcUtm.atanh((Math.sin(Math.toRadians(lambda - lambda_0))/Math.sqrt(1+t*t)));
	}
	
	/* x座標を求めるメソッド */
	private static double getXcoord(double[] alpha, double A, double eta, double xi){
		double end_eq = 0.0f;
		for(int i=1;i<4;i++)
			end_eq += alpha[i] * Math.cos(2.0*i*xi) * Math.sinh(2.0*i*eta);
		return E_0 + K_0 * A * (eta + end_eq);
	}
	
	/* y座標を求める */
	private static double getYcoord(double[] alpha, double A, double eta, double xi){
		double end_eq = 0.0f;
		for(int i=1;i<4;i++)
			end_eq += alpha[i] * Math.sin(2.0*i*xi) * Math.cosh(2.0*i*eta);
		return N_0 + K_0 * A * (xi + end_eq);
	}

}
