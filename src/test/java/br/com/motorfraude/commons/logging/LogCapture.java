package br.com.motorfraude.commons.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Captura o que o appender JSON escreve no stdout e entrega cada linha já como JSON. */
public final class LogCapture {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private LogCapture() {}

    public static List<JsonNode> capture(Runnable logging) {
        var original = System.out;
        var buffer = new ByteArrayOutputStream();
        try (var capture = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            logging.run();
        } finally {
            System.setOut(original);
        }
        return Arrays.stream(buffer.toString(StandardCharsets.UTF_8).split("\\R"))
                .filter(line -> !line.isBlank())
                .map(MAPPER::readTree)
                .toList();
    }

    public static JsonNode captureOne(Runnable logging) {
        var lines = capture(logging);
        if (lines.size() != 1) {
            throw new AssertionError("esperava 1 linha de log JSON, vieram " + lines.size() + ": " + lines);
        }
        return lines.get(0);
    }
}
