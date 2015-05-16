package com.chatchat.ui;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chatchat.model.UdpMessage;
import com.chatchat.model.User;
import com.chatchat.service.ChatService;
import com.chatchat.service.ChatService.MyBinder;
import com.chatchat.tool.MemoryCache;
import com.chatchat.tool.MyApplication;
import com.chatchat.tool.Util;
import com.chatchat.util.IconTcpServer;
import com.example.chatchat.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final int MESSAGE_PORT = 2425;
	public static final int ON_LINE = 101;
	public static final String ACTION_REFRESH = "com.chatchat.refresh";
	private MulticastSocket messageSocket = null;
	public static MyBinder binder;
	private boolean binded = false;//��־��ǰ�Ƿ���chatService��
	
	MyServiceConnection conn;
	RefreshReceiver receiver = new RefreshReceiver();

	private UserAdapter adapter;
	private ListView listView;

	/* public MyHandler myHandler = new MyHandler(); */
	TextView tv, user_ip;

	private List<User> users = new ArrayList<User>();
	private User myself = new User(); 

	/**
	 * ����ChatService��MainActivity������connection
	 */
	public class MyServiceConnection implements ServiceConnection {
		// ֻ�������ӳɹ���һ�̵���
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (MyBinder) service;// ��ȡ��ChatService���󣬿ɴ��е�����Ҫ����Ϣ������activityҳ��
			binded = true;// ��Service�ı�־
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//��ʼ�����ң������ڲ��ԣ�
		myself.setName(MyApplication.appInstance.getMyName());
		myself.setIp(MyApplication.appInstance.getLocalIp());
		myself.setDeviceCode(MyApplication.appInstance.getDeviceCode());
		
		
		initService();// ��ʼ��Service
		initUserList();// ��ʼ���û��б�
		initReceiver();//ע��㲥������������ˢ���û��б�Ĺ㲥������
		
	}

	/**
	 * ��ʼ������
	 */
	private void initService() {
		Intent serviceIntent = new Intent(MainActivity.this, ChatService.class);
		startService(serviceIntent);// ��������
		bindService(serviceIntent, conn = new MyServiceConnection(),
				Context.BIND_AUTO_CREATE);
	}

	/**
	 * ��ʼ���û��б�
	 */
	private void initUserList() {
		listView = (ListView) findViewById(R.id.user_list);

		/**
		 * ����û��������������
		 */
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(binded){
					unbindService(conn);
					binded=false;
				}
				Intent intent = new Intent(MainActivity.this, ChatActivity.class);
				User chatter = users.get(position);
				intent.putExtra("IP", chatter.getIp());
				intent.putExtra("DeviceCode" , chatter.getDeviceCode());
				intent.putExtra("name",chatter.getName());
				startActivity(intent);
			}
		});

	}

	/**
	 * ������Ϣ��һ��(���ڽ����ڲ��ԣ��ֶ�����һ��������Ϣ)
	 */
	class Client extends Thread {
		@Override
		public void run() {
			super.run();
			// �򿪷�����Ϣ��socket
			try {
				String tmp = MyApplication.appInstance.generateMyMessage(
						"Hello", ON_LINE).toString();
				DatagramPacket dps = new DatagramPacket(tmp.getBytes("gbk"),
						tmp.length(), InetAddress.getByName("255.255.255.255"),
						MESSAGE_PORT);
				messageSocket.send(dps);
				Log.i("������", "�ɹ�����");
				interrupt();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * �û��б�������adapter
	 */
	public class UserAdapter extends ArrayAdapter<User> {
		private int resourceId;

		public UserAdapter(Context context, int resource, List<User> objects) {
			super(context, resource, objects);
			resourceId = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			User tmp = getItem(position);
			View view;
			ViewHolder viewHolder;

			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(resourceId,
						null);// ��ȡ���Ӳ���
				viewHolder = new ViewHolder();
				viewHolder.userIcon = (ImageView) view
						.findViewById(R.id.user_list_icon);
				viewHolder.userName = (TextView) view
						.findViewById(R.id.user_list_name);
				viewHolder.userIp = (TextView) view
						.findViewById(R.id.user_list_ip);
				view.setTag(viewHolder);
			} else {
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}

			// �����û��б��е�item����ʾ
			viewHolder.userName.setText(tmp.getName());
			viewHolder.userIp.setText(tmp.getIp());
			
			if(position == 0){
				//�Լ�
				Bitmap bitmap = MemoryCache.getInstance().get("me");
				if(bitmap == null){
					//������û��ͼƬ������Ҫ���ļ��ж���
					bitmap = BitmapFactory.decodeFile(MyApplication.iconPath + "me");
					if(bitmap != null){
						//�ļ��д���
						viewHolder.userIcon.setImageBitmap(Util.getRoundedCornerBitmap(bitmap));
						MemoryCache.getInstance().put("me", bitmap);
					}
				}else{
					//����������ͼƬ
					viewHolder.userIcon.setImageBitmap(Util.getRoundedCornerBitmap(bitmap));
				}
			}else{
				//�����û�
				Bitmap bitmap1 = MemoryCache.getInstance().get(tmp.getDeviceCode());//�����û����豸���ڻ�����Ѱ�Ҷ�Ӧ��ͷ��
				if(bitmap1 == null){
					//�ڴ���û��,�����ļ��в���
					bitmap1 = BitmapFactory.decodeFile(MyApplication.iconPath + tmp.getDeviceCode());
					if(bitmap1 != null){//�ļ�����
						viewHolder.userIcon.setImageBitmap(Util.getRoundedCornerBitmap(bitmap1));
						MemoryCache.getInstance().put(tmp.getDeviceCode(), bitmap1);//���뻺����
						if(!tmp.isRefreshIcon()){
							reFreashIcon(tmp, viewHolder.userIcon);
						}
					}else{
						//�ļ���Ҳû��
						viewHolder.userIcon.setImageResource(R.drawable.ic_launcher);
						reFreashIcon(tmp, viewHolder.userIcon);
					}
				}else{
					//����������ͼƬ
					viewHolder.userIcon.setImageBitmap(Util.getRoundedCornerBitmap(bitmap1));
				}
			}
			return view;
		}

		class ViewHolder {
			TextView userName;
			TextView userIp;
			ImageView userIcon;
		}
	}
	
	/**
	 * ˢ���û�ͷ��
	 */
	public void reFreashIcon(User userTmp, View view){
		if(binder != null){
			//��һ������ͷ��ķ����
			IconTcpServer ts = new IconTcpServer(userTmp);
			ts.start();
			
			//����ͷ��������Ϣ
			UdpMessage message = MyApplication.appInstance.generateMyMessage("", ChatService.REQUIRE_ICON);
			binder.sendMsg(message, userTmp.getIp());
			
			
		}
	}
	
	private void initReceiver(){
		IntentFilter filter = new IntentFilter(ACTION_REFRESH);
		registerReceiver(receiver, filter);
	}
	
	/**
	 * �㲥�����������չ㲥��ˢ�º����б�
	 */
	class RefreshReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if(binder != null){
				users.clear();
				users.add(myself);
				List<User> listTmp = binder.getUsers();
				for (int i = 0; i < listTmp.size(); i++){
					users.add(listTmp.get(i));
				}
				if(adapter == null){
					adapter = new UserAdapter(MainActivity.this, R.layout.item_list, users);
					listView.setAdapter(adapter);
				}
				adapter.notifyDataSetChanged();
			}else{
				unbindService(conn);
				binded = false;
				bindService(new Intent(MainActivity.this, ChatService.class), conn=new MyServiceConnection(),Context.BIND_AUTO_CREATE);
			}
		}
	}

	
	
	
    long oldTime;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			long currentTime=System.currentTimeMillis();
			if(currentTime-oldTime<3*1000){
				finish();
			}else{
				Toast.makeText(getApplicationContext(), "�ٰ�һ���˳�", Toast.LENGTH_SHORT).show();
				oldTime=currentTime;
			}
		}
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		//�޸������ú�ˢ���Լ�
		myself.setName(MyApplication.appInstance.getMyName());
		
		adapter = new UserAdapter(MainActivity.this, R.layout.item_list, users);
		listView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		if(binded){
			unbindService(conn);
			binded = false;
		}
		stopService(new Intent(MainActivity.this, ChatService.class));
		Log.i("Activity", "activity�����ٰ���");
	}
	
	//actionbar ��� Setting
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	  // Handle presses on the action bar items
	  switch (item.getItemId()) {
	    case R.id.action_settings:
	    	Log.d("main","action_setting");
	    	Intent setIntent = new Intent(MainActivity.this, SetActivity.class);
	    	Log.d("main","before start");
			startActivity(setIntent);
	      return true;
	    default:
	      return super.onOptionsItemSelected(item);
	  }
	}
}
