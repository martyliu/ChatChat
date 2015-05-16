package com.chatchat.ui;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import com.chatchat.model.Media;
import com.chatchat.model.UdpMessage;
import com.chatchat.model.User;
import com.chatchat.service.ChatService;
import com.chatchat.service.ChatService.MyBinder;
import com.chatchat.tool.MyApplication;
import com.chatchat.tool.Util;
import com.example.chatchat.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity implements OnClickListener{
	private ListView listView;
	private Button sendBtn, mediaBtn;
	private Button imageBtn;
	private EditText msgEdt;

	private MyConnection conn;
	private MyBinder binder;
	private ChatAdapter adapter;
	private RefreshBroadcastReceiver receiver = new RefreshBroadcastReceiver();
	
	
	private List<UdpMessage> myMessages = new ArrayList<UdpMessage>();
	private User chatter = new User();
	private String ChatterIp, ChatterDeviceCode, ChatterName;

	private Media media = new Media();  //��Ƶ�࣬����¼������
	private String mediaName = null;
	public static final int REQUEST_SEND_IMAGE = 108;
	public static final int RECEIVE_MEDIA = 106;
	public static final int REQUEST_SEND_MEDIA = 104;
	public static final int TEXT_MESSAGE = 103;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		// ��ʼ������Է�
		ChatterIp = getIntent().getStringExtra("IP");
		ChatterDeviceCode = getIntent().getStringExtra("DeviceCode");
		ChatterName = getIntent().getStringExtra("name");
		chatter.setIp(ChatterIp);
		chatter.setDeviceCode(ChatterDeviceCode);
		chatter.setName(ChatterName);

		initView();// ��ʼ������
		initService();// ��ʼ��service
		initReceiver();// ��ʼ���㲥������

	}

	/**
	 * ��ʼ������
	 */
	private void initView() {
		listView = (ListView) findViewById(R.id.chat_listview);
		sendBtn = (Button) findViewById(R.id.send_btn);
		mediaBtn = (Button) findViewById(R.id.audio_btn);
		msgEdt = (EditText) findViewById(R.id.chat_edt);
		imageBtn = (Button)findViewById(R.id.send_image_btn);
		sendBtn.setOnClickListener(this);
		final Dialog dialog = new Dialog(ChatActivity.this, R.style.MyDialog);
		dialog.setContentView(R.layout.activity_record);
		
		mediaBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN:
					
					
					dialog.show();
					media.startRecord();
					break;
				case MotionEvent.ACTION_UP:
					dialog.dismiss();
					media.stopRecord();
					
					sendMediaRecord(media.getName());
					//�˴���ӷ�����Ƶ  sendMediaRecord(media.getName());
					//		�ú���������1������Ƶ���͵�ָ����ip���ȷ�����Ƶ֪ͨ��֪ͨ�Է���ͨtcp�˿ڽ��գ�
					//			        ��2��ˢ��������棬�ڽ��������һ��������Ϣ
					//			        ��3�����õ���¼����������������Ϣ���ͻᲥ������
					
					break;
				}
				return false;
			}
		});
		
		imageBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(intent,1);
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.send_btn:
			String txt = msgEdt.getText().toString();
			if(txt.length() <= 0 )	return ;
			try {
				sendMsg(MyApplication.appInstance.generateMyMessage(txt, ChatService.TEXT_MESSAGE));
				//������Ϣ����1����ȡ�������chatter
				//		  ��2���ж϶Է��Ƿ�����
				//		  ��3�����ߣ�������Ϣ��ȥ
				//    	  ��4�������������
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			Uri uri = data.getData();
			try{
				String[] pojo = {MediaStore.Images.Media.DATA};
				Cursor cursor = managedQuery(uri, pojo, null, null, null);
				if(cursor !=null){
					ContentResolver cr = this.getContentResolver();
					int colunm_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					String path = cursor.getString(colunm_index);
					
					//��ȡ��ͼƬ·��������
					//Toast.makeText(getApplicationContext(), path, Toast.LENGTH_SHORT).show();
					sendImageMsg(path);
					
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * �����ı���Ϣ
	 */
	private void sendMsg(UdpMessage msg) throws UnknownHostException{
		if(binder != null){
			binder.sendMsg(msg, chatter.getIp());
			myMessages.add(msg);
			msgEdt.setText("");
			refresh();
		}else{
			unbindService(conn);
			Intent intent1 = new Intent(ChatActivity.this, ChatService.class);
			startService(intent1);
		}
	}

	/**
	 * ������Ƶ��Ϣ
	 */
	private void sendMediaRecord(String name){
		/*mediaName = media.getSendPath() + "/" + name;
		String m = (new File(mediaName)).getName();*/
		UdpMessage msgTmp = MyApplication.appInstance.generateMyMessage(name, REQUEST_SEND_MEDIA);
		binder.sendMsg(msgTmp, chatter.getIp());
		//����һ�������ļ�������Ϣ ֪ͨ �Է�׼���ý�����Ƶ�ļ�
		
		//�ڴ˴�ˢ���Լ����������
		//��1��������������Ϣ���뵽��Ϣ����
		//��2������refresh������ˢ��adapter��adapter���ݽ��յ���msg���ͽ���ˢ�¡�
		myMessages.add(msgTmp);
		refresh();
	}
	
	/**
	 * ����ͼƬ��Ϣ
	 */
	private void sendImageMsg(String path){
		//����һ����������ͼƬ��֪ͨ
		UdpMessage msgTmp = MyApplication.appInstance.generateMyMessage(path, REQUEST_SEND_IMAGE);
		binder.sendMsg(msgTmp, chatter.getIp());
		
		myMessages.add(msgTmp);
		refresh();
	}
	
	
	/**
	 * ��service 1����ȡservice����������ȡ�����û���δ����Ϣ���Լ�����sendmsg������
	 * 2�������������listview���Զ���һ��adapter�����䣩 3����ȡ�Է���������Ϣ,���з���
	 */
	private void initService() {
		Intent intent = new Intent(ChatActivity.this, ChatService.class);
		bindService(intent, conn = new MyConnection(), Context.BIND_AUTO_CREATE);
	}

	/**
	 * ��service�󶨳ɹ�����еĲ��� 1����ȡservice����������ȡ�����û���δ����Ϣ���Լ�����sendmsg������
	 * 2�������������listview���Զ���һ��adapter�����䣩 3����ȡ�Է���������Ϣ,���з���
	 */
	public class MyConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (MyBinder) service;// ��ȡ����̨���պͷ�����Ϣ��service��������ȡ�����û���δ����Ϣ���Լ���������ķ�����
			Queue<UdpMessage> queue = binder.getMessages().get(chatter.getIp()); // ��ȡ�Է���������Ϣ����
			listView.setAdapter(adapter = new ChatAdapter());

			if (queue != null) {
				ergodicMessage(queue);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	}

	private void initReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(RefreshBroadcastReceiver.ACTION_NOTIFY_DATA);
		filter.addAction(RefreshBroadcastReceiver.ACTION_HEARTBEAT);
		registerReceiver(receiver, filter);
	}

	/**
	 * �㲥����������1��ˢ������
	 *		         ��2�����������ն�(δʵ�֣�
	 *
	 */
	public class RefreshBroadcastReceiver extends BroadcastReceiver {
		public static final String ACTION_HEARTBEAT = "com.chatchat.heartbeat";
		public static final String ACTION_NOTIFY_DATA = "com.chatchat.notifydata";

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("������", "���յ�ˢ��ҳ��Ĺ㲥�����ڽ���ˢ�¡�����");
			if((intent.getAction()).equals(ACTION_NOTIFY_DATA)){
				
				if(adapter != null){
					if(binder != null){
						Queue<UdpMessage> queue = binder.getMessages().get(chatter.getIp());
						if(queue != null){
							Log.i("��Ϣ����", "��Ϣ���в�Ϊ�գ�����");
							ergodicMessage(queue);
						}
					}else{
						unbindService(conn);
						Intent it = new Intent(ChatActivity.this, ChatService.class);
						bindService(it, conn= new MyConnection(), Context.BIND_AUTO_CREATE);
					}
				}else{
					adapter = new ChatAdapter();
				}
			}
		}
	}

	/**
	 * ������service����������Ϣ���У�������Ϣ��type������в�ͬ����
	 * TEXT_MESSAGE����ͨ�ı���Ϣ������뵽myMessages����
	 * @param queue
	 */
	private void ergodicMessage(Queue<UdpMessage> queue) {
		Iterator<UdpMessage> it = queue.iterator();
		UdpMessage message;
		while (it.hasNext()) {
			message = it.next();
			switch (message.getType()) {
			case ChatService.TEXT_MESSAGE:
			case ChatService.RECEIVE_MEDIA:
			case ChatService.RECEIVE_IMAGE:
				myMessages.add(message);
				break;
			}
			queue.clear();//ȡ����Ϣ���������
			refresh();
		}
	}
	
	private void refresh(){
		adapter.notifyDataSetChanged();
		listView.setSelection(adapter.getCount());
	}

	/**
	 * ���������������������Ϣ�Ĳ�ͬ���ͣ��Լ����͵ġ��Է����͵ģ�������Ӧ��item�����������
	 */
	class ChatAdapter extends BaseAdapter {
		private final int owner = 0;
		private final int other = 1;
		private final int owner_media = 2;
		private final int other_media = 3;
		private final int owner_image = 4;
		private final int other_image = 5;
		

		// ����ͼƬ����image��������������audio

		@Override
		public int getCount() {
			if (myMessages != null)
				return myMessages.size();
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public int getItemViewType(int position) {
			UdpMessage msg = myMessages.get(position);
			
			
			if (msg.isOwn() && (msg.getType()==TEXT_MESSAGE))
				return owner;
			else if(!msg.isOwn() && (msg.getType()==TEXT_MESSAGE))
				return other;
			else if(msg.isOwn() && (msg.getType()==REQUEST_SEND_MEDIA))
				return owner_media;
			else if(!msg.isOwn() && (msg.getType() == RECEIVE_MEDIA))
				return other_media;
			else if(msg.isOwn() && (msg.getType() == REQUEST_SEND_IMAGE))
				return owner_image;
			else
				return other_image;
		}

		@Override
		public int getViewTypeCount() {
			return 6;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			int type = getItemViewType(position);
			if (convertView == null) {
				holder = new ViewHolder();
				switch (type) {
				case owner:
					convertView = getLayoutInflater().inflate(
							R.layout.chat_my_listview, null);
					holder.txt = (TextView) convertView
							.findViewById(R.id.chat_my_txt);
					holder.chatterName = (TextView) convertView
							.findViewById(R.id.chat_my_name);
					break;
				case other:
					convertView = getLayoutInflater().inflate(
							R.layout.chat_other_listview, null);
					holder.txt = (TextView) convertView
							.findViewById(R.id.chat_other_txt);
					holder.chatterName = (TextView) convertView
							.findViewById(R.id.chat_other_name);
					break;
				case owner_media:
					convertView = getLayoutInflater().inflate(
							R.layout.chat_my_media_listview,null);
					holder.media = (Button) convertView.findViewById(R.id.chat_my_media_button);
					holder.chatterName = (TextView) convertView.findViewById(R.id.chat_my_media_name);
					break;
				case other_media:
					convertView = getLayoutInflater().inflate(R.layout.chat_other_media_listview, null);
					holder.media = (Button) convertView.findViewById(R.id.chat_other_media_button);
					holder.chatterName = (TextView) convertView.findViewById(R.id.chat_other_media_name);
					break;
				case owner_image:
					convertView = getLayoutInflater().inflate(R.layout.chat_my_image_listview, null);
					holder.picture = (ImageView) convertView.findViewById(R.id.chat_my_image);
					holder.chatterName = (TextView) convertView.findViewById(R.id.chat_my_image_name);
					break;
				case other_image:
					convertView = getLayoutInflater().inflate(R.layout.chat_other_image_listview, null);
					holder.picture = (ImageView) convertView.findViewById(R.id.chat_other_image);
					holder.chatterName = (TextView) convertView.findViewById(R.id.chat_other_image_name);
					break;
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			switch (type) {
			case owner:
				UdpMessage message = myMessages.get(position);
				String content = message.getMsg();
				holder.txt.setText(content);
				holder.chatterName.setText(MyApplication.appInstance.getMyName());
				break;
			case other:
				message = myMessages.get(position);
				content = message.getMsg();
				holder.txt.setText(content);
				holder.chatterName.setText(chatter.getName());
				break;
			case owner_media:
				message = myMessages.get(position);
				final String mediaName = message.getMsg(); //��Ƶ������
				MediaPlayer mp = MediaPlayer.create(ChatActivity.this,Uri.parse(media.getSendPath() + "/" + mediaName));
				int duration = mp.getDuration()/1000;
				
				holder.media.setText(duration+"\"" + " (((");
				holder.chatterName.setText(MyApplication.appInstance.getMyName());
				holder.media.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
							media.startPlay(media.getSendPath() + "/" + mediaName);
						}
					});
				break;
				
			case other_media:
				message = myMessages.get(position);
				final String mediaName1 = message.getMsg(); //��Ƶ������
				mp = MediaPlayer.create(ChatActivity.this,Uri.parse(media.getReceivePath() + "/" + mediaName1));
				duration = mp.getDuration()/1000;
				holder.media.setText("))) "+duration+"\"");
				holder.chatterName.setText(chatter.getName());
				holder.media.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
							media.startPlay(media.getReceivePath()+ "/" + mediaName1);
						}
					});
				break;
	
			case owner_image:
				message = myMessages.get(position);
				String pathTmp = message.getMsg();//ͼƬ·��
				
				//��ͼƬ��������
				Options opts = new Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(pathTmp, opts);
				int imageWidth = opts.outWidth;
				int imageHeight = opts.outHeight;
				Display display = ChatActivity.this.getWindowManager().getDefaultDisplay();
				int screenWidth = display.getWidth();
				int screenHeight = display.getHeight();
				int widthScale = imageWidth / screenWidth;
				int heightScale = imageHeight / screenHeight;
				int scale = widthScale > heightScale ? widthScale:heightScale;
				opts.inJustDecodeBounds= false;
				opts.inSampleSize = scale;
				//��ȡ���ź��ͼƬ����ʾ�����������
				Bitmap bm = BitmapFactory.decodeFile(pathTmp,opts);
				holder.picture.setImageBitmap(bm);
				holder.chatterName.setText(MyApplication.appInstance.getMyName());
				break;
				
			case other_image:
				message = myMessages.get(position);
				pathTmp = message.getMsg();//ͼƬ·����
				//��ͼƬ��������
				opts = new Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(pathTmp, opts);
				imageWidth = opts.outWidth;
				imageHeight = opts.outHeight;
				display = ChatActivity.this.getWindowManager().getDefaultDisplay();
				screenWidth = display.getWidth();
				screenHeight = display.getHeight();
				widthScale = imageWidth / screenWidth;
				heightScale = imageHeight / screenHeight;
				scale = widthScale > heightScale ? widthScale:heightScale;
				opts.inJustDecodeBounds= false;
				opts.inSampleSize = scale;
				//��ȡ���ź��ͼƬ����ʾ�����������
				bm = BitmapFactory.decodeFile(pathTmp,opts);
				holder.picture.setImageBitmap(bm);
				Toast.makeText(getApplicationContext(), "���յ�ͼƬ�����浽"+pathTmp, Toast.LENGTH_LONG);
				holder.chatterName.setText(chatter.getName());
				break;
			}
			
			return convertView;
		}

		class ViewHolder {
			ImageView icon;
			TextView txt;
			TextView chatterName;
			Button media;
			ImageView picture;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
		unregisterReceiver(receiver);
	}
}
