package io.github.wimdeblauwe.ttcli.util;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Prints status lines prefixed with an emoji, falling back to plain ASCII on terminals that
 * cannot render emoji (non-UTF-8 output encoding, {@code TERM=dumb}/{@code linux}, or when the
 * {@code NO_EMOJI} environment variable is set).
 * <p>
 * Only single code point emoji are used on purpose: sequences with skin tone modifiers,
 * zero-width joiners or variation selectors render inconsistently across terminals.
 */
public final class ConsoleOutput {

    public enum Icon {
        SUCCESS("✅", "[OK]"),
        ERROR("❌", "[ERROR]"),
        INFO("🔍", "->"),
        BUILD("🔨", "->"),
        CONFIGURE("🔧", "->"),
        PACKAGE("📦", "->");

        private final String emoji;
        private final String fallback;

        Icon(String emoji, String fallback) {
            this.emoji = emoji;
            this.fallback = fallback;
        }
    }

    private ConsoleOutput() {
    }

    public static void println(Icon icon, String message) {
        System.out.println(format(icon, message));
    }

    public static void printlnError(Icon icon, String message) {
        System.err.println(format(icon, message));
    }

    private static String format(Icon icon, String message) {
        // Evaluated on each call (not cached in a static field) so a native image never bakes in build-time values
        boolean emoji = supportsEmoji(System.getProperty("stdout.encoding"), System.getenv());
        return (emoji ? icon.emoji : icon.fallback) + " " + message;
    }

    static boolean supportsEmoji(String stdoutEncoding, Map<String, String> env) {
        if (env.containsKey("NO_EMOJI")) {
            return false;
        }
        String term = env.get("TERM");
        if ("dumb".equals(term) || "linux".equals(term)) {
            return false;
        }
        String encoding = stdoutEncoding != null ? stdoutEncoding : Charset.defaultCharset().name();
        return encoding.replace("-", "").equalsIgnoreCase("UTF8");
    }
}
