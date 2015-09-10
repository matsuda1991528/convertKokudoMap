import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;


public class GnuplotUser extends JFrame{
	private static final String GNUPLOT = "C:\\Program Files\\gnuplot\\bin\\pgnuplot.exe"; //Gnuplotのアプリケーションファイルへのパス
	private static final String IMG_FILE_NAME = "test.jpg"; //出力する画像ファイル名
	
	public static void plotGnuplot(String plt_file_name) throws IOException{
		//Process p = new ProcessBuilder(GNUPLOT, "sin(x)").start();
		Process p = Runtime.getRuntime().exec(GNUPLOT); //外部プロセスとしてGNUPLOTの呼び出し
		PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(p.getOutputStream())));
		
		//gnuplotへ打ち込む命令達
		pw.println("unset xtics");
		pw.println("unset ytics");
		pw.println("plot \'"+plt_file_name+"\' u 3:4 with lines lc rgb 'grey' notitle");
		pw.println("set term jpeg");
		pw.println("set output \'"+IMG_FILE_NAME+"\'");
		pw.println("replot");
		pw.flush();
		pw.close();		
		
		try{
			//スレッドを5000msスリープ状態にする．（この間にgnuplotタスクを実行）
			Thread.sleep(5000);
		}
		catch(InterruptedException e){}
		BufferedImage readImg = null;
		try{
			readImg = ImageIO.read(new File(IMG_FILE_NAME));
		}catch(Exception e){
			e.printStackTrace();
			readImg = null;
		}
		ImageIcon icon = new ImageIcon(readImg);
		drawMap(icon);
	}
	
	private static void drawMap(ImageIcon icon){
		GnuplotUser frame = new GnuplotUser(icon);
		
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(0, 0, icon.getIconWidth(),icon.getIconHeight());
		frame.setTitle(IMG_FILE_NAME);
		frame.setVisible(true);
		frame.setIconImage(icon.getImage());
	}

	GnuplotUser(ImageIcon icon){		
		JLabel label = new JLabel(icon);
		JPanel p = new JPanel();
		p.add(label);
		
		getContentPane().add(p, BorderLayout.CENTER);
	}	
}
