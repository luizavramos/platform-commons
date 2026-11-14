package br.com.motorfraude.commons.architecture.fixtures.bad.clock.application;

import java.time.Clock;
import java.time.Instant;

public class Deadline {
    public Instant now() {
        return Instant.now();
    }

    public Clock clock() {
        return Clock.systemUTC();
    }
}
