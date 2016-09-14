package com.dexmatech.styx.modules.discovery;

import com.dexmatech.styx.core.http.HttpResponse;
import com.dexmatech.styx.core.pipeline.stages.StageResult;
import com.dexmatech.styx.core.pipeline.stages.request.RequestPipelineStage;
import com.dexmatech.styx.core.pipeline.stages.routing.DefaultRoutingStage;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.dexmatech.styx.core.pipeline.stages.AbortedStage.because;
import static com.dexmatech.styx.core.pipeline.stages.StageResult.completeStageFailingWith;
import static com.dexmatech.styx.core.http.utils.Uris.changeHost;
import static java.util.stream.Collectors.toList;

/**
 * Created by aortiz on 13/09/16.
 */
public class ServerSideDiscoveryStage {

	public static final String ABORTING_BLACKLIST_MATCH_MSG = "Aborting stage because path '%s' is in blacklist '%s'";

	public static Builder usingDefaults() {
		return new Builder();
	}

	public static class Builder {

		private String headerUsedToRoute = DefaultRoutingStage.DEFAULT_HEADER_USED_TO_ROUTE;
		private HttpResponse responseWhenBlacklistMatch = HttpResponse.notFound();
		private List<Pattern> regexPathBlackList = new ArrayList<>();
		private List<HostRoutingRule> hostRoutingRules = new ArrayList<>();
		private String defaultHostRouting = null;

		public Builder withBlackList(List<String> regexPathBlackList) {
			this.regexPathBlackList = regexPathBlackList.stream().map(Pattern::compile).collect(toList());
			return this;
		}

		public Builder whenBlackListMatchRespondWith(HttpResponse httpResponse) {
			this.responseWhenBlacklistMatch = httpResponse;
			return this;
		}

		public Builder changingResultingCustomRoutingHeaderTo(String header) {
			this.headerUsedToRoute = header;
			return this;
		}

		public Builder withDefaultHostRule(String defaultHost) {
			this.defaultHostRouting = defaultHost;
			return this;
		}

		public Builder withHostRoutingRule(String regexPath, String host) {
			this.hostRoutingRules.add(HostRoutingRule.from(regexPath, host));
			return this;
		}

		public RequestPipelineStage build() {
			Objects.requireNonNull(defaultHostRouting, "Please provide a default host routing rule");
			HostSelector hostSelector = HostSelector.from(hostRoutingRules, defaultHostRouting);
			return httpRequest -> {
				URI uri = httpRequest.getRequestLine().getUri();
				if (regexPathBlackList.stream().anyMatch(pattern -> pattern.matcher(uri.getPath()).matches())) {
					return completeStageFailingWith(
							responseWhenBlacklistMatch,
							because(String.format(ABORTING_BLACKLIST_MATCH_MSG, uri.getPath(), regexPathBlackList))
					);
				}
				String host = hostSelector.hostToRoute(httpRequest);
				String routeValue = changeHost(uri, host);
				return StageResult.completeStageSuccessfullyWith(httpRequest.addHeader(headerUsedToRoute, routeValue));
			};
		}


	}
}
