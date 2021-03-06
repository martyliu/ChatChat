package com.chatchat.model;

import java.io.Serializable;

public class User implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String name;//用户姓名
	private String ip;//用户ip地址
	private String deviceCode;//用户的唯一标识码
	private String heartTime;//用户上一次心跳时间（用于判断用户仍然在线）,十秒跳一次10,000ms
	private boolean refreshIcon;//记录是否刷新头像（登录第一次会刷新头像）
	
	//构造函数
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
	 * 验证对方是否在线
	 * @return
	 */
	public boolean checkOnline(){
		return !(System.currentTimeMillis()-Long.valueOf(heartTime)>21000);
	}
}
