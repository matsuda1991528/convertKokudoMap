import javax.swing.*;

import java.io.File;
import java.awt.BorderLayout;
import java.awt.event.*;

public class FileChooser extends JFrame implements ActionListener{
	JLabel label;
	
	public static void main(String[] args){
		FileChooser frame = new FileChooser();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(10, 10, 300, 200);
		frame.setTitle("タイトル");
		frame.setVisible(true);
	}
	
	FileChooser(){
		JButton button = new JButton("ファイルの選択");
		button.addActionListener(this);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(button);
		
		label = new JLabel();
		
		JPanel labelPanel = new JPanel();
		labelPanel.add(label);
		
		getContentPane().add(labelPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
	}

	public void actionPerformed(ActionEvent e){
		JFileChooser filechooser = new JFileChooser();
		
		int selected = filechooser.showDialog(this, "実行する");
		if(selected == JFileChooser.APPROVE_OPTION){
			File file = filechooser.getSelectedFile();
			
			XmlReader xmlreader = new XmlReader();
			String xmlUrl;
			xmlUrl = file.getAbsolutePath();
			System.out.println(xmlUrl);
			try{
				xmlreader.parseXml(file.getAbsolutePath());
			}catch(Exception ex){
				System.out.println("errror");
			}
				
			label.setText(file.getName());
		}
		else if(selected == JFileChooser.CANCEL_OPTION){
			label.setText("キャンセルされました");
		}
		else if(selected == JFileChooser.ERROR_OPTION){
			label.setText("エラー又は取り消しがありました");
		}
	}
}
