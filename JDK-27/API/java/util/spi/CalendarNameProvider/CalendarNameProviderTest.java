/*
 * @test
 * @summary java.util.spi.CalendarNameProvider conformance: white-box structure + black-box behavior.
 * @run main CalendarNameProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.spi.CalendarNameProvider;
import java.util.spi.LocaleServiceProvider;

public class CalendarNameProviderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.spi.CalendarNameProvider");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()),
              "public abstract class");
        check(c.getSuperclass() == LocaleServiceProvider.class, "extends LocaleServiceProvider");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected ctor");

        Method name = c.getMethod("getDisplayName", String.class, int.class, int.class, int.class, Locale.class);
        check(Modifier.isAbstract(name.getModifiers()) && name.getReturnType() == String.class,
              "getDisplayName abstract String");
        Method names = c.getMethod("getDisplayNames", String.class, int.class, int.class, Locale.class);
        check(Modifier.isAbstract(names.getModifiers()), "getDisplayNames abstract");
        check(names.getReturnType() == Map.class, "getDisplayNames returns Map");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CalendarNameProvider p = new CalendarNameProvider() {
            public Locale[] getAvailableLocales() { return new Locale[]{Locale.US}; }
            public String getDisplayName(String calendarType, int field, int value, int style, Locale l) {
                return calendarType + ":" + field + ":" + value + ":" + style;
            }
            public Map<String, Integer> getDisplayNames(String calendarType, int field, int style, Locale l) {
                Map<String, Integer> m = new TreeMap<>();
                m.put("January", 0);
                m.put("February", 1);
                m.put("March", 2);
                return m;
            }
        };

        check(p instanceof LocaleServiceProvider, "extends LocaleServiceProvider");
        check("gregory:2:0:2".equals(
                p.getDisplayName("gregory", Calendar.MONTH, Calendar.JANUARY, Calendar.LONG, Locale.US)),
              "getDisplayName round-trip");

        Map<String, Integer> names = p.getDisplayNames("gregory", Calendar.MONTH, Calendar.LONG, Locale.US);
        check(names != null && names.size() == 3, "getDisplayNames size == 3");
        String[] keys = names.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        check(Arrays.equals(keys, new String[]{"February", "January", "March"}),
              "getDisplayNames keys sorted");
        check(names.get("January") == 0 && names.get("February") == 1 && names.get("March") == 2,
              "getDisplayNames values");
    }


    // ===================== 契约深化：匿名实现 getDisplayName + null NPE + style 变体 =====================
    static void testContracts() {
        final Locale[] locs = {Locale.GERMANY};
        CalendarNameProvider p = new CalendarNameProvider() {
            public Locale[] getAvailableLocales() { return locs; }
            public String getDisplayName(String calendarType, int field, int value, int style, Locale locale) {
                return calendarType + "-F" + field + "V" + value + "S" + style; }
            public java.util.Map<String, Integer> getDisplayNames(String calendarType, int field, int style, Locale locale) {
                return java.util.Collections.singletonMap("only", 1); }
        };
        check("gregory-F2V2S1".equals(p.getDisplayName("gregory", java.util.Calendar.MONTH, 2,
                java.util.Calendar.SHORT_FORMAT, Locale.GERMANY)),
              "getDisplayName(String,int,int,int,Locale) override behavior");
        check(p.getDisplayNames("gregory", java.util.Calendar.MONTH,
                java.util.Calendar.LONG_FORMAT, Locale.GERMANY).get("only") == Integer.valueOf(1),
              "getDisplayNames override behavior");
        // Calendar style 常量矩阵（SHORT/LONG x FORMAT/STANDALONE）
        check(java.util.Calendar.SHORT_FORMAT != java.util.Calendar.LONG_FORMAT
              && java.util.Calendar.SHORT_STANDALONE != java.util.Calendar.LONG_STANDALONE
              && java.util.Calendar.SHORT_FORMAT != java.util.Calendar.SHORT_STANDALONE,
              "four style constants pairwise distinct");
        // 自有实现 null 容忍
        check(p.getDisplayName(null, 2, 2, java.util.Calendar.SHORT_FORMAT, Locale.GERMANY).startsWith("null-"),
              "own impl tolerates null calendarType");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
