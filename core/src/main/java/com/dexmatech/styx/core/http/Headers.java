package com.dexmatech.styx.core.http;


import lombok.AccessLevel;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by aortiz on 9/08/16.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class Headers {

	private final Map<String,String> map;


	public static Headers empty() {
		return new Headers(new HashMap<>());
	}
	public static Headers from(String key, String value) {
		return Headers.empty().put(key,value);
	}

	public static Headers from(Map<String,String> map) {
		HashMap<String, String> headers = new HashMap<>();
		headers.putAll(map);
		return new Headers(Collections.unmodifiableMap(headers));
	}

	public Headers merge(Headers headers) {
		Map<String,String> map = new HashMap<>();
		map.putAll(this.map);
		map.putAll(headers.toMap());
		return Headers.from(map);
	}

	public Headers compute(String key, BiFunction<String,String,String> ifKeyPresent, Function<String,String> ifKeyAbsent) {
		HashMap<String, String> headers = new HashMap<>();
		headers.putAll(map);
		headers.computeIfPresent(key,ifKeyPresent);
		headers.computeIfAbsent(key,ifKeyAbsent);
		return new Headers(Collections.unmodifiableMap(headers));
	}

	public boolean contains(String key) {
		return map.containsKey(key);
	}

	public Headers put(String key, String value) {
		HashMap<String, String> headers = new HashMap<>();
		headers.putAll(map);
		headers.put(key,value);
		return new Headers(Collections.unmodifiableMap(headers));
	}


	public Headers remove(String key) {
		HashMap<String, String> headers = new HashMap<>();
		headers.remove(key);
		return new Headers(Collections.unmodifiableMap(headers));
	}

	public String get(String key) {
		return map.get(key);
	}

	public Map<String,String> toMap() {
		return map;
	}

	public void ifNotEmpty(Consumer<Headers> consumer) {
		consumer.accept(this);
	}

}
