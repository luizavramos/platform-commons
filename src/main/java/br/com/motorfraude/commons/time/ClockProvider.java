package br.com.motorfraude.commons.time;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Fonte única de {@link Clock} (constituição VIII): nenhum código de domínio ou de caso de uso chama
 * {@code Instant.now()}; recebe um {@code Clock} por injeção. Em produção é o relógio UTC; nos testes, um fixo ou
 * o {@code MutableClock} do test-jar deste módulo.
 */
public final class ClockProvider {

    private ClockProvider() {}

    public static Clock system() {
        return Clock.systemUTC();
    }

    public static Clock fixed(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }
}
