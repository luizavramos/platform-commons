package br.com.motorfraude.commons.tracing;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

public final class KafkaTracePropagation {

    private KafkaTracePropagation() {}

    public static void inject(Headers headers, TraceParent traceParent) {
        headers.remove(StandardHeaders.TRACEPARENT);
        headers.add(StandardHeaders.TRACEPARENT, traceParent.format().getBytes(StandardCharsets.UTF_8));
    }

    public static Optional<TraceParent> extract(Headers headers) {
        Header header = headers.lastHeader(StandardHeaders.TRACEPARENT);
        if (header == null || header.value() == null) {
            return Optional.empty();
        }
        return TraceParent.parse(new String(header.value(), StandardCharsets.UTF_8));
    }

    public static TraceParent extractOrStart(Headers headers) {
        return extract(headers).map(TraceParent::child).orElseGet(TraceParent::generate);
    }
}
