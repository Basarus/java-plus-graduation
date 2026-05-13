package ru.practicum.ewm.stats.avro;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;

public class UserActionAvroDeserializer implements Deserializer<UserActionAvro> {

    private final SpecificDatumReader<UserActionAvro> reader =
            new SpecificDatumReader<>(UserActionAvro.class);

    @Override
    public UserActionAvro deserialize(String topic, byte[] data) {
        if (data == null) return null;
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(in, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize UserActionAvro", e);
        }
    }
}
