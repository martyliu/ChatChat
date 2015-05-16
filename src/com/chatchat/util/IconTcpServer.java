package com.chatchat.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.chatchat.model.Media;
import com.chatchat.model.UdpMessage;
import com.chatchat.model.User;
import com.chatchat.service.ChatService;
import com.chatchat.tool.MemoryCache;
import com.chatchat.tool.MyApplication;

public class IconTcpServer{
	private User user;
	
	public IconTcpServer(User user){
		this.user = user;
	}
	
	public void start(){
		server s= new server();
		s.start();
	}
	
	class server extends Thread{ 
		@Override
		public void run() {
			super.run();
			try {
				createServer();//����tcp�����
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void createServer() throws IOException, Exception{
			ServerSocket ss =new ServerSocket(2223);
			Socket s = ss.accept();//��ʼ����
			
			
			File file = new File(MyApplication.iconPath + user.getDeviceCode());//���յ���ͷ��Ĵ洢·��
			if(file.exists()){//��֮ǰ��ͷ��ɾ����
				file.delete();
			}
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			
			
			BufferedInputStream is = new BufferedInputStream(s.getInputStream()); // ����
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));// д��
			Thread.sleep(1000);
			
			byte[] data = new byte[1024 * 5];
			int len = -1;
			
			while((len = is.read(data)) != -1){
				os.write(data,0,len);
			}
			
			is.close();
			os.flush();
			os.close();
			s.close();
			ss.close();
			
			
			//��msg������Ϣ������
			//����ChatService��onreceive�������㲥��֪ͨˢ���б�
			
			//MemoryCache.getInstance().put(user.getDeviceCode(), bitmap1);//���뻺����
 		}
	}
	

}
