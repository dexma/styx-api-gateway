package com.dexmatech.styx.authentication;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by gszeliga on 22/12/14.
 */
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetaInfo {

	public static String VALID_KEY_CHARACTERS = "^\\p{ASCII}*$";
	private static final Pattern KEY_PATTERN = Pattern.compile(VALID_KEY_CHARACTERS);

	private final Map<String, String> content = new HashMap<>();

	public static MetaInfo empty() {
		return new MetaInfo();
	}

	public static MetaInfo initWith(String key, String value) {
		return new MetaInfo().put(key, value);
	}

	public MetaInfo put(String key, String value) {
		if (KEY_PATTERN.matcher(key).matches()) {
			content.putIfAbsent(key, value);
		} else {
			throw new IllegalArgumentException(
					String.format("Invalid key '%s' Meta key only accepts ASCII values", key)
			);
		}
		return this;
	}

	public Optional<Object> get(String key) {

		return Optional.ofNullable(content.get(key));
	}

	public Collection<Map.Entry<String, String>> content() {
		return content.entrySet();
	}

}
