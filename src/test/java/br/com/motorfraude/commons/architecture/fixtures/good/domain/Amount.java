package br.com.motorfraude.commons.architecture.fixtures.good.domain;

public record Amount(long cents) {
    public Amount {
        if (cents <= 0) {
            throw new IllegalArgumentException("valor deve ser positivo");
        }
    }
}
