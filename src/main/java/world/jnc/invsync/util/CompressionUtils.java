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

	public static byte[] compress(byte[] data) throws IOException {
		Deflater deflater = new Deflater();
		@Cleanup
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		byte[] buffer = new byte[1024];

		deflater.setInput(data);
		deflater.finish();

		while (!deflater.finished()) {
			// returns the generated code... index
			int count = deflater.deflate(buffer);

			outputStream.write(buffer, 0, count);
		}

		outputStream.close();
		byte[] output = outputStream.toByteArray();

		logger.debug("Original: " + data.length / 1024 + " Kb");
		logger.debug("Compressed: " + output.length / 1024 + " Kb");

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

		logger.debug("Original: " + data.length);
		logger.debug("Decompressed: " + output.length);

		return output;
	}
}
