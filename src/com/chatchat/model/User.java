package com.chatchat.model;

import java.io.Serializable;

public class User implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String name;//�û�����
	private String ip;//�û�ip��ַ
	private String deviceCode;//�û���Ψһ��ʶ��
	private String heartTime;//�û���һ������ʱ�䣨�����ж��û���Ȼ���ߣ�,ʮ����һ��10,000ms
	private boolean refreshIcon;//��¼�Ƿ�ˢ��ͷ�񣨵�¼��һ�λ�ˢ��ͷ��
	
	//���캯��
	public User(){
		setHeartTime(System.currentTimeMillis()+"");
		refreshIcon = false;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getDeviceCode() {
		return deviceCode;
	}
	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public String getHeartTime() {
		return heartTime;
	}

	public void setHeartTime(String heartTime) {
		this.heartTime = heartTime;
	}
	public boolean isRefreshIcon() {
		return refreshIcon;
	}
	public void setRefreshIcon(boolean refreshIcon) {
		this.refreshIcon = refreshIcon;
	}

	/**
	 * ��֤�Է��Ƿ�����
	 * @return
	 */
	public boolean checkOnline(){
		return !(System.currentTimeMillis()-Long.valueOf(heartTime)>21000);
	}
}
