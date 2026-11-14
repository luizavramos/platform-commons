package br.com.motorfraude.commons.tracing;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KafkaTracePropagationTest {

    private static final TraceParent TP =
            TraceParent.parse("00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01").orElseThrow();

    @Test
    void injectEExtractFazemIdaEVolta() {
        var headers = new RecordHeaders();

        KafkaTracePropagation.inject(headers, TP);

        assertThat(KafkaTracePropagation.extract(headers)).contains(TP);
    }

    @Test
    @DisplayName("o header usa o nome padrão 'traceparent' e texto UTF-8")
    void nomeEFormatoDoHeader() {
        var headers = new RecordHeaders();

        KafkaTracePropagation.inject(headers, TP);

        var header = headers.lastHeader("traceparent");
        assertThat(header).isNotNull();
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo(TP.format());
        assertThat(StandardHeaders.TRACEPARENT).isEqualTo("traceparent");
    }

    @Test
    @DisplayName("inject substitui o header existente: nunca fica duplicado")
    void injectSubstitui() {
        var headers = new RecordHeaders();
        KafkaTracePropagation.inject(headers, TraceParent.generate());

        KafkaTracePropagation.inject(headers, TP);

        assertThat(headers.headers("traceparent")).hasSize(1);
        assertThat(KafkaTracePropagation.extract(headers)).contains(TP);
    }

    @Test
    void semHeaderNaoHaContexto() {
        assertThat(KafkaTracePropagation.extract(new RecordHeaders())).isEmpty();
    }

    @Test
    void headerMalformadoNaoHaContexto() {
        var headers = new RecordHeaders().add("traceparent", "lixo".getBytes(StandardCharsets.UTF_8));

        assertThat(KafkaTracePropagation.extract(headers)).isEmpty();
    }

    @Test
    void headerComValorNuloNaoHaContexto() {
        var headers = new RecordHeaders().add("traceparent", null);

        assertThat(KafkaTracePropagation.extract(headers)).isEmpty();
    }

    @Test
    @DisplayName("extractOrStart continua o trace recebido, ou começa um novo se não veio nenhum")
    void extractOrStart() {
        var comContexto = new RecordHeaders();
        KafkaTracePropagation.inject(comContexto, TP);

        assertThat(KafkaTracePropagation.extractOrStart(comContexto).traceId()).isEqualTo(TP.traceId());

        var novo = KafkaTracePropagation.extractOrStart(new RecordHeaders());
        assertThat(TraceParent.parse(novo.format())).contains(novo);
    }

    @Test
    @DisplayName("nomes dos headers padrão dos tópicos (plan 000 §3.2)")
    void nomesPadrao() {
        assertThat(StandardHeaders.RECEIVED_AT).isEqualTo("receivedAt");
        assertThat(StandardHeaders.SCHEMA_VERSION).isEqualTo("schemaVersion");
        assertThat(StandardHeaders.COMMAND_TYPE).isEqualTo("commandType");
    }
}
