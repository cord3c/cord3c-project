package io.cord3c.ssi.api.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;


public class IOUtils {

	public static final String LINE_SEPARATOR;

	static {
		// avoid security issues
		StringWriter buf = new StringWriter(4);
		PrintWriter out = new PrintWriter(buf);
		out.println();
		LINE_SEPARATOR = buf.toString();
	}

	/**
	 * The default buffer size to use.
	 */
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	/**
	 * Instances should NOT be constructed in standard programming.
	 */
	public IOUtils() {
		super();
	}

	/**
	 * Get the contents of an <code>InputStream</code> as a String using the default character encoding of the platform.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 *
	 * @param input the <code>InputStream</code> to read from
	 * @return the requested String
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs
	 */
	public static String toString(InputStream input) throws IOException {
		StringWriter sw = new StringWriter();
		copy(input, sw);
		return sw.toString();
	}

	/**
	 * Get the contents of an <code>InputStream</code> as a String using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 *
	 * @param input    the <code>InputStream</code> to read from
	 * @param encoding the encoding to use, null means platform default
	 * @return the requested String
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs
	 */
	public static String toString(InputStream input, String encoding)
			throws IOException {
		StringWriter sw = new StringWriter();
		copy(input, sw, encoding);
		return sw.toString();
	}

	/**
	 * Get the contents of a <code>Reader</code> as a String.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 *
	 * @param input the <code>Reader</code> to read from
	 * @return the requested String
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs
	 */
	public static String toString(Reader input) throws IOException {
		StringWriter sw = new StringWriter();
		copy(input, sw);
		return sw.toString();
	}

	/**
	 * Get the contents of a <code>byte[]</code> as a String using the default character encoding of the platform.
	 *
	 * @param input the byte array to read from
	 * @return the requested String
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs (never occurs)
	 * @deprecated Use {@link String#String(byte[])}
	 */
	public static String toString(byte[] input) throws IOException {
		return new String(input);
	}

	/**
	 * Get the contents of a <code>byte[]</code> as a String using the specified character encoding.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 *
	 * @param input    the byte array to read from
	 * @param encoding the encoding to use, null means platform default
	 * @return the requested String
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs (never occurs)
	 * @deprecated Use {@link String#String(byte[], String)}
	 */
	public static String toString(byte[] input, String encoding)
			throws IOException {
		if (encoding == null) {
			return new String(input);
		}
		else {
			return new String(input, encoding);
		}
	}

	// readLines
	//-----------------------------------------------------------------------

	public static void copy(InputStream input, Writer output)
			throws IOException {
		InputStreamReader in = new InputStreamReader(input);
		copy(in, output);
	}

	/**
	 * Copy bytes from an <code>InputStream</code> to chars on a
	 * <code>Writer</code> using the specified character encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedInputStream</code>.
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * <p>
	 * This method uses {@link InputStreamReader}.
	 *
	 * @param input    the <code>InputStream</code> to read from
	 * @param output   the <code>Writer</code> to write to
	 * @param encoding the encoding to use, null means platform default
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since Commons IO 1.1
	 */
	public static void copy(InputStream input, Writer output, String encoding)
			throws IOException {
		if (encoding == null) {
			copy(input, output);
		}
		else {
			InputStreamReader in = new InputStreamReader(input, encoding);
			copy(in, output);
		}
	}

	// copy from Reader
	//-----------------------------------------------------------------------

	/**
	 * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 * <p>
	 * Large streams (over 2GB) will return a chars copied value of
	 * <code>-1</code> after the copy has completed since the correct
	 * number of chars cannot be returned as an int. For large streams use the <code>copyLarge(Reader, Writer)</code> method.
	 *
	 * @param input  the <code>Reader</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @return the number of characters copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @throws ArithmeticException  if the character count is too large
	 * @since Commons IO 1.1
	 */
	public static int copy(Reader input, Writer output) throws IOException {
		long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	/**
	 * Copy chars from a large (over 2GB) <code>Reader</code> to a <code>Writer</code>.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * <code>BufferedReader</code>.
	 *
	 * @param input  the <code>Reader</code> to read from
	 * @param output the <code>Writer</code> to write to
	 * @return the number of characters copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since Commons IO 1.3
	 */
	public static long copyLarge(Reader input, Writer output) throws IOException {
		char[] buffer = new char[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
