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
	public static MyApplication appInstance;//单例模式
	private String localIp;//本地的ip地址
	private String deviceCode;//手机的唯一标识码
	
	public static String iconPath;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		//初始化
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
	 * 得到本机IP地址
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
				Toast.makeText(this, "获取本机IP地址失败", Toast.LENGTH_SHORT).show();
			}
			return null;
		}
	
	/**
	 * 获取设备唯一标识
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
		return getSharedPreferences("me", 0).getString("name", "无名");
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
