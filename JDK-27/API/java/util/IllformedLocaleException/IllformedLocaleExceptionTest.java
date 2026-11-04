/*
 * @test
 * @summary java.util.IllformedLocaleException conformance: white-box structure + black-box behavior.
 * @run main IllformedLocaleExceptionTest
 */

import java.util.IllformedLocaleException;
import java.util.Locale;

public class IllformedLocaleExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.IllformedLocaleException");
        check(c.getSuperclass() == RuntimeException.class, "extends RuntimeException");
        c.getDeclaredConstructor();
        c.getDeclaredConstructor(String.class);
        c.getDeclaredConstructor(String.class, int.class);
        check(c.getDeclaredMethod("getErrorIndex").getReturnType() == int.class,
                "getErrorIndex int");
    }

    static void testBlackBox() {
        check(new IllformedLocaleException().getErrorIndex() == -1, "default errorIndex -1");
        check(new IllformedLocaleException("m").getErrorIndex() == -1, "String ctor errorIndex -1");
        check(new IllformedLocaleException("m", 3).getErrorIndex() == 3, "explicit errorIndex");
        check("m [at index 3]".equals(new IllformedLocaleException("m", 3).getMessage()),
                "message with index");

        expectThrows(() -> new Locale.Builder().setLanguage("abcdefghi"),
                IllformedLocaleException.class, "ill-formed language");
    }


    // ===================== 契约深化：层次/消息保持/Builder 触发携带错误索引 =====================
    static void testIllformedContracts() {
        check(new IllformedLocaleException("m") instanceof RuntimeException,
              "extends RuntimeException");
        check("m2".equals(new IllformedLocaleException("m2").getMessage()), "message preserved");
        check(new IllformedLocaleException("m2").getErrorIndex() == -1,
              "1-arg ctor default index -1");
        check(new IllformedLocaleException("i3", 7).getMessage().startsWith("i3")
              && new IllformedLocaleException("i3", 7).getErrorIndex() == 7,
              "2-arg ctor message + index (message decorated with index)");
        try {
            new java.util.Locale.Builder().setRegion("USA");
            check(false, "Builder.setRegion triggers IllformedLocaleException");
        } catch (IllformedLocaleException ile) {
            check(ile.getErrorIndex() >= 0, "trigger carries error index");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testIllformedContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
