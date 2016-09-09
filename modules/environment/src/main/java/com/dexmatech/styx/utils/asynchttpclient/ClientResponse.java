package com.dexmatech.styx.utils.asynchttpclient;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.Cookie;
import org.asynchttpclient.uri.Uri;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Created by aortiz on 7/09/16.
 */
public class ClientResponse {

	public static final UnsupportedOperationException UNSUPPORTED_OPERATION_EXCEPTION = new UnsupportedOperationException(
			"Please add code to this test class");
	public static final String CONTENT_TYPE_HEADER = "Content-Type";
	public static final String DEFAULT_CONTENT_TYPE = "Content-type: text/plain; charset=us-ascii";


	public static Builder fake() {
		return new Builder();
	}

	public static class Builder {

		private static URI DEFAULT_URI;

		// this is test code
		static {
			try {
				DEFAULT_URI = new URI("http://localhost:8080/");
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		private Uri uri = new Uri(DEFAULT_URI.getScheme(), DEFAULT_URI.getUserInfo(), DEFAULT_URI.getHost(), DEFAULT_URI.getPort(),
				DEFAULT_URI.getPath(), DEFAULT_URI.getQuery());

		private int statusCode = 200;
		private byte[] bodyAsBytes = new byte[0];
		private java.util.Map<String, String> headers = new HashMap<>();

		public Builder withUri(Uri uri) {
			this.uri = uri;
			return this;
		}

		public Builder withStatusCode(int statusCode) {
			this.statusCode = statusCode;
			return this;
		}

		public Builder withBody(byte[] bodyAsBytes) {
			this.bodyAsBytes = bodyAsBytes;
			return this;
		}

		public Builder withHeaders(Map<String, String> headers) {
			this.headers = headers;
			return this;
		}

		public ListenableFuture<Response> buildAsFuture() {
			return new ListenableFuture<Response>() {
				@Override public void done() {

				}

				@Override public void abort(Throwable throwable) {

				}

				@Override public void touch() {

				}

				@Override public ListenableFuture<Response> addListener(Runnable runnable, Executor executor) {
					return null;
				}

				@Override public CompletableFuture<Response> toCompletableFuture() {
					return CompletableFuture.completedFuture(build());
				}

				@Override public boolean cancel(boolean mayInterruptIfRunning) {
					return false;
				}

				@Override public boolean isCancelled() {
					return false;
				}

				@Override public boolean isDone() {
					return false;
				}

				@Override public Response get() throws InterruptedException, ExecutionException {
					return build();
				}

				@Override public Response get(long timeout, TimeUnit unit)
						throws InterruptedException, ExecutionException, TimeoutException {
					return build();
				}
			};
		}

		public Response build() {
			return new Response() {
				@Override public int getStatusCode() {
					return statusCode;
				}

				@Override public String getStatusText() {
					return null;
				}

				@Override public byte[] getResponseBodyAsBytes() {
					return bodyAsBytes;
				}

				@Override public ByteBuffer getResponseBodyAsByteBuffer() {
					throw UNSUPPORTED_OPERATION_EXCEPTION;
				}

				@Override public InputStream getResponseBodyAsStream() {
					return new ByteArrayInputStream(bodyAsBytes);
				}

				@Override public String getResponseBody(Charset charset) {
					return new String(bodyAsBytes, charset);
				}

				@Override public String getResponseBody() {
					return new String(bodyAsBytes, Charset.defaultCharset());
				}

				@Override public Uri getUri() {
					return uri;
				}

				@Override public String getContentType() {
					return Optional.ofNullable(headers.get(CONTENT_TYPE_HEADER)).orElse(DEFAULT_CONTENT_TYPE);
				}

				@Override public String getHeader(String key) {
					return headers.get(key);
				}

				@Override public List<String> getHeaders(String s) {
					throw UNSUPPORTED_OPERATION_EXCEPTION;
				}

				@Override public HttpHeaders getHeaders() {
					HttpHeaders clientHeaders = new DefaultHttpHeaders();
					headers.entrySet().forEach(e-> clientHeaders.add(e.getKey(),e.getValue()));
					return clientHeaders;
				}

				@Override public boolean isRedirected() {
					throw UNSUPPORTED_OPERATION_EXCEPTION;
				}

				@Override public List<Cookie> getCookies() {
					throw UNSUPPORTED_OPERATION_EXCEPTION;
				}

				@Override public boolean hasResponseStatus() {
					throw UNSUPPORTED_OPERATION_EXCEPTION;
				}

				@Override public boolean hasResponseHeaders() {
					throw UNSUPPORTED_OPERATION_EXCEPTION;
				}

				@Override public boolean hasResponseBody() {
					return bodyAsBytes.length > 0;
				}

				@Override public SocketAddress getRemoteAddress() {
					throw UNSUPPORTED_OPERATION_EXCEPTION;
				}

				@Override public SocketAddress getLocalAddress() {
					throw UNSUPPORTED_OPERATION_EXCEPTION;
				}
			};
		}
	}

}
