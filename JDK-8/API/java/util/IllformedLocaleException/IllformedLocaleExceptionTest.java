/*
 * @test
 * @summary java.util.IllformedLocaleException API conformance: getErrorIndex.
 * @run main IllformedLocaleExceptionTest
 */

import java.util.IllformedLocaleException;

public class IllformedLocaleExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testIllformedLocaleException() {
        IllformedLocaleException e = new IllformedLocaleException("msg", 3);
        check(e instanceof RuntimeException, "extends RuntimeException");
        check(e.getErrorIndex() == 3, "getErrorIndex");
        check(new IllformedLocaleException("m").getErrorIndex() == -1, "no-index ctor uses -1");
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

    public static void main(String[] args) {
        testIllformedLocaleException();
        testIllformedContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
