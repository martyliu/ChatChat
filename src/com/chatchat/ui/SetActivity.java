package com.chatchat.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.chatchat.tool.MemoryCache;
import com.chatchat.tool.Util;
import com.example.chatchat.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * ����ͷ����ǳƵĽ���
 *
 */
public class SetActivity extends Activity implements OnClickListener {
	private ImageView icon;
	private Button iconButton, nicknameButton;
	private EditText nicknameEdt;

	private final int CUT_PHOTO_REQUEST_CODE = 201;
	private final int SELECT_PHOTO_REQUEST_CODE = 200;

	private String iconPath;// �Լ���ͷ���·��
	public static String iconName = "me";// �洢�û��Լ���Ϣ���ļ���

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_set);

		iconPath = getFilesDir() + File.separator + iconName;// ���û��Լ���ͷ��Ĵ洢·����ֵ
		Log.i("filepath", iconPath);

		initView();//�󶨲���

		// ����ͷ��imageView�е�����
		setIcon();

		// �����ǳ�EditText�е����ݣ���me�ĵ��л�ȡ����������Ϣ
		nicknameEdt.setText(getSharedPreferences("me", MODE_PRIVATE).getString(
				"name", "����"));

		// ����button�¼�
		icon.setOnClickListener(this);// ѡ��ͼƬ��ť
		nicknameButton.setOnClickListener(this);// ȷ���޸��ǳư�ť
	}

	private void initView() {
		// �󶨲���
		icon = (ImageView) findViewById(R.id.setactivity_icon_image);
		//iconButton = (Button) findViewById(R.id.setactivity_select_icon_btn);
		nicknameButton = (Button) findViewById(R.id.setactivity_change_nickname_btn);
		nicknameEdt = (EditText) findViewById(R.id.setactivity_nickname_edt);
	}

	private void setIcon() {
		Bitmap bitmap = MemoryCache.getInstance().get(iconName);
		// �ж��Ƿ��ڻ�����
		if (bitmap != null) {
			Log.i("test", "ͼƬ�ڻ�����");
			icon.setImageBitmap(Util.getRoundedCornerBitmap(bitmap));
		} else {
			bitmap = BitmapFactory.decodeFile(iconPath);// ����ļ�·�������Ƿ񱣴����ļ�����
			if (bitmap == null) {
				// �ļ�·����Ҳû�У�����һ��Ĭ��ͼ
				icon.setImageResource(R.drawable.ic_launcher);
			} else {
				Log.i("test", "ͼƬ���ļ�·����");
				// �ļ�·���д��ڣ���
				icon.setImageBitmap(Util.getRoundedCornerBitmap(bitmap));
				MemoryCache.getInstance().put(iconName, bitmap);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setactivity_icon_image:
			try {
				Intent i = new Intent(Intent.ACTION_PICK,
						Media.EXTERNAL_CONTENT_URI);
				i.setType("image/*");
				startActivityForResult(i, SELECT_PHOTO_REQUEST_CODE);// ��ϵͳ���
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case R.id.setactivity_change_nickname_btn:
			String nickName = nicknameEdt.getText().toString().trim();
			if (nickName.equals("")) {
				Toast.makeText(getApplicationContext(), "�ǳƲ���Ϊ��",
						Toast.LENGTH_LONG).show();
				return;
			}
			Editor editor = getSharedPreferences("me", MODE_PRIVATE).edit();// ���µ��ǳƴ����ļ���
			editor.putString("name", nickName);
			editor.commit();
			Toast.makeText(getApplicationContext(), "�ɹ��޸�", Toast.LENGTH_LONG)
					.show();
			break;
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK
				&& data != null) {
			Uri uri = data.getData();
			if (uri != null) {
				// �ü�ͼƬ
				final Intent intent = new Intent(
						"com.android.camera.action.CROP");
				intent.setDataAndType(uri, "image/*");
				intent.putExtra("crop", "true");
				intent.putExtra("aspectX", 1);
				intent.putExtra("aspectY", 1);
				intent.putExtra("outputX", 100);
				intent.putExtra("outputY", 100);
				intent.putExtra("return-data", true);
				startActivityForResult(intent, CUT_PHOTO_REQUEST_CODE);
			}
		} else if (requestCode == CUT_PHOTO_REQUEST_CODE
				&& resultCode == RESULT_OK && data != null) {
			try {
				// ��ȡ�ü����Բ��ͼƬ
				Bitmap bitmap = data.getParcelableExtra("data");
				bitmap = Util.getRoundedCornerBitmap(bitmap);
				icon.setImageBitmap(bitmap);
				File file = new File(iconPath);
				file.delete();// ɾ��֮ǰ������ͼƬ
				file.createNewFile();
				FileOutputStream outputStream = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);//ת��jpeg
				outputStream.flush();
				outputStream.close();
				
				MemoryCache.getInstance().put(iconName, bitmap);// ���뵽������
				Toast.makeText(getApplicationContext(), "�ɹ��޸�ͷ��",
						Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "����ʧ��",
						Toast.LENGTH_LONG).show();
			}
		}
	}

}
