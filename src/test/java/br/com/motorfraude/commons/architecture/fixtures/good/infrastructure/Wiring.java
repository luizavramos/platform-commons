package br.com.motorfraude.commons.architecture.fixtures.good.infrastructure;

import br.com.motorfraude.commons.time.ClockProvider;
import br.com.motorfraude.commons.architecture.fixtures.good.adapters.TransferEndpoint;
import br.com.motorfraude.commons.architecture.fixtures.good.application.ApproveTransfer;

public final class Wiring {
    public static TransferEndpoint endpoint() {
        return new TransferEndpoint(new ApproveTransfer(ClockProvider.system()));
    }
}
