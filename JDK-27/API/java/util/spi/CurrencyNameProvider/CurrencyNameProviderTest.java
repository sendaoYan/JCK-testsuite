/*
 * @test
 * @summary java.util.spi.CurrencyNameProvider conformance: white-box structure + black-box behavior.
 * @run main CurrencyNameProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.spi.CurrencyNameProvider;
import java.util.spi.LocaleServiceProvider;

public class CurrencyNameProviderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static <T extends Throwable> T expectThrows(Class<T> type, Runnable r, String m) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return type.cast(t); }
            throw new AssertionError(m + ": expected " + type.getName() + " but got " + t.getClass().getName());
        }
        throw new AssertionError(m + ": expected " + type.getName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.spi.CurrencyNameProvider");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()),
              "public abstract class");
        check(c.getSuperclass() == LocaleServiceProvider.class, "extends LocaleServiceProvider");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected ctor");

        Method sym = c.getMethod("getSymbol", String.class, Locale.class);
        check(Modifier.isAbstract(sym.getModifiers()) && sym.getReturnType() == String.class,
              "getSymbol abstract String");
        Method dn = c.getMethod("getDisplayName", String.class, Locale.class);
        check(!Modifier.isAbstract(dn.getModifiers()) && dn.getReturnType() == String.class,
              "getDisplayName concrete String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CurrencyNameProvider p = new CurrencyNameProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
            public String getSymbol(String currencyCode, Locale l) {
                return "USD".equals(currencyCode) ? "$" : currencyCode;
            }
        };

        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check("$".equals(p.getSymbol("USD", Locale.US)), "getSymbol USD -> $");
        check("EUR".equals(p.getSymbol("EUR", Locale.US)), "getSymbol EUR -> EUR");

        // valid code + available locale -> default returns null
        check(p.getDisplayName("USD", Locale.US) == null, "getDisplayName available locale -> null");

        // malformed currency codes are rejected before locale lookup
        expectThrows(IllegalArgumentException.class, () -> p.getDisplayName("US", Locale.US),
                     "getDisplayName 2-letter code");
        expectThrows(IllegalArgumentException.class, () -> p.getDisplayName("usd", Locale.US),
                     "getDisplayName lowercase code");
        expectThrows(IllegalArgumentException.class, () -> p.getDisplayName("", Locale.US),
                     "getDisplayName empty code");
        expectThrows(NullPointerException.class, () -> p.getDisplayName(null, Locale.US),
                     "getDisplayName null code");
        expectThrows(NullPointerException.class, () -> p.getDisplayName("USD", null),
                     "getDisplayName null locale");

        // valid code but locale not in getAvailableLocales -> IllegalArgumentException
        expectThrows(IllegalArgumentException.class, () -> p.getDisplayName("USD", Locale.FRANCE),
                     "getDisplayName unavailable locale");
    }


    // ===================== 契约深化：匿名实现行为 + null locale NPE + availableLocales 契约 =====================
    static void testContracts() {
        final Locale[] locs = {Locale.US};
        CurrencyNameProvider p = new CurrencyNameProvider() {
            public Locale[] getAvailableLocales() { return locs; }
            public String getSymbol(String currencyCode, Locale locale) {
                return "CNY-SYM-" + currencyCode; }
            public String getDisplayName(String currencyCode, Locale locale) {
                return "CNY-NAME-" + currencyCode; }
        };
        check("CNY-SYM-USD".equals(p.getSymbol("USD", Locale.US)), "getSymbol override behavior");
        check("CNY-NAME-USD".equals(p.getDisplayName("USD", Locale.US)), "getDisplayName override behavior");
        check(p.getAvailableLocales() == locs, "getAvailableLocales passthrough");
        // 自有实现：null 实参直通（NPE 校验属 JDK 内建 provider，不在接口层强制）
        check("CNY-SYM-null".equals(p.getSymbol(null, Locale.US)), "own impl handles null code");
        check("CNY-NAME-XXX".equals(p.getDisplayName("XXX", Locale.US)), "unknown code passthrough");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
