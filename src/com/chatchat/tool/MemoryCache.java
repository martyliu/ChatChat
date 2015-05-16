package com.chatchat.tool;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * 用于用户的头像图片缓存 提高存取效率
 */
public class MemoryCache {
	private static MemoryCache instance;//单例模式

	// 缓存大小 10M
	private final int CACHESIZE = 1024 * 1024 * 10;

	private LruCache<String, Bitmap> lruCache;//把bitmap位图放入缓存中
	private List<String> keys;// 记录key值
	
	private MemoryCache(){
		keys=new ArrayList<String>();
		lruCache = new LruCache<String, Bitmap>(CACHESIZE){
			@Override
			protected int sizeOf(String key, Bitmap value) {
				//每行的字节数 乘以 高，得出一张图片的大小
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
		Log.d("LocalMemoryCache", "清除图片缓存");
	}
	
}
