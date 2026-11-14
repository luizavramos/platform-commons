package br.com.motorfraude.commons.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PiiPatternsTest {

    @Test
    @DisplayName("H3.1: 'CPF 123.456.789-09' sai como 'CPF ***.***.***-**'")
    void cpfFormatado() {
        assertThat(PiiPatterns.mask("CPF 123.456.789-09")).isEqualTo("CPF ***.***.***-**");
    }

    @Test
    void cpfSemFormatacao() {
        assertThat(PiiPatterns.mask("cpf 12345678909 informado")).isEqualTo("cpf *********** informado");
    }

    @Test
    void email() {
        assertThat(PiiPatterns.mask("contato maria.silva+pix@exemplo.com.br enviado"))
                .isEqualTo("contato ***@*** enviado");
    }

    @ParameterizedTest(name = "telefone \"{0}\" é mascarado")
    @ValueSource(strings = {"+55 11 99999-0000", "+5511999990000", "(11) 99999-0000", "(11)999990000", "11 99999-0000", "(11) 3333-4444"})
    void telefone(String phone) {
        var masked = PiiPatterns.mask("ligar para " + phone + " hoje");

        assertThat(masked).isEqualTo("ligar para (**) *****-**** hoje");
    }

    @ParameterizedTest(name = "cartão \"{0}\" (Luhn válido) é mascarado")
    @ValueSource(strings = {"4111 1111 1111 1111", "4111-1111-1111-1111", "4111111111111111", "5555555555554444", "378282246310005"})
    void cartaoComLuhnValido(String card) {
        assertThat(PiiPatterns.mask("cartao " + card + " recusado")).isEqualTo("cartao **** **** **** **** recusado");
    }

    @Test
    @DisplayName("sequência de 16 dígitos com Luhn inválido não é cartão")
    void luhnInvalidoNaoEMascarado() {
        assertThat(PiiPatterns.mask("ref 1234 5678 9012 3456")).isEqualTo("ref 1234 5678 9012 3456");
    }

    @Test
    void variosDadosNaMesmaLinha() {
        var masked = PiiPatterns.mask("cliente maria@exemplo.com cpf 123.456.789-09 tel (11) 99999-0000");

        assertThat(masked).isEqualTo("cliente ***@*** cpf ***.***.***-** tel (**) *****-****");
    }

    @ParameterizedTest(name = "\"{0}\" não é dado pessoal e fica intacto")
    @ValueSource(strings = {
        "acc_tok_00000001",
        "cus_tok_00000001",
        "dev_0f3a9c1d5b7e2a64",
        "tx_20261113_0001",
        "7c9e6679-7425-5a1e-b2f1-3f1d8e6a9c10",
        "12345678-1234-1234-1234-123456789012",
        "4bf92f3577b34da6a3ce929d0e0e4736",
        "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
        "2026-11-13T21:30:00.120Z",
        "valor 15000 centavos",
        "epoch 1731533400000",
        "ip 192.168.0.10 porta 9092",
        "bankIspb 60701190",
        "count1m=3 sum1h=45000 score=75"
    })
    void naoMascaraIdentificadoresDoProjeto(String text) {
        assertThat(PiiPatterns.mask(text)).isEqualTo(text);
    }

    @Test
    @DisplayName("mascarar duas vezes dá o mesmo resultado")
    void idempotente() {
        var once = PiiPatterns.mask("cpf 123.456.789-09 maria@exemplo.com (11) 99999-0000 4111 1111 1111 1111");

        assertThat(PiiPatterns.mask(once)).isEqualTo(once);
    }

    @Test
    void nuloEVazio() {
        assertThat(PiiPatterns.mask(null)).isNull();
        assertThat(PiiPatterns.mask("")).isEmpty();
    }
}
