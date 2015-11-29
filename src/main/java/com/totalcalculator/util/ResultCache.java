package com.totalcalculator.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ResultCache {
	INSTANCE;
	
	private Map<Integer, Integer> cache = new ConcurrentHashMap<>();
	
	public Integer get(Integer key) {
		return cache.get(key);
	}
	
	public void put(Integer key, Integer value) {
		if (key == null || value == null) {
			return;
		}
		
		cache.put(key, value);
	}
	
	public Map<Integer, Integer> getAll() {
		return cache;
	}
}
