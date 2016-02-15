package connection;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

/**
 * Utilities to manage ByteBuffers
 */
public class BufferUtils {

    /**
     * Convert ByteBuffer to String
     * @param buf ByteBuffer to convert
     * @return String
     * @throws CharacterCodingException
     */
	public static String bufferToString(ByteBuffer buf) throws CharacterCodingException{
		return new String(buf.array(), 0, buf.remaining());
	}
}
