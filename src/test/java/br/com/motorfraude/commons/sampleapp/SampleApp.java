package br.com.motorfraude.commons.sampleapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SampleApp {

    @RestController
    static class PingController {

        private static final Logger log = LoggerFactory.getLogger(PingController.class);

        @GetMapping("/sample/ping")
        String ping() {
            log.info("CPF 123.456.789-09 informado");
            return "ok";
        }
    }
}
