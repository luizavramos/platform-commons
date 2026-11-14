package br.com.motorfraude.commons.tracing;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

/** Leva o {@code traceparent} para dentro e para fora dos headers de uma mensagem Kafka. */
public final class KafkaTracePropagation {

    private KafkaTracePropagation() {}

    /** Grava o header, substituindo o que já existir (nunca fica duplicado). */
    public static void inject(Headers headers, TraceParent traceParent) {
        headers.remove(StandardHeaders.TRACEPARENT);
        headers.add(StandardHeaders.TRACEPARENT, traceParent.format().getBytes(StandardCharsets.UTF_8));
    }

    /** Lê o header; vazio se ausente, nulo ou malformado. */
    public static Optional<TraceParent> extract(Headers headers) {
        Header header = headers.lastHeader(StandardHeaders.TRACEPARENT);
        if (header == null || header.value() == null) {
            return Optional.empty();
        }
        return TraceParent.parse(new String(header.value(), StandardCharsets.UTF_8));
    }

    /**
     * O contexto para o trabalho deste serviço: um span novo dentro do trace recebido ou, se a mensagem não trouxe
     * contexto válido, um trace novo.
     */
    public static TraceParent extractOrStart(Headers headers) {
        return extract(headers).map(TraceParent::child).orElseGet(TraceParent::generate);
    }
}
