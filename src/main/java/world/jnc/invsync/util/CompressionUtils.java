package world.jnc.invsync.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.slf4j.Logger;

import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import world.jnc.invsync.InventorySync;

@UtilityClass
public class CompressionUtils {
	private static final Logger logger = InventorySync.getLogger();
	private static final int MAX_COMPRESSION_LEVEL = 9;
	private static final String MSG_PREFIX = "[Compression] ";
	private static final String MSG_ORIGINAL = MSG_PREFIX + "Original: ";
	private static final String MSG_COMPRESSED = MSG_PREFIX + "Compressed: ";
	private static final String MSG_DECOMPRESSED = MSG_PREFIX + "Decompressed: ";

	public static byte[] compress(byte[] data) throws IOException {
		Deflater deflater = new Deflater();
		@Cleanup
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte[1024];

		deflater.setLevel(MAX_COMPRESSION_LEVEL);
		deflater.setInput(data);
		deflater.finish();

		while (!deflater.finished()) {
			// returns the generated code... index
			int count = deflater.deflate(buffer);

			outputStream.write(buffer, 0, count);
		}

		outputStream.close();
		byte[] output = outputStream.toByteArray();

		logger.trace(MSG_ORIGINAL + data.length);
		logger.trace(MSG_COMPRESSED + output.length);

		return output;
	}

	public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
		Inflater inflater = new Inflater();
		@Cleanup
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte[1024];

		inflater.setInput(data);
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);

			outputStream.write(buffer, 0, count);
		}

		outputStream.close();
		byte[] output = outputStream.toByteArray();

		logger.trace(MSG_ORIGINAL + data.length);
		logger.trace(MSG_DECOMPRESSED + output.length);

		return output;
	}
}
