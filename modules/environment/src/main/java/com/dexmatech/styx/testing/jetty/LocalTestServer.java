package com.dexmatech.styx.testing.jetty;

/**
 * Created by aortiz on 8/09/16.
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

import com.dexmatech.styx.testing.SocketUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.handler.ContextHandler;

import java.util.HashMap;
import java.util.Optional;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalTestServer {

	private Server server;
	@Getter
	private int runningPort;

	public static Builder setUpLocalServer() {
		return new Builder();
	}

	public static class Builder {

		private Optional<Integer> port = Optional.empty();
		private Optional<Integer> delayInMillis = Optional.empty();
		private String responseContentType = "text/html;charset=utf-8";
		private int responseStatus = 200;
		private byte[] responseBody = new byte[0];
		private Map<String,String> responseHeaders = new HashMap<>();
		private Optional<String> virtualHost = Optional.empty();
		private Optional<BiConsumer<HttpServletRequest, HttpServletResponse>> responseHandler;

		public Builder onPort(int port) {
			this.port = Optional.of(port);
			return this;
		}

		public Builder applyingDelayOnResponse(int delayInMillis) {
			this.delayInMillis = Optional.of(delayInMillis);
			return this;
		}

		public Builder respondingWith(int status) {
			this.responseStatus = status;
			return this;
		}

		public Builder withVirtualHost(String virtualHost) {
			this.virtualHost = Optional.of(virtualHost);
			return this;
		}

		public Builder respondingWith(int status, Map<String, String> headers) {
			this.responseStatus = status;
			this.responseHeaders = headers;
			return this;
		}

		public Builder respondingWith(int status, Map<String, String> headers, String contentType, byte[] body) {
			this.responseStatus = status;
			this.responseHeaders = headers;
			this.responseBody = body;
			this.responseContentType = contentType;
			return this;
		}

		public Builder respondingWith(Map<String, String> headers, String contentType, byte[] body) {
			this.responseHeaders = headers;
			this.responseBody = body;
			this.responseContentType = contentType;
			return this;
		}

		public Builder handlingResponseWith(BiConsumer<HttpServletRequest, HttpServletResponse> handler) {
			this.responseHandler = Optional.of(handler);
			return this;
		}

		public Builder respondingWith( String contentType, byte[] body) {
			this.responseBody = body;
			this.responseContentType = contentType;
			return this;
		}


		public LocalTestServer build() {
			Integer port = this.port.orElseGet(SocketUtils::findRandomPort);
			Server server = new Server(port);
			ContextHandler contextHandler = new ContextHandler("/");
			virtualHost.ifPresent(v-> contextHandler.setVirtualHosts(new String[]{v}));

			AbstractHandler handler = new AbstractHandler() {
				@Override public void handle(String target, Request request, HttpServletRequest httpServletRequest,
						HttpServletResponse httpServletResponse)
						throws IOException, ServletException {

					if(responseHandler.isPresent()) {
							responseHandler.get().accept(httpServletRequest, httpServletResponse);
							request.setHandled(true);

					} else {
						httpServletResponse.setStatus(responseStatus);
						responseHeaders.entrySet().forEach(e -> httpServletResponse.addHeader(e.getKey(), e.getValue()));
						delayInMillis.ifPresent(integer -> {
							try {
								Thread.sleep(integer);
							} catch (InterruptedException e) {
								log.error("Impossible apply delay", e);
							}
						});
						request.setHandled(true);
						httpServletResponse.setContentType(responseContentType);
						httpServletResponse.getOutputStream().write(responseBody);
					}
					//					httpServletResponse.getWriter().println("<h1>Hello World</h1>");

				}
			};
			contextHandler.setHandler(handler);
			server.setHandler(contextHandler);
			return new LocalTestServer(server,port);
		}

	}


	@FunctionalInterface
	public interface TestableCode {
		void test() throws Exception;
	}

	public void runAndKill(TestableCode code) throws Exception {
		try {
			server.start();
			log.info("Local HTTP jetty server started successfully at port " + runningPort);
//			server.join();
			code.test();

		} catch (Exception e) {
			log.error("Local jetty server crash because ->",e);
			throw e;
		} finally {
			if (server != null)
					server.stop();

			log.info("Local HTTP jetty server stopped" );
		}
	}
}
