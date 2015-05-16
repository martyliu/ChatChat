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
import com.chatchat.service.ChatService;
import com.chatchat.tool.MyApplication;

public class ImageTcpServer{
	ChatService service = null;
	UdpMessage msg;
	String senderIp;//��Ϣ����Դ
	
	public ImageTcpServer(UdpMessage msg, String ip, ChatService service){
		this.msg = msg;
		senderIp = ip;
		this.service = service;
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
			ServerSocket ss =new ServerSocket(2224);
			Socket s = ss.accept();//��ʼ����
			
			String[] info = ((String)msg.getMsg()).split("/");
			String tmp = new Media().getReceivePath() + "/" +info[info.length-1];
			File file = new File(tmp);//���յ����ļ��Ĵ洢·��
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
			msg.setType(ChatService.RECEIVE_IMAGE);
			msg.setMsg(tmp);
			String hostAddress = senderIp;
			if (service.getMsgs().containsKey(hostAddress)) {
				service.getMsgs().get(hostAddress).add(msg);
			} else {
				Queue<UdpMessage> queue = new ConcurrentLinkedQueue<UdpMessage>();
				queue.add(msg);
				service.getMsgs().put(hostAddress, queue);
			}
			
			service.onReceiver(ChatService.RECEIVE_IMAGE);
 		}
	}
	

}