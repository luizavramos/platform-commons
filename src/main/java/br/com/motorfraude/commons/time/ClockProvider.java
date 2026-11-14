package br.com.motorfraude.commons.time;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public final class ClockProvider {

    private ClockProvider() {}

    public static Clock system() {
        return Clock.systemUTC();
    }

    public static Clock fixed(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }
}
