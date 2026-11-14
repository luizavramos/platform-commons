package br.com.motorfraude.commons.architecture.fixtures.good.adapters;

import br.com.motorfraude.commons.architecture.fixtures.good.application.ApproveTransfer;
import br.com.motorfraude.commons.architecture.fixtures.good.domain.Amount;

public final class TransferEndpoint {
    private final ApproveTransfer useCase;

    public TransferEndpoint(ApproveTransfer useCase) {
        this.useCase = useCase;
    }

    public String handle(long cents) {
        return useCase.approvedAt(new Amount(cents)).toString();
    }
}
