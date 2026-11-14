package br.com.motorfraude.commons.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Relógio controlado pelo teste, publicado no test-jar do commons: os testes dos serviços fazem o tempo andar com
 * {@link #advance(Duration)} em vez de dormir. Sempre UTC: {@link #withZone(ZoneId)} devolve o próprio relógio.
 */
public final class MutableClock extends Clock {

    private volatile Instant now;

    public MutableClock(Instant start) {
        this.now = start;
    }

    public void advance(Duration duration) {
        this.now = now.plus(duration);
    }

    public void set(Instant instant) {
        this.now = instant;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return now;
    }
}
