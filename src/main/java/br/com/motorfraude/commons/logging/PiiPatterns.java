package br.com.motorfraude.commons.logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mascaramento de dados pessoais em texto (LGPD, constituição VI). Função pura, sem dependências.
 *
 * <p>Mascara CPF, e-mail, telefone brasileiro e número de cartão (só com dígito verificador de Luhn válido, para
 * não mascarar identificadores comuns). Tokens do projeto ({@code acc_tok_…}, UUIDs, ids de transação, traceId,
 * instantes ISO) ficam intactos: os padrões exigem que a sequência não esteja colada a letras, dígitos, {@code _}
 * ou {@code -}.
 *
 * <p>Limite conhecido: sequências de 11 dígitos soltas são tratadas como CPF; é melhor mascarar demais do que de
 * menos num log.
 */
public final class PiiPatterns {

    static final String CPF_MASK = "***.***.***-**";
    static final String CPF_DIGITS_MASK = "***********";
    static final String EMAIL_MASK = "***@***";
    static final String PHONE_MASK = "(**) *****-****";
    static final String CARD_MASK = "**** **** **** ****";

    // Fronteiras: não colado a [A-Za-z0-9_] nem a '-' (tokens, UUIDs, ids), nem a '.' antes (datas, IPs, versões).
    private static final String BEFORE = "(?<![\\w.-])";
    private static final String AFTER = "(?![\\w-])";

    private static final Pattern CARD = Pattern.compile(BEFORE + "\\d(?:[ -]?\\d){13,18}" + AFTER);

    private static final Pattern EMAIL = Pattern.compile(
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}");

    private static final Pattern CPF_FORMATTED = Pattern.compile("(?<!\\d)\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}(?!\\d)");

    private static final Pattern PHONE = Pattern.compile(
            // +55 (11) 99999-0000 | +5511999990000
            "(?<![\\w.-])\\+55[ -]?\\(?\\d{2}\\)?[ -]?9?\\d{4}[ -]?\\d{4}" + AFTER
            // (11) 99999-0000 | (11)999990000 | (11) 3333-4444
            + "|" + BEFORE + "\\(\\d{2}\\)[ -]?9?\\d{4}[ -]?\\d{4}" + AFTER
            // 11 99999-0000 (DDD separado por espaço ou hífen)
            + "|" + BEFORE + "\\d{2}[ -]9?\\d{4}[ -]?\\d{4}" + AFTER);

    private static final Pattern CPF_DIGITS = Pattern.compile(BEFORE + "\\d{11}" + AFTER);

    private PiiPatterns() {}

    /** Devolve o texto com os dados pessoais mascarados; {@code null} continua {@code null}. */
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
        // Fronteiras transparentes: o lookbehind enxerga o texto antes da região de busca.
        Matcher m = CARD.matcher(text).useTransparentBounds(true).useAnchoringBounds(false);
        StringBuilder out = null;
        int last = 0;
        while (m.find()) {
            if (!luhnValid(m.group())) {
                // A sequência gulosa pode ter engolido dígitos vizinhos (ex.: um telefone colado a um cartão).
                // Recomeça uma posição adiante para achar um cartão válido dentro dela.
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
