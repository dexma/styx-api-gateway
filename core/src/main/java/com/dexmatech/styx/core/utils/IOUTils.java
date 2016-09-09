package com.dexmatech.styx.core.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Created by aortiz on 9/08/16.
 */
public class IOUTils {

	public static void fastCopy(final InputStream src, final OutputStream dest) throws IOException {
		final ReadableByteChannel inputChannel = Channels.newChannel(src);
		final WritableByteChannel outputChannel = Channels.newChannel(dest);
		fastCopy(inputChannel, outputChannel);
	}

	public static void fastCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);

		while(src.read(buffer) != -1) {
			buffer.flip();
			dest.write(buffer);
			buffer.compact();
		}

		buffer.flip();

		while(buffer.hasRemaining()) {
			dest.write(buffer);
		}
	}

	public static String toString(final InputStream inputStream)  {
		Scanner s = new Scanner(inputStream).useDelimiter("\\A");
		String result = s.hasNext() ? s.next() : "";
		return result;
	}

	public static String toString(final InputStream inputStream, String charset) throws IOException {
		final int bufferSize = 1024;
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(inputStream, charset);
		for (; ; ) {
			int rsz = in.read(buffer, 0, buffer.length);
			if (rsz < 0)
				break;
			out.append(buffer, 0, rsz);
		}
		return out.toString();
	}
}
