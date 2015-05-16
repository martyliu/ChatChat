package com.chatchat.model;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.UUID;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;

/**
 * ���ͺͽ���¼������
 *
 */
public class Media {
	private String sendPath, receivePath;//��Ƶ�ļ������ַ
	private String name;//�����ļ�������
	private MediaRecorder myRecorder;
	private MediaPlayer myPlayer;
	private File saveFilePath;
	
	public static Random random = new Random();
	
	public Media(){
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			//����Ҫ���͵�¼���ı���·��
			try{
				sendPath = Environment.getExternalStorageDirectory().
						getCanonicalPath().toString() + "/ChatChatMessageMediaSend";
				File files = new File(sendPath);
				if (!files.exists()) {
					files.mkdir();
				}
			}catch (Exception e){
				e.getStackTrace();
			}
		}
		
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			//���ý��յ���¼����ŵ�·��
			try {
				receivePath = Environment.getExternalStorageDirectory()
						.getCanonicalPath().toString()
						+ "/ChatChatMessageMediaReceive";
				File files = new File(receivePath);
				if (!files.exists()) {
					files.mkdir();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void startRecord(){
		myRecorder = new MediaRecorder();
		myRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);//���ô���˷�¼��
		myRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);//���������ʽ
		myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//���ñ����ʽ
		
		this.name = "AND" + getRandomId() + new SimpleDateFormat(
				"yyyyMMddHHmmss").format(System
				.currentTimeMillis()) + ".amr";//���ļ�������AND + 4λ����� + ���� + .amr
		
		String paths = sendPath + "/" + name;	//�ļ��ľ���·��
		saveFilePath = new File(paths);	//��ȡ���ļ�����
		
		myRecorder.setOutputFile(saveFilePath.getAbsolutePath());//����¼��������ļ�
		
		try{
			saveFilePath.createNewFile();
			myRecorder.prepare();
		}catch(Exception e){
			e.getStackTrace();
		}
		
		myRecorder.start();//��ʼ¼��
	}
	
	public void stopRecord(){
		if (saveFilePath.exists() && saveFilePath != null) {
			myRecorder.stop();
			myRecorder.release();	
			myRecorder = null;
		}
	}
	
	public void startPlay(String path0){
		myPlayer = new MediaPlayer();
		
		try{
			myPlayer.reset();
			myPlayer.setDataSource(path0);
			if(!myPlayer.isPlaying()){
				myPlayer.prepare();
				myPlayer.start();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static String getRandomId(){
		return random.nextInt(9999)+"";
	}
	
	public String getName(){
		return name;
	}
	public String getSendPath(){
		return sendPath;
	}
	public String getReceivePath(){
		return receivePath;
	}
}
