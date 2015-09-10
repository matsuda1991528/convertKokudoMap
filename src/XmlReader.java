import java.io.FileInputStream;
import java.io.File;
import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlReader {
	private final static String TYPE = "type";
	private final static String RDCL = "RdCL";
	private final static String LOC = "loc";
	private final static String CURVE = "gml:Curve";
	private final static String SEGMENTS = "gml:segments";
	private final static String LINESTRINGSEGMENT = "gml:LineStringSegment";
	private final static String TSUUZYOUBU = "通常部";
	private final static String FILE_NAME = "map_coord.dat"; //座標出力先のファイル名


	public void parseXml(String xmlFile)throws Exception{
		DocumentBuilderFactory dbf
						= DocumentBuilderFactory.newInstance();  /* DOMパーサ用のファクトリの生成 */
		
		DocumentBuilder db
						= dbf.newDocumentBuilder();  /* DOM Document　用のインスタンス用ファクトリの生成 */
		
		Document doc = db.parse(new FileInputStream(xmlFile)); /* 引数として与えたxmlfileの解析とDocumentインスタンスの取得 */
		
		Element root = doc.getDocumentElement();  /* ルート要素の取得 */
		
		//walk(root);  /* 取得したルートを辿っていく */
		
		GnuplotUser.plotGnuplot(FILE_NAME);
		System.out.println("finish!!!");
		//CvtUtm.readLatLonFile();
	}
	
	/* 引数で受け取ったノードの全ての子ノードを走査していく（このメソッドでは，孫ノードでは無くて，兄弟ノードのみを走査する．） */
	private static void walk(Node node){
		/* 指定されたノードの子を取得し，ノードの子が存在する限りループ */
		for(Node ch = node.getFirstChild();ch != null;ch = ch.getNextSibling()){
			if(ch.getNodeName().equals(RDCL))
				getSpecificNodeValue(RDCL, ch.getNodeName(), ch); //指定したノード要素（RDCL）を探索する
		}
	}
	
	/* あるノード（Node node）の子ノードからターゲットノード（targetNodeName）を探索していくメソッド */
	private static void getSpecificNodeValue(String targetNodeName, String nodeName, Node node){
		//Node node の子ノードを順に辿っていく
		for(Node ch = node.getFirstChild();ch != null;ch = ch.getNextSibling()){
			/* 
			 * 各セグメント（交差点間を結ぶ道路）の道路タイプをチェックする
			 * 道路タイプが通常部   ：ノード要素がLOCの子ノードを走査していく．
			 * otherwise      ：次のセグメントの探索へ移行
			 */
			if(ch.getNodeName().equals(LOC)){
				boolean isRoad = checkRoadType(ch, TSUUZYOUBU);
				if(isRoad == true)
					getSpecificNodeValue(CURVE, ch.getNodeName(), ch);
				else
					break;
			}
			
			/* ノードの要素がCURVEならば，SEGMENTを要素に持つ，CURVEの子ノードを探す */
			else if(ch.getNodeName().equals(CURVE))
				getSpecificNodeValue(SEGMENTS, ch.getNodeName(), ch);
			
			/* ノードの要素がSEGMENTならば，LINESTRINGSEGMENTを要素に持つ，SEGMENTの子ノードを探す */
			else if(ch.getNodeName().equals(SEGMENTS))
				getSpecificNodeValue(LINESTRINGSEGMENT, ch.getNodeName(), ch);
			
			/* ノードの要素がLINESTRINGSEGMENTならば，そのノードのテキスト（経緯度）を取得する */
			else if(ch.getNodeName().equals(LINESTRINGSEGMENT)){
				/* 経緯度をワンセットとした文字列としてbufAryに格納していく */
				/* 経緯度のワンセットはこんな感じ→　34.325743 135.98543 　これがString型で格納される*/
				String buf = new String(ch.getTextContent());
				String[] bufAry = buf.split("\n", -1);
				

				/* 
				 * bufAry配列内の文字列から経緯度及びそれらのxy座標値をcoordに格納する
				 * 格納したcoordデータをfwriteCoordDataでファイル書き込みする．
				 *  */
				for(int i=0;i<bufAry.length;i++){
					if(bufAry[i].length()!=0){
						Coord coord = new Coord();
						coord = getCoord(bufAry[i]);
						System.out.println(coord.x+" "+coord.y);
						fwriteCoordData(coord);
					}
				}
				System.out.println();
				fwriteLineBreak();
				//System.out.println(ch.getTextContent());	
			}
		}
	}
	
	public static Coord getCoord(String coordLine){
		String[] buf = coordLine.split("[\\n \\s]", -1);
		Coord coord = new Coord();
		coord.lon = Double.valueOf(buf[1]);
		coord.lat = Double.valueOf(buf[2]);
		
		CvtUtm cvtUtm = new CvtUtm();
		coord.x = cvtUtm.cvtLattoXcoord(coord.lon, coord.lat);
		coord.y = cvtUtm.cvtLontoYcoord(coord.lon, coord.lat);
		return coord;
	}

	/* 各道路セグメントの道路タイプをチェックするメソッド */
	//【引数】node:辿るノード　targetType　抽出したい道路タイプ
	//【戻り値】抽出したい道路タイプである→true otherwise→false
	public static boolean checkRoadType(Node node, String targetType){
		String Type = null;
		//nodeの兄弟ノードを辿っていく
		for(;node != null;node = node.getNextSibling()){
			//type要素を持つノードならば，テキストを取得する．
			if(node.getNodeName().equals(TYPE)){
				Type = node.getTextContent();
				break;
			}
		}
		if(Type.equals(targetType))
			return true;
		else
			return false;
	}
	
	/* 引数として受け取った座標データ（x,y 経度，緯度）をファイル出力する */
	public static void fwriteCoordData(Coord coord){
		File file = new File(FILE_NAME);
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(
					new OutputStreamWriter(
							new FileOutputStream(file, true)));
			pw.println(coord.lat+" "+coord.lon+" "+coord.x+" "+coord.y);
		}catch(IOException e){
			System.out.println(e);
		}finally{
			if(pw != null){
				pw.close();
			}
		}
	}

	public static void fwriteLineBreak(){
		File file = new File(FILE_NAME);
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(
					new OutputStreamWriter(
							new FileOutputStream(file, true)));
			pw.println();
		}catch(IOException e){
			System.out.println(e);
		}finally{
			if(pw != null){
				pw.close();
			}
		}
	}
}
