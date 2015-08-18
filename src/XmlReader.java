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
	private final static String RDCL = "RdCL";
	private final static String LOC = "loc";
	private final static String CURVE = "gml:Curve";
	private final static String SEGMENTS = "gml:segments";
	private final static String LINESTRINGSEGMENT = "gml:LineStringSegment";
	private final static String POSLIST = "gml:posList";
	private final static String TSUUZYOUBU = "通常部";
	private static int flag = 0;

/*
	public static void main(String[] args) throws Exception{
		if(args.length < 1){
			System.err.println("Usage:�����Pxml�t�@�C��");
			System.exit(-1);
		}
*/
		/* xml�t�@�C�����̊i�[ */
/*
	xmlFile = args[0];
		System.out.println("first stage");
		parseXml(xmlFile);
		System.out.println("finish");
	}
*/	
	public void parseXml(String xmlFile)throws Exception{
		DocumentBuilderFactory dbf
						= DocumentBuilderFactory.newInstance();
		DocumentBuilder db
						= dbf.newDocumentBuilder();
		Document doc = db.parse(new FileInputStream(xmlFile));
		
		Element root = doc.getDocumentElement();
		
		walk(root);
	}
	
	private static void walk(Node node){
		for(Node ch = node.getFirstChild();  //指定されたノードの子を取得して
				ch != null;                  //子がいる限り
				ch = ch.getNextSibling()){   //兄弟を辿る
			//System.out.println(ch.getNodeName());
			//System.out.println(ch.getTextContent());
			getSpecificNodeValue(RDCL, ch.getNodeName(), ch);
		}
	}
	
	private static void getSpecificNodeValue(String targetNodeName, String nodeName, Node node){
		if(nodeName != null){
			//nodeName == RdEdg�̏ꍇ
			if(targetNodeName.equals(nodeName)){
				for(Node ch = node.getFirstChild();
						ch != null;
						ch = ch.getNextSibling()){
					if(targetNodeName == LINESTRINGSEGMENT){
						//System.out.println(ch.getNodeName());
						System.out.println(ch.getTextContent());
						fwriteCoordData(ch.getTextContent());
						//getSpecificNodeText(POSLIST, ch.getNodeName(), ch);
					}
					if(targetNodeName == SEGMENTS){
						getSpecificNodeValue(LINESTRINGSEGMENT, ch.getNodeName(), ch);
					}
					else if(targetNodeName == CURVE){
						getSpecificNodeValue(SEGMENTS, ch.getNodeName(), ch);
					}
					else if(targetNodeName == LOC){
						System.out.println("second stage");
						getSpecificNodeValue(CURVE, ch.getNodeName(), ch);
					}
					else{
						if("type".equals(ch.getNodeName())){
							//System.out.println(ch.getNodeName() + "==" + ch.getTextContent());
							if(TSUUZYOUBU.equals(ch.getTextContent())){
								flag = 1;
							}else{
								flag = 2;
							}
						}
						if(flag == 1){
							getSpecificNodeValue(LOC, ch.getNodeName(), ch);
						}
					}
				}
			}
		}
	}
	
	public static void fwriteCoordData(String targetCoord){
		File file = new File("map_only_latlon.txt");
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(
					new OutputStreamWriter(
							new FileOutputStream(file, true)));
			pw.println(targetCoord);
		}catch(IOException e){
			System.out.println(e);
		}finally{
			if(pw != null){
				pw.close();
			}
		}
	}
}
