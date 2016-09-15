package com.dexmatech.styx.authentication;

import com.dexmatech.styx.core.http.Headers;
import com.dexmatech.styx.core.http.HttpResponse;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Created by aortiz on 15/09/16.
 */
public class AuthenticationStageDefaults {
	public static final String CUSTOM_HEADER_PREFIX = "X-security-";
	public static final String DEFAULT_PERMISSIONS_HEADER_KEY = "X-security-permissions";
	public static final String PERMISSIONS_HEADER_VALUE_DELIMITER = ",";
	public static final HttpResponse DEFAULT_AUTHENTICATION_FAIL_RESPONSE = HttpResponse.forbidden();

	public static final String EMPTY = "";
	public static final Function<String, Function<List<Permission>, Headers>> PARSE_PERMISSIONS_TO_ONE_HEADER = header -> permissions
			-> {
		String value = permissions.stream().map(Permission::toString).collect(Collectors.joining(PERMISSIONS_HEADER_VALUE_DELIMITER));
		if (EMPTY.equals(value)) {
			return Headers.empty();
		} else {
			return Headers.from(header, value);
		}
	};
	public static final Function<MetaInfo, Headers> PARSE_METAINFO_TO_HEADERS = metaInfo ->
			Headers.from(metaInfo.content().stream().collect(toMap(k -> CUSTOM_HEADER_PREFIX + k.getKey(), Map.Entry::getValue)));
}
