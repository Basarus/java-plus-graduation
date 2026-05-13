package ru.practicum.ewm.stats.avro;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;

public class EventSimilarityAvroDeserializer implements Deserializer<EventSimilarityAvro> {

    private final SpecificDatumReader<EventSimilarityAvro> reader =
            new SpecificDatumReader<>(EventSimilarityAvro.class);

    @Override
    public EventSimilarityAvro deserialize(String topic, byte[] data) {
        if (data == null) return null;
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(in, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize EventSimilarityAvro", e);
        }
    }
}
