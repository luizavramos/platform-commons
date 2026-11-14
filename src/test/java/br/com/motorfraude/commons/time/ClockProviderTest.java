package br.com.motorfraude.commons.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClockProviderTest {

    @Test
    @DisplayName("o relógio de produção é UTC e anda com o tempo real")
    void relogioDeProducao() {
        var clock = ClockProvider.system();

        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
        assertThat(clock.instant()).isBetween(Instant.now().minusSeconds(5), Instant.now().plusSeconds(5));
    }

    @Test
    void relogioFixoParaTestes() {
        var clock = ClockProvider.fixed(Instant.parse("2026-11-13T21:30:00Z"));

        assertThat(clock.instant()).isEqualTo(Instant.parse("2026-11-13T21:30:00Z"));
        assertThat(clock.instant()).isEqualTo(clock.instant());
        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("MutableClock avança só quando o teste manda")
    void relogioMutavel() {
        var clock = new MutableClock(Instant.parse("2026-11-13T21:30:00Z"));

        assertThat(clock.instant()).isEqualTo(Instant.parse("2026-11-13T21:30:00Z"));

        clock.advance(Duration.ofMinutes(10));
        assertThat(clock.instant()).isEqualTo(Instant.parse("2026-11-13T21:40:00Z"));

        clock.set(Instant.parse("2026-11-14T00:00:00Z"));
        assertThat(clock.millis()).isEqualTo(Instant.parse("2026-11-14T00:00:00Z").toEpochMilli());
    }

    @Test
    void relogioMutavelEmUtcEIgnoraTrocaDeFuso() {
        var clock = new MutableClock(Instant.parse("2026-11-13T21:30:00Z"));

        assertThat(clock.getZone()).isEqualTo(ZoneOffset.UTC);
        assertThat(clock.withZone(java.time.ZoneId.of("America/Sao_Paulo")).instant()).isEqualTo(clock.instant());
    }
}
