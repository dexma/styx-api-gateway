package com.dexmatech.styx.modules.discovery;

import com.dexmatech.styx.core.http.utils.HostValidators;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.regex.Pattern;

/**
 * Created by aortiz on 13/09/16.
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HostRoutingRule {
	public static final Pattern REGEX_TO_MATCH_ANY_PATH = Pattern.compile(".*");
	private Pattern regexUriPath;
	private String host;


	public static HostRoutingRule defaultHostRule(String toHost) {
		if (!HostValidators.HOST_WITH_OPTIONAL_PORT_VALIDATOR.test(toHost)) {
			throw new IllegalArgumentException("Impossible create a host routing rule , host provided is malformed => " + toHost);
		}
		return new HostRoutingRule(REGEX_TO_MATCH_ANY_PATH, toHost);
	}

	public static HostRoutingRule from(String regexUriPath, String toHost) {
		if (!HostValidators.HOST_WITH_OPTIONAL_PORT_VALIDATOR.test(toHost)) {
			throw new IllegalArgumentException("Impossible create a host routing rule , host provided is malformed => " + toHost);
		}
		return new HostRoutingRule(Pattern.compile(regexUriPath), toHost);
	}

	public boolean match(String path) {
		return regexUriPath.matcher(path).matches();

	}
}
