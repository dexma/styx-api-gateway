package com.dexmatech.styx.core.http.utils;

import com.dexmatech.styx.core.http.QueryParam;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by aortiz on 14/09/16.
 */
public class Uris {

	private static Function<Map.Entry<String, List<String>>, Stream<? extends QueryParam>> ENTRY_AS_QUERY_PARAM_STREAM =
			(entry) -> entry.getValue().stream().map(value -> new QueryParam(entry.getKey(), value));

	// TODO : add user:password
	// scheme:[//[user:password@]host[:port]][/]path[?query][#fragment]
	public static String changeHost(URI uri, String host) {
		String hostAndPort;
		String[] splitByPortSeparator = host.split(":");
		boolean isPortIncluded = splitByPortSeparator.length == 2;
		if(isPortIncluded) {
			hostAndPort = host;
		} else {
			String port = Optional.ofNullable(uri.getPort()).filter(p -> p != -1).map(p -> ":" + p).orElseGet(() -> "");
			hostAndPort = host + port;
		}

		String scheme = Optional.ofNullable(uri.getScheme()).map(s -> s + "://").orElseGet(() -> "http://");

		QueryStringEncoder encoder = new QueryStringEncoder(scheme + hostAndPort + uri.getPath());
		extractQueryParams(uri).forEach(queryParam -> {
			encoder.addParam(queryParam.getName(), queryParam.getValue());
		});
		return encoder.toString();
	}

	public static List<QueryParam> extractQueryParams(URI uri) {
		QueryStringDecoder decoder = new QueryStringDecoder(uri);
		return decoder.parameters().entrySet()
				.stream()
				.flatMap(ENTRY_AS_QUERY_PARAM_STREAM)
				.collect(Collectors.toList());
	}


	public static URI create(String uri) {

		try {
			return URI.create(uri).parseServerAuthority();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}

	}
}
