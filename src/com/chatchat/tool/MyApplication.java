package com.chatchat.tool;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import com.chatchat.model.UdpMessage;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Debug;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class MyApplication extends Application{
	public static MyApplication appInstance;//����ģʽ
	private String localIp;//���ص�ip��ַ
	private String deviceCode;//�ֻ���Ψһ��ʶ��
	
	public static String iconPath;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		//��ʼ��
		localIp = getLocalIpAddress();
		appInstance = this;
		iconPath = getFilesDir()+"/";
		getDeviceId();
		
	}
	
	public UdpMessage generateMyMessage(String msg,int type){
		UdpMessage message=new UdpMessage();
		message.setType(type);
		message.setSenderName(getMyName());
		message.setDestIp("");
		message.setMsg(msg);
		message.setDeviceCode(getDeviceCode());
		message.setOwn(true);
		return message;
	}
	
	/**
	 * �õ�����IP��ַ
	 * @return
	 */
		private String getLocalIpAddress(){
			try{
				Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
				while(en.hasMoreElements()){
					NetworkInterface nif = en.nextElement();
					Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses();
					while(enumIpAddr.hasMoreElements()){
						InetAddress mInetAddress = enumIpAddr.nextElement();
						if(!mInetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(mInetAddress.getHostAddress())){
							return mInetAddress.getHostAddress();
						}
					}
				}
			}catch(SocketException e){
				e.printStackTrace();
				Toast.makeText(this, "��ȡ����IP��ַʧ��", Toast.LENGTH_SHORT).show();
			}
			return null;
		}
	
	/**
	 * ��ȡ�豸Ψһ��ʶ
	 */
	private void getDeviceId(){
		 TelephonyManager telephonyManager=(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		 setDeviceCode(telephonyManager.getDeviceId());
		 //Log.d("=============", "DeviceId  :"+deviceCode);
		 if(getDeviceCode()==null){
			 WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE); 
			 WifiInfo info = wifi.getConnectionInfo(); 
			 setDeviceCode(info.getMacAddress());  
		 }
		 if(getDeviceCode()==null){
			 setDeviceCode(getSharedPreferences("me", 0).getString("deviceCode", System.currentTimeMillis()+""));
			 getSharedPreferences("me", 0).edit().putString("deviceCode", getDeviceCode()).commit();
		 }
	}
	
	public String getMyName() {
		return getSharedPreferences("me", 0).getString("name", "����");
	}
	
	public String getLocalIp(){
		if(localIp == null)
			localIp = getLocalIpAddress();
		return localIp;
	}
	public String getDeviceCode() {
		return deviceCode;
	}
	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}
}
