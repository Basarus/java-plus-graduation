package ru.practicum.ewm.stats.avro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;

public class UserActionAvroSerializer implements Serializer<UserActionAvro> {

    private final SpecificDatumWriter<UserActionAvro> writer =
            new SpecificDatumWriter<>(UserActionAvro.class);

    @Override
    public byte[] serialize(String topic, UserActionAvro data) {
        if (data == null) return null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize UserActionAvro", e);
        }
    }
}
