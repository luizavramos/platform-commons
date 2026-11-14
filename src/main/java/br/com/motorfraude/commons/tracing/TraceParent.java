package br.com.motorfraude.commons.tracing;

import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record TraceParent(String traceId, String parentId, String flags) {

    private static final Pattern FORMAT =
            Pattern.compile("^([0-9a-f]{2})-([0-9a-f]{32})-([0-9a-f]{16})-([0-9a-f]{2})(-.*)?$");
    private static final String ZERO_TRACE_ID = "0".repeat(32);
    private static final String ZERO_PARENT_ID = "0".repeat(16);
    private static final HexFormat HEX = HexFormat.of();

    public static Optional<TraceParent> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        Matcher m = FORMAT.matcher(value);
        if (!m.matches()) {
            return Optional.empty();
        }
        String version = m.group(1);
        if (version.equals("ff") || (version.equals("00") && m.group(5) != null)) {
            return Optional.empty();
        }
        String traceId = m.group(2);
        String parentId = m.group(3);
        if (traceId.equals(ZERO_TRACE_ID) || parentId.equals(ZERO_PARENT_ID)) {
            return Optional.empty();
        }
        return Optional.of(new TraceParent(traceId, parentId, m.group(4)));
    }

    public static TraceParent generate() {
        return new TraceParent(randomHex(16, ZERO_TRACE_ID), randomHex(8, ZERO_PARENT_ID), "01");
    }

    public TraceParent child() {
        return new TraceParent(traceId, randomHex(8, ZERO_PARENT_ID), flags);
    }

    public boolean sampled() {
        return (Integer.parseInt(flags, 16) & 1) == 1;
    }

    public String format() {
        return "00-" + traceId + "-" + parentId + "-" + flags;
    }

    private static String randomHex(int bytes, String zero) {
        var random = ThreadLocalRandom.current();
        byte[] buffer = new byte[bytes];
        String hex;
        do {
            random.nextBytes(buffer);
            hex = HEX.formatHex(buffer);
        } while (hex.equals(zero));
        return hex;
    }
}
