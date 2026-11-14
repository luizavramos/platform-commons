package br.com.motorfraude.commons.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PiiPatterns {

    static final String CPF_MASK = "***.***.***-**";
    static final String CPF_DIGITS_MASK = "***********";
    static final String EMAIL_MASK = "***@***";
    static final String PHONE_MASK = "(**) *****-****";
    static final String CARD_MASK = "**** **** **** ****";

    private static final String BEFORE = "(?<![\\w.-])";
    private static final String AFTER = "(?![\\w-])";

    private static final Pattern CARD = Pattern.compile(BEFORE + "\\d(?:[ -]?\\d){13,18}" + AFTER);

    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}");

    private static final Pattern CPF_FORMATTED = Pattern.compile("(?<!\\d)\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}(?!\\d)");

    private static final Pattern PHONE = Pattern.compile(
            "(?<![\\w.-])\\+55[ -]?\\(?\\d{2}\\)?[ -]?9?\\d{4}[ -]?\\d{4}" + AFTER
            + "|" + BEFORE + "\\(\\d{2}\\)[ -]?9?\\d{4}[ -]?\\d{4}" + AFTER
            + "|" + BEFORE + "\\d{2}[ -]9?\\d{4}[ -]?\\d{4}" + AFTER);

    private static final Pattern CPF_DIGITS = Pattern.compile(BEFORE + "\\d{11}" + AFTER);

    private PiiPatterns() {}

    public static String mask(String text) {
        if (text == null || text.length() < 8) {
            return text;
        }
        String result = maskCards(text);
        result = EMAIL.matcher(result).replaceAll(Matcher.quoteReplacement(EMAIL_MASK));
        result = CPF_FORMATTED.matcher(result).replaceAll(Matcher.quoteReplacement(CPF_MASK));
        result = PHONE.matcher(result).replaceAll(Matcher.quoteReplacement(PHONE_MASK));
        result = CPF_DIGITS.matcher(result).replaceAll(Matcher.quoteReplacement(CPF_DIGITS_MASK));
        return result;
    }

    private static String maskCards(String text) {
        Matcher m = CARD.matcher(text).useTransparentBounds(true).useAnchoringBounds(false);
        StringBuilder out = null;
        int last = 0;
        while (m.find()) {
            if (!luhnValid(m.group())) {
                if (m.start() + 1 >= text.length()) {
                    break;
                }
                m.region(m.start() + 1, text.length());
                continue;
            }
            if (out == null) {
                out = new StringBuilder(text.length());
            }
            out.append(text, last, m.start()).append(CARD_MASK);
            last = m.end();
        }
        if (out == null) {
            return text;
        }
        return out.append(text, last, text.length()).toString();
    }

    static boolean luhnValid(String candidate) {
        int sum = 0;
        int digits = 0;
        boolean doubleIt = false;
        for (int i = candidate.length() - 1; i >= 0; i--) {
            char c = candidate.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleIt = !doubleIt;
            digits++;
        }
        return digits >= 13 && sum % 10 == 0;
    }
}
