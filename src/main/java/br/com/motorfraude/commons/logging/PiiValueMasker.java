package br.com.motorfraude.commons.logging;

import net.logstash.logback.mask.ValueMasker;
import tools.jackson.core.TokenStreamContext;

/**
 * Liga o {@link PiiPatterns} ao {@code MaskingJsonGeneratorDecorator} do logstash-logback-encoder: todo valor de
 * texto do JSON de log (mensagem, argumentos, MDC, stack trace) passa por aqui antes de ser escrito.
 *
 * <p>Precisa de construtor público sem argumentos: o Logback o instancia pelo nome da classe em
 * {@code logback-base.xml}.
 */
public class PiiValueMasker implements ValueMasker {

    @Override
    public Object mask(TokenStreamContext context, Object value) {
        if (value instanceof CharSequence text) {
            return PiiPatterns.mask(text.toString());
        }
        return value;
    }
}
