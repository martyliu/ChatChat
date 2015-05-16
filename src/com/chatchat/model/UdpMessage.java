package com.chatchat.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

/**
 * ��Ϣ��ʵ���࣬�����߸��ݽ��յ�����Ϣ��type������Ϣ������Ӧ�Ĳ���
 *
 */
public class UdpMessage {
	private String senderName;//����������
	private String destIp;//Ŀ�ĵ�ip��ַ
	private String msg;//��Ϣ����
	private String sendTime;//����ʱ��
	private String deviceCode;//�ֻ�Ψһ��ʶ��
	private int type;//��Ϣ������
	private boolean own;//�ж�������Ϣ�Ƿ����Լ����͵�
	
	public UdpMessage(){
		setSendTime(System.currentTimeMillis()+"");
	}
	
	public UdpMessage(String msg,boolean own){
		this();
		this.setMsg(msg);
		this.setOwn(own);
	}
	
	/**
	 * �ڽ�����Ϣ��packet�л�ȡ��byte����Ϣ�����ɶ�Ӧ��String����Ϣ��������jsonobject��
	 * Ȼ����jsonObject������Ϊ���������ɶ�Ӧ��UdpMessage
	 * @throws JSONException 
	 */
	public UdpMessage(JSONObject object) throws JSONException{
		senderName = new String(Base64.decode(object.getString("senderName").getBytes(),Base64.DEFAULT));
		destIp = object.getString("destIp");
		msg = new String(Base64.decode(object.getString("msg").getBytes(),Base64.DEFAULT));
		setSendTime(object.getString("sendTime"));
		setDeviceCode(object.getString("deviceCode"));
		type = object.getInt("type");
		object = null;//����
	}
	
	/**
	 * ��UdpMessgaeת����JSONObject����Ȼ��תΪ�ַ�������ʽ����
	 * @return
	 */
	public String toString(){
		JSONObject object = new JSONObject();
		try {
			//�п��ܳ��������ַ��ı�����Ҫ����base64ת�룬���ⲻ�����ַ����µĴ���unterminated string character
			object.put("senderName", Base64.encodeToString(senderName.getBytes(), Base64.DEFAULT));
			object.put("destIp",destIp);
			object.put("msg",Base64.encodeToString(msg.getBytes(),Base64.DEFAULT));
			object.put("deviceCode",getDeviceCode());
			object.put("type",type);
			object.put("sendTime",sendTime);
			return object.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
		
	}
	
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getDestIp() {
		return destIp;
	}
	public void setDestIp(String destIp) {
		this.destIp = destIp;
	}
	public String getSendTime() {
		return sendTime;
	}
	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}
	public boolean isOwn() {
		return own;
	}
	public void setOwn(boolean own) {
		this.own = own;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getDeviceCode() {
		return deviceCode;
	}
	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}
	
}
