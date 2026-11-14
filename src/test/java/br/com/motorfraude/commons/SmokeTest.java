package br.com.motorfraude.commons;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class SmokeTest {

    @Test
    void slf4jUsaLogbackComoBackend() {
        assertThat(LoggerFactory.getILoggerFactory().getClass().getName())
                .isEqualTo("ch.qos.logback.classic.LoggerContext");
    }
}
