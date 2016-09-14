package com.dexmatech.styx.modules.discovery;

import com.dexmatech.styx.core.http.HttpRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aortiz on 2/09/16.
 */
@Slf4j
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HostSelector {

	private final List<HostRoutingRule> hostRoutingRules;

	public static HostSelector from(List<HostRoutingRule> hostRoutingRules, String defaultHost) {

		List<HostRoutingRule> rules = new ArrayList<>(hostRoutingRules);
		rules.add(HostRoutingRule.defaultHostRule(defaultHost));

		if (log.isDebugEnabled()) {

			log.debug("Host selector initialized with => '{}'", rules);
		}

		return new HostSelector(rules);
	}

	public String hostToRoute(HttpRequest request) {

		String host = hostRoutingRules.stream()
				.peek(r -> log.debug("Checking if host routing rule '{}' matches with ", r, request)) // here put
				.filter(rule -> rule.match(request.getRequestLine().getUri().getPath()))
				.findFirst()
				.map(HostRoutingRule::getHost)
				.orElseThrow(() -> new IllegalStateException(
								String.format("Impossible to select one host, there is an setup error", request)
						)
				);
		log.debug("Host '{}' is selected", host);
		return host;
	}
}
