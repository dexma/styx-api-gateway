package com.dexmatech.styx.testing;

import javax.net.ServerSocketFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Random;

/**
 * Created by aortiz on 8/09/16.
 */
public class SocketUtils {

	public static final int PORT_RANGE_MAX = 65535;
	public static final int PORT_RANGE_MIN = 1024;
	private static final Random random = new Random(System.currentTimeMillis());

	public static int findAvailableTcpPort(int minPort, int maxPort) {
		return findAvailablePort(minPort, maxPort);
	}

	public static int findRandomPort() {
		return findRandomPort(PORT_RANGE_MIN, PORT_RANGE_MAX);
	}

	private static int findRandomPort(int minPort, int maxPort) {
		int portRange = maxPort - minPort;
		return minPort + random.nextInt(portRange + 1);
	}

	public static int findAvailablePort(int minPort, int maxPort) {
		isTrue(minPort > 0, "'minPort' must be greater than 0");
		isTrue(maxPort >= minPort, "'maxPort' must be greater than or equals 'minPort'");
		isTrue(maxPort <= PORT_RANGE_MAX, "'maxPort' must be less than or equal to " + PORT_RANGE_MAX);

		int portRange = maxPort - minPort;
		int candidatePort;
		int searchCounter = 0;
		do {
			if (++searchCounter > portRange) {
				throw new IllegalStateException(String.format(
						"Could not find an available TCP port in the range [%d, %d] after %d attempts",
						minPort, maxPort, searchCounter));
			}
			candidatePort = findRandomPort(minPort, maxPort);
		}
		while (!isPortTCPAvailable(candidatePort));

		return candidatePort;
	}

	public static boolean isPortTCPAvailable(int port) {
		try {
			ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(
					port, 1, InetAddress.getByName("localhost"));
			serverSocket.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static void isTrue(boolean expression, String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}

}
