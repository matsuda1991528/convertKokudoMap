import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CvtUtm {
	private final static int SEMI_MAJOR_AXIS = 6378137; //楕円の長半径
	private final static double OBLATENESS = 1.0 / 298.257222101; //扁平率
	private final static int CENTRAL_MEDIAN = 315; //中央子午線（ゾーン番号によって異なる）
	private final static double K_0 = 0.9996; //オフセット値
	private final static int E_0 = 500000;
	private final static int N_0 = 0; //北半球ならば0->N_0，南半球ならば10000000->N_0
	
	public static void readLatLonFile(){
		String url = "map_only_latlon.txt";
		try{
			File file = new File(url);
			
			if(checkBeforeReadfile(file)){
				BufferedReader br = new BufferedReader(new FileReader(url));
				
				String str;
				while((str = br.readLine()) != null){
					System.out.println(str);
				}
				
				br.close();
				
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		
	}
	
	private static boolean checkBeforeReadfile(File file){
		if(file.exists()){
			if(file.isFile() && file.canRead()){
				return true;
			}
		}
		return false;
	}
	
}
