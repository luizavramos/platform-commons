package br.com.motorfraude.commons.tracing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TraceParentTest {

    private static final String VALID = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01";

    @Test
    void leHeaderValido() {
        var tp = TraceParent.parse(VALID).orElseThrow();

        assertThat(tp.traceId()).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
        assertThat(tp.parentId()).isEqualTo("00f067aa0ba902b7");
        assertThat(tp.flags()).isEqualTo("01");
        assertThat(tp.sampled()).isTrue();
    }

    @Test
    void flagsSemBitDeAmostragem() {
        var tp = TraceParent.parse("00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-00").orElseThrow();

        assertThat(tp.sampled()).isFalse();
    }

    @Test
    void formatDevolveOHeaderOriginal() {
        assertThat(TraceParent.parse(VALID).orElseThrow().format()).isEqualTo(VALID);
    }

    @ParameterizedTest(name = "\"{0}\" é inválido")
    @NullAndEmptySource
    @ValueSource(strings = {
        "lixo",
        "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7",
        "00-4bf92f3577b34da6a3ce929d0e0e473-00f067aa0ba902b7-01",
        "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b-01",
        "00-4BF92F3577B34DA6A3CE929D0E0E4736-00f067aa0ba902b7-01",
        "00-00000000000000000000000000000000-00f067aa0ba902b7-01",
        "00-4bf92f3577b34da6a3ce929d0e0e4736-0000000000000000-01",
        "ff-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
        "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01-extra",
        "00-4bf92f3577b34da6a3ce929d0e0e47zz-00f067aa0ba902b7-01",
        " 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"
    })
    void rejeitaInvalidos(String value) {
        assertThat(TraceParent.parse(value)).isEmpty();
    }

    @Test
    @DisplayName("versão futura com campos extras é aceita, lendo só os quatro primeiros campos (regra do W3C)")
    void versaoFuturaTolerante() {
        var tp = TraceParent.parse("01-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01-campo-novo");

        assertThat(tp).isPresent();
        assertThat(tp.get().traceId()).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
        assertThat(tp.get().format()).startsWith("00-");
    }

    @Test
    void generateProduzHeaderValidoEDiferenteACadaVez() {
        var a = TraceParent.generate();
        var b = TraceParent.generate();

        assertThat(TraceParent.parse(a.format())).contains(a);
        assertThat(a.sampled()).isTrue();
        assertThat(a.traceId()).isNotEqualTo(b.traceId());
        assertThat(a.parentId()).isNotEqualTo(b.parentId());
    }

    @Test
    void childMantemTraceIdETrocaOParentId() {
        var parent = TraceParent.parse(VALID).orElseThrow();

        var child = parent.child();

        assertThat(child.traceId()).isEqualTo(parent.traceId());
        assertThat(child.parentId()).isNotEqualTo(parent.parentId());
        assertThat(child.flags()).isEqualTo(parent.flags());
        assertThat(TraceParent.parse(child.format())).contains(child);
    }
}
