package com.chatchat.tool;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * �����û���ͷ��ͼƬ���� ��ߴ�ȡЧ��
 */
public class MemoryCache {
	private static MemoryCache instance;//����ģʽ

	// �����С 10M
	private final int CACHESIZE = 1024 * 1024 * 10;

	private LruCache<String, Bitmap> lruCache;//��bitmapλͼ���뻺����
	private List<String> keys;// ��¼keyֵ
	
	private MemoryCache(){
		keys=new ArrayList<String>();
		lruCache = new LruCache<String, Bitmap>(CACHESIZE){
			@Override
			protected int sizeOf(String key, Bitmap value) {
				//ÿ�е��ֽ��� ���� �ߣ��ó�һ��ͼƬ�Ĵ�С
				return value.getRowBytes()*value.getHeight();
			}
		};
	}
	
	public static MemoryCache getInstance(){
		return instance==null?instance=new MemoryCache():instance; 
	}
	
	public void put(String key,Bitmap bitmap){
		if(bitmap!=null){
			lruCache.put(key, bitmap);
			keys.add(key);
		}
	}
	
	public Bitmap get(String key){
		return lruCache.get(key);
	}
	
	public void remove(String key){
		lruCache.remove(key);
	}
	
	public void removeAll(){
		for(String k:keys){
			lruCache.remove(k);
		}
		keys.clear();
		Log.d("LocalMemoryCache", "���ͼƬ����");
	}
	
}
