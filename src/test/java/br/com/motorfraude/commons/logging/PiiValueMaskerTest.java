package br.com.motorfraude.commons.logging;

import static net.logstash.logback.argument.StructuredArguments.kv;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

class PiiValueMaskerTest {

    private static final Logger log = LoggerFactory.getLogger(PiiValueMaskerTest.class);

    @Test
    @DisplayName("log sai em JSON de uma linha, com nível e mensagem")
    void saiEmJson() {
        var json = LogCapture.captureOne(() -> log.info("transacao recebida"));

        assertThat(json.get("message").asString()).isEqualTo("transacao recebida");
        assertThat(json.get("level").asString()).isEqualTo("INFO");
        assertThat(json.has("@timestamp")).isTrue();
    }

    @Test
    @DisplayName("H3.1: mensagem com CPF sai mascarada")
    void mensagem() {
        var json = LogCapture.captureOne(() -> log.info("CPF 123.456.789-09"));

        assertThat(json.get("message").asString()).isEqualTo("CPF ***.***.***-**");
    }

    @Test
    @DisplayName("argumento posicional da mensagem")
    void argumentoPosicional() {
        var json = LogCapture.captureOne(() -> log.info("cliente {} pagou", "maria@exemplo.com"));

        assertThat(json.get("message").asString()).isEqualTo("cliente ***@*** pagou");
    }

    @Test
    @DisplayName("argumento estruturado vira campo do JSON, mascarado")
    void argumentoEstruturado() {
        var json = LogCapture.captureOne(() -> log.info("contato {}", kv("telefone", "+5511999990000")));

        assertThat(json.get("telefone").asString()).isEqualTo("(**) *****-****");
        assertThat(json.get("message").asString()).doesNotContain("5511999990000");
    }

    @Test
    @DisplayName("valores do MDC também são mascarados, e traceId/transactionId/alertId passam intactos")
    void mdc() {
        MDC.put("customerEmail", "maria@exemplo.com");
        MDC.put("traceId", "4bf92f3577b34da6a3ce929d0e0e4736");
        MDC.put("transactionId", "tx_20261113_0001");
        MDC.put("alertId", "7c9e6679-7425-5a1e-b2f1-3f1d8e6a9c10");
        try {
            var json = LogCapture.captureOne(() -> log.info("processando"));

            assertThat(json.get("customerEmail").asString()).isEqualTo("***@***");
            assertThat(json.get("traceId").asString()).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
            assertThat(json.get("transactionId").asString()).isEqualTo("tx_20261113_0001");
            assertThat(json.get("alertId").asString()).isEqualTo("7c9e6679-7425-5a1e-b2f1-3f1d8e6a9c10");
        } finally {
            MDC.clear();
        }
    }

    @Test
    @DisplayName("mensagem de exceção e stack trace são mascarados")
    void excecao() {
        var json = LogCapture.captureOne(
                () -> log.error("falha ao notificar", new IllegalStateException("conta de maria@exemplo.com cpf 123.456.789-09")));

        var stack = json.get("stack_trace").asString();
        assertThat(stack).contains("IllegalStateException").contains("***@***").contains("***.***.***-**");
        assertThat(stack).doesNotContain("maria@exemplo.com").doesNotContain("123.456.789-09");
    }

    @Test
    @DisplayName("nenhum dado pessoal aparece em nenhuma parte da linha de log")
    void linhaInteiraSemDadoPessoal() {
        var json = LogCapture.captureOne(() -> log.warn("cliente {} {} {}", "maria@exemplo.com", "123.456.789-09", "(11) 99999-0000"));

        assertThat(json.toString())
                .doesNotContain("maria@exemplo.com")
                .doesNotContain("123.456.789-09")
                .doesNotContain("99999-0000");
    }
}
