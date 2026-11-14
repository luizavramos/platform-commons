package br.com.motorfraude.commons.architecture.fixtures.good.application;

import br.com.motorfraude.commons.architecture.fixtures.good.domain.Amount;
import java.time.Clock;
import java.time.Instant;

public final class ApproveTransfer {
    private final Clock clock;

    public ApproveTransfer(Clock clock) {
        this.clock = clock;
    }

    public Instant approvedAt(Amount amount) {
        return amount.cents() > 0 ? clock.instant() : Instant.EPOCH;
    }
}
