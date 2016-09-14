package com.dexmatech.styx.authentication;

/**
 * Created by aortiz on 13/09/16.
 */
public class AuthenticationStage {

	public static final String ABORTING_BLACKLIST_MATCH_MSG = "Aborting stage because path '%s' is in blacklist '%s'";

	//	public static Builder usingDefaults() {
	//		return new Builder();
	//	}

	//	public static class Builder {
	//
	//		private String headerUsedToRoute = DefaultRoutingStage.DEFAULT_HEADER_USED_TO_ROUTE;
	//
	//
	//
	//
	//		public Builder withHostRoutingRule(String regexPath, String host) {
	//			this.hostRoutingRules.add(HostRoutingRule.from(regexPath, host));
	//			return this;
	//		}
	//
	//		public RequestPipelineStage build() {
	//			Objects.requireNonNull(defaultHostRouting, "Please provide a default host routing rule");
	//
	//			return httpRequest -> {
	//				ult.completeStageSuccessfullyWith(httpRequest.addHeader(headerUsedToRoute, routeValue));
	//			};
	//		}
	//
	//	}
}
