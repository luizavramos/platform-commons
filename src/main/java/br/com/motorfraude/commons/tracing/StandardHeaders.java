package br.com.motorfraude.commons.tracing;

/** Nomes dos headers Kafka padrão dos tópicos do projeto (specs/000 plan §3.2), definidos num lugar só. */
public final class StandardHeaders {

    /** Contexto de trace W3C. */
    public static final String TRACEPARENT = "traceparent";

    /** Instante de chegada no receiver, ISO-8601 UTC. Base do SLO de 500 ms. */
    public static final String RECEIVED_AT = "receivedAt";

    public static final String SCHEMA_VERSION = "schemaVersion";

    /** Só em comandos (ex.: {@code EvaluateTransaction}). */
    public static final String COMMAND_TYPE = "commandType";

    private StandardHeaders() {}
}
