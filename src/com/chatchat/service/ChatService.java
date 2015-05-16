package com.chatchat.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import com.chatchat.model.UdpMessage;
import com.chatchat.model.User;
import com.chatchat.tool.MyApplication;
import com.chatchat.ui.MainActivity;
import com.chatchat.util.IconTcpClient;
import com.chatchat.util.ImageTcpClient;
import com.chatchat.util.ImageTcpServer;
import com.chatchat.util.MediaTcpClient;
import com.chatchat.util.MediaTcpServer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class ChatService extends Service {
	public static final int MESSAGE_PORT = 2425;
	// ��Ϣ����
	public static final int ON_LINE = 101;
	public static final int REPLY_ONLINE = 102;
	public static final int TEXT_MESSAGE = 103;
	public static final int REQUEST_SEND_MEDIA = 104;
	public static final int REPLY_SEND_MEDIA = 105;
	public static final int RECEIVE_MEDIA = 106;
	public static final int REQUIRE_ICON = 107;
	public static final int REQUEST_SEND_IMAGE = 108;
	public static final int REPLY_SEND_IMAGE = 109;
	public static final int RECEIVE_IMAGE = 110;



	public static final String ACTION_HEARTBEAT = "com.chatchat.heartbeat";
	public static final String ACTION_NOTIFY_DATA = "com.chatchat.notifydata";

	private MulticastSocket messageSocket = null;
	private ExecutorService executor = Executors.newFixedThreadPool(20);// ����һ���̶���С���̳߳���������Ϣ����СΪ20���߳�
	
	
	private MyBinder myBinder = new MyBinder();
	public Server server = new Server();

	private List<User> users = new ArrayList<User>();
	final Map<String, Queue<UdpMessage>> messages = new ConcurrentHashMap<String, Queue<UdpMessage>>();

	@Override
	public IBinder onBind(Intent intent) {
		return myBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// ��ʼ��socket
		try {
			messageSocket = new MulticastSocket(MESSAGE_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		server.start();// ����������Ϣ�߳�
		onLine();

	}

	/**
	 * ������Ϣ��һ��
	 */
	class Server extends Thread {
		@Override
		public void run() {
			super.run();

			byte[] data = new byte[2 * 1024];
			DatagramPacket dp = new DatagramPacket(data, data.length);

			while (!server.isInterrupted()) {
				try {
					messageSocket.receive(dp);// ������������Ϣ

					String tmp = new String(dp.getData(), 0, dp.getLength(),
							"gbk");
					deal(tmp, dp.getAddress().getHostAddress());// ������ȡ������Ϣ

					dp.setLength(data.length);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * ������յ�����Ϣ
		 */
		private void deal(String tmp, String hostAddress) throws JSONException {
			UdpMessage msg = new UdpMessage(new JSONObject(tmp));//
			int type = msg.getType();// �������Ͷ���Ϣ���в���(��ʱ���ã�
			switch (type) {
			case ON_LINE:
				User user = new User();
				user.setName(msg.getSenderName());
				user.setIp(hostAddress);
				user.setDeviceCode(msg.getDeviceCode());
				
				boolean flag = true;//������Ѿ����ڵ��û�
				for(User userTmp:users){
					if(userTmp.getIp() == hostAddress){
						flag = false;
						break;
					}
				}
				
				// ���������Ϣ��Դͷ�����Լ����������ӵ������б����ҷ���һ��REPLY����Ϣ
				if (!(MyApplication.appInstance.getLocalIp()).equals(hostAddress) && flag) {
					users.add(user);
					send(MyApplication.appInstance.generateMyMessage("",
							REPLY_ONLINE).toString(), hostAddress);
				}

				break;

			case REPLY_ONLINE:
				user = new User();
				user.setName(msg.getSenderName());
				user.setIp(hostAddress);
				user.setDeviceCode(msg.getDeviceCode());
				flag = true;//������Ѿ����ڵ��û�
				for(User userTmp:users){
					if(userTmp.getIp() == hostAddress){
						flag = false;
						break;
					}
				}
				if(flag)
					users.add(user);
				
				break;

			case TEXT_MESSAGE:
				if (messages.containsKey(hostAddress)) {
					messages.get(hostAddress).add(msg);
				} else {
					Queue<UdpMessage> queue = new ConcurrentLinkedQueue<UdpMessage>();
					queue.add(msg);
					messages.put(hostAddress, queue);
				}
				break;
			
			case REQUEST_SEND_MEDIA:
				//��tcp�����׼��������Ƶ
				MediaTcpServer ts = new MediaTcpServer(msg, hostAddress, ChatService.this);
				ts.start();
				
				UdpMessage msg1 = MyApplication.appInstance.generateMyMessage(msg.getMsg(), REPLY_SEND_MEDIA);
				send(msg1.toString(), hostAddress);//����һ����Ӧ��Ϣ
				break;
				
			case REPLY_SEND_MEDIA:
				MediaTcpClient tc = new MediaTcpClient(msg,hostAddress);
				tc.start();
				
				break;
				
			case REQUEST_SEND_IMAGE:
				ImageTcpServer its = new ImageTcpServer(msg, hostAddress, ChatService.this);
				its.start();
				
				UdpMessage msg2 = MyApplication.appInstance.generateMyMessage(msg.getMsg(), REPLY_SEND_IMAGE);
				send(msg2.toString(), hostAddress);//����һ����Ӧ��Ϣ
				break;
			
			case REPLY_SEND_IMAGE:
				ImageTcpClient imtc = new ImageTcpClient(msg, hostAddress);
				imtc.start();
				break;
				
			case REQUIRE_ICON:
				IconTcpClient itc = new IconTcpClient(hostAddress);//���Լ���ͷ���͵�ָ��ip
				itc.start();
			
			}	
			onReceiver(type);// ����һ���㲥��֪ͨ��ҳ��ˢ�º����б�
		}
	}
	
	public void onReceiver(int type) {
		switch(type){
		case ON_LINE:
		case REPLY_ONLINE:
			sendBroadcast(new Intent(MainActivity.ACTION_REFRESH));
			break;
		case TEXT_MESSAGE:
		case RECEIVE_MEDIA:
		case RECEIVE_IMAGE:
			Log.i("�㲥", "ˢ�����������");
			sendBroadcast(new Intent(ACTION_NOTIFY_DATA));
			break;
		}
	}
	
	/**
	 * �ھ������ڹ㲥һ���Լ����ߵ���Ϣ����Ŀ�ĵ�ַΪ"255.255.255.255")
	 */
	public void onLine() {
		send(MyApplication.appInstance.generateMyMessage("", ON_LINE)
				.toString(), "255.255.255.255");
		Log.i("������", "����������");
	}

	/**
	 * ����һ����Ϣ��message�˿�
	 */
	private void send(final String msg, final String destip) {
		executor.execute(new Runnable() {
			public void run() {
				try {
					DatagramPacket dps = new DatagramPacket(
							msg.getBytes("gbk"), msg.length(), InetAddress
									.getByName(destip), MESSAGE_PORT);
					messageSocket.send(dps);

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		messageSocket.close();
		server.interrupt();
		Log.i("Service", "�ұ���������");
	}

	public Map<String, Queue<UdpMessage>> getMsgs() {
		return messages;
	}
	
	public class MyBinder extends Binder {
		public List<User> getUsers() {
			return users;
		}

		public Map<String, Queue<UdpMessage>> getMessages() {
			return messages;
		}

		public void sendMsg(UdpMessage msg, String destIp) {
			send(msg.toString(), destIp);
			Log.i("������", "�����ť���ͳɹ�");
		}
	}
}
