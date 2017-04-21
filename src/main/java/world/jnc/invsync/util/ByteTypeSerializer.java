package world.jnc.invsync.util;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

public class ByteTypeSerializer implements TypeSerializer<Byte> {
	private static final TypeToken<Integer> integerToken = TypeToken.of(Integer.class);
	private static final TypeToken<Byte> byteToken = TypeToken.of(Byte.class);

	public static void register() {
		TypeSerializers.getDefaultSerializers().registerType(byteToken, new ByteTypeSerializer());
	}

	@Override
	public Byte deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
		if (!type.equals(byteToken))
			throw new ObjectMappingException();

		return (byte) value.getInt();
	}

	@Override
	public void serialize(TypeToken<?> type, Byte obj, ConfigurationNode value) throws ObjectMappingException {
		if (!type.equals(byteToken))
			throw new ObjectMappingException();

		value.setValue(integerToken, (int) obj);
	}
}
