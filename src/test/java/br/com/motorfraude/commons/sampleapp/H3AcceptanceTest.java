package br.com.motorfraude.commons.sampleapp;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.motorfraude.commons.logging.LogCapture;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.JsonNode;

/**
 * T020a — aceite da H3 (spec 000): um serviço Spring que usa o {@code logback-base.xml} do commons expõe
 * métricas, loga em JSON mascarado e carrega o traceId recebido em {@code traceparent}.
 *
 * <p>O app de exemplo existe só em src/test; o commons em si não depende de Spring.
 */
@SpringBootTest(
        classes = SampleApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "management.endpoints.web.exposure.include=health,prometheus",
            "management.endpoint.health.probes.enabled=true",
            "management.tracing.sampling.probability=1.0",
            "management.opentelemetry.tracing.export.otlp.enabled=false",
            "management.otlp.metrics.export.enabled=false",
            "spring.main.banner-mode=off"
        })
class H3AcceptanceTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @Value("${local.server.port}")
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path, String... headers) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET();
        for (int i = 0; i < headers.length; i += 2) {
            request.header(headers[i], headers[i + 1]);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonNode sampleLogLine(String... headers) {
        var lines = LogCapture.capture(() -> {
            try {
                assertThat(get("/sample/ping", headers).statusCode()).isEqualTo(200);
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });
        return lines.stream()
                .filter(l -> l.has("message") && l.get("message").asString().startsWith("CPF"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("log do controller não encontrado em " + lines));
    }

    @Test
    @DisplayName("H3.1: log com CPF sai em JSON com 'CPF ***.***.***-**'")
    void logMascarado() {
        var line = sampleLogLine();

        assertThat(line.get("message").asString()).isEqualTo("CPF ***.***.***-** informado");
        assertThat(line.get("level").asString()).isEqualTo("INFO");
        assertThat(line.toString()).doesNotContain("123.456.789-09");
    }

    @Test
    @DisplayName("H3.2: requisição com traceparent → o log contém o mesmo traceId")
    void traceIdPropagado() {
        var line = sampleLogLine("traceparent", "00-" + TRACE_ID + "-00f067aa0ba902b7-01");

        assertThat(line.get("traceId").asString()).isEqualTo(TRACE_ID);
    }

    @Test
    @DisplayName("sem traceparent o serviço começa um trace próprio (traceId presente no log)")
    void semTraceparentCriaTrace() {
        var line = sampleLogLine();

        assertThat(line.get("traceId").asString()).matches("[0-9a-f]{32}").isNotEqualTo(TRACE_ID);
    }

    @Test
    @DisplayName("/actuator/prometheus expõe métricas, inclusive as HTTP depois de uma chamada")
    void metricasPrometheus() throws Exception {
        get("/sample/ping");

        var response = get("/actuator/prometheus");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("jvm_memory_used_bytes").contains("http_server_requests");
    }

    @Test
    @DisplayName("health com liveness e readiness (constituição VII)")
    void healthLivenessEReadiness() throws Exception {
        assertThat(get("/actuator/health/liveness").statusCode()).isEqualTo(200);
        assertThat(get("/actuator/health/readiness").statusCode()).isEqualTo(200);
    }
}
