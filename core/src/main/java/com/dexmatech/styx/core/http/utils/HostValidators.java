package com.dexmatech.styx.core.http.utils;

import sun.net.util.IPAddressUtil;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Created by aortiz on 5/09/16.
 */
public class HostValidators {

	private static Pattern VALID_HOST_NAME_PATTERN = Pattern.compile(
			"^(([a-z]|[a-z][a-z0-9-]*[a-z0-9]).)*([a-z]|[a-z][a-z0-9-]*[a-z0-9])$", Pattern.CASE_INSENSITIVE
	);

	public static Predicate<String> HOST_VALIDATOR = host -> {
		if (host.contains("/")) {
			return false;
		}
		boolean matchIPV4 = IPAddressUtil.isIPv4LiteralAddress(host);
		boolean matchIPV6 = IPAddressUtil.isIPv6LiteralAddress(host);
		boolean matchHost = VALID_HOST_NAME_PATTERN.matcher(host).matches();

		return matchIPV4 || matchHost || matchIPV6;
	};

}
