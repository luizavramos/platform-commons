package br.com.motorfraude.commons.logging;

import net.logstash.logback.mask.ValueMasker;
import tools.jackson.core.TokenStreamContext;

public class PiiValueMasker implements ValueMasker {

    @Override
    public Object mask(TokenStreamContext context, Object value) {
        if (value instanceof CharSequence text) {
            return PiiPatterns.mask(text.toString());
        }
        return value;
    }
}
