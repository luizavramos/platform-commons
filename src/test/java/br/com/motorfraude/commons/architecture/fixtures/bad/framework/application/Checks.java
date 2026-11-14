package br.com.motorfraude.commons.architecture.fixtures.bad.framework.application;

import org.springframework.util.Assert;

public class Checks {
    public void ensure(Object value) {
        Assert.notNull(value, "value");
    }
}
