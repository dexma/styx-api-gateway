package com.dexmatech.styx.core.utils;

import java.util.List;

/**
 * Created by aortiz on 11/08/16.
 */
public class ArrayUtils {

	public static<T> T head(List<T> list) {
		if(list.isEmpty()) {
			throw new IllegalArgumentException("Imposible to get head on empty list");
		} else {
			return list.iterator().next();
		}
	}

	public static<T> List<T> tail(List<T> list) {
		if(list.isEmpty()) {
			return list;
		} else {
			return list.subList(1,list.size());
		}
	}
}
