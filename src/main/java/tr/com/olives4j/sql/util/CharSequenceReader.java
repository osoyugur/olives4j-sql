/*******************************************************************************
 *   Copyright (c) 2016, Omer Dogan.  All rights reserved.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *    
 *******************************************************************************/
package tr.com.olives4j.sql.util;

import java.io.Reader;
import java.io.Serializable;

@SuppressWarnings("serial")
public class CharSequenceReader extends Reader implements Serializable {
	private static enum TYPE {
		StringBuilder, StringBuffer, String, CharSequence;
	};

	private final CharSequence charSequence;
	private int idx;
	private int mark;
	private final int length;
	private final TYPE type;

	/**
	 * Construct a new instance with the specified character sequence.
	 * 
	 * @param charSequence
	 *            The character sequence, may be <code>null</code>
	 */
	public CharSequenceReader(CharSequence charSequence) {
		this.charSequence = (charSequence != null ? charSequence : "");

		if (charSequence instanceof StringBuilder) {
			this.type = TYPE.StringBuilder;
		} else if (charSequence instanceof StringBuffer) {
			this.type = TYPE.StringBuffer;
		} else if (charSequence instanceof String) {
			this.type = TYPE.String;
		} else {
			this.type = TYPE.CharSequence;
		}
		length = charSequence.length();
	}

	/**
	 * Close resets the file back to the start and removes any marked position.
	 */
	public void close() {
		idx = 0;
		mark = 0;
	}

	/**
	 * Mark the current position.
	 * 
	 * @param readAheadLimit
	 *            ignored
	 */
	public void mark(int readAheadLimit) {
		mark = idx;
	}

	/**
	 * Mark is supported (returns true).
	 * 
	 * @return <code>true</code>
	 */
	public boolean markSupported() {
		return true;
	}

	/**
	 * Read a single character.
	 * 
	 * @return the next character from the character sequence or -1 if the end has been reached.
	 */
	public int read() {
		if (idx >= length) {
			return -1;
		} else {
			return charSequence.charAt(idx++);
		}
	}

	/**
	 * Read the sepcified number of characters into the array.
	 * 
	 * @param array
	 *            The array to store the characters in
	 * @param offset
	 *            The starting position in the array to store
	 * @param length
	 *            The maximum number of characters to read
	 * @return The number of characters read or -1 if there are no more
	 */
	public int read(char[] array, int offset, int length) {
		if (idx >= this.length) {
			return -1;
		}
		if (array == null) {
			throw new NullPointerException("Character array is missing");
		}
		if (length < 0 || (offset + length) > array.length) {
			throw new IndexOutOfBoundsException("Array Size=" + array.length + ", offset=" + offset + ", length="
					+ length);
		}

		switch (type) {
		case String:
			return read((String) charSequence, array, offset, length);
		case StringBuffer:
			return read((StringBuffer) charSequence, array, offset, length);
		case StringBuilder:
			return read((StringBuilder) charSequence, array, offset, length);
		default:
			return read(charSequence, array, offset, length);
		}
	}

	/**
	 * Read the sepcified number of characters into the array.
	 * 
	 * @param array
	 *            The array to store the characters in
	 * @param offset
	 *            The starting position in the array to store
	 * @param length
	 *            The maximum number of characters to read
	 * @return The number of characters read or -1 if there are no more
	 */
	private int read(CharSequence buffer, char[] array, int offset, int length) {
		int count = 0;
		for (int i = 0; i < length; i++) {
			int c = read();
			if (c == -1) {
				return count;
			}
			array[offset + i] = (char) c;
			count++;
		}
		return count;
	}

	/**
	 * Read the sepcified number of characters into the array.
	 * 
	 * @param array
	 *            The array to store the characters in
	 * @param offset
	 *            The starting position in the array to store
	 * @param length
	 *            The maximum number of characters to read
	 * @return The number of characters read or -1 if there are no more
	 */
	private int read(StringBuffer buffer, char[] array, int offset, int length) {
		int end = idx + length;
		if (end > buffer.length()) {
			end = buffer.length();
		}
		buffer.getChars(idx, end, array, offset);
		int readCount = end - idx;
		idx = end;
		return readCount;
	}

	/**
	 * Read the sepcified number of characters into the array.
	 * 
	 * @param array
	 *            The array to store the characters in
	 * @param offset
	 *            The starting position in the array to store
	 * @param length
	 *            The maximum number of characters to read
	 * @return The number of characters read or -1 if there are no more
	 */
	private int read(StringBuilder buffer, char[] array, int offset, int length) {
		int end = idx + length;
		if (end > buffer.length()) {
			end = buffer.length();
		}
		buffer.getChars(idx, end, array, offset);
		int readCount = end - idx;
		idx = end;
		return readCount;
	}

	/**
	 * Read the sepcified number of characters into the array.
	 * 
	 * @param array
	 *            The array to store the characters in
	 * @param offset
	 *            The starting position in the array to store
	 * @param length
	 *            The maximum number of characters to read
	 * @return The number of characters read or -1 if there are no more
	 */
	private int read(String buffer, char[] array, int offset, int length) {
		int end = idx + length;
		if (end > buffer.length()) {
			end = buffer.length();
		}
		buffer.getChars(idx, end, array, offset);
		int readCount = end - idx;
		idx = end;
		return readCount;
	}

	/**
	 * Reset the reader to the last marked position (or the beginning if mark has not been called).
	 */
	public void reset() {
		idx = mark;
	}

	/**
	 * Skip the specified number of characters.
	 * 
	 * @param n
	 *            The number of characters to skip
	 * @return The actual number of characters skipped
	 */
	public long skip(long n) {
		if (n < 0) {
			throw new IllegalArgumentException("Number of characters to skip is less than zero: " + n);
		}
		if (idx >= charSequence.length()) {
			return -1;
		}
		int dest = (int) Math.min(charSequence.length(), (idx + n));
		int count = dest - idx;
		idx = dest;
		return count;
	}

	/**
	 * Return a String representation of the underlying character sequence.
	 * 
	 * @return The contents of the character sequence
	 */
	public String toString() {
		return charSequence.toString();
	}

}