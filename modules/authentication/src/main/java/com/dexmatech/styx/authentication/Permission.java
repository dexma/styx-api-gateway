package com.dexmatech.styx.authentication;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Created by gszeliga on 22/12/14.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@Slf4j
public class Permission {

	public static String VALID_CHARACTERS = "^[a-zA-Z0-9_-]*$";
	public static String TARGET_ACTION_JOIN_CHAR = ":";
	private static final Pattern PERMISSION_PATTERN = Pattern.compile(VALID_CHARACTERS);

	private static final Map<String, Permission> cache = new HashMap<>();

	private String target;
	private String action;

	public static Permission of(String target, String action) {
		String key = target + action;
		if (cache.containsKey(key)) {
			return cache.get(key);
		} else if (PERMISSION_PATTERN.matcher(key).matches()) {
			Permission value = new Permission(target, action);
			cache.putIfAbsent(key, value);
			return value;
		} else {
			throw new IllegalArgumentException(
					String.format(
							"Permission(target='%s' ,action='%s') contains invalid characters [valid regex '%s']", target,
							action, VALID_CHARACTERS
					)
			);
		}
	}

	public String toString() {
		return target + TARGET_ACTION_JOIN_CHAR + action;
	}

	public String toString(Function<Permission, String> f) {
		return f.apply(this);
	}

}
