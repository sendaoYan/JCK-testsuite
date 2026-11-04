/*
 * @test
 * @summary JLS 3.10.8 (Java SE 8): the null literal is the sole value of the
 *          null type; it may be assigned or cast to any reference type; null
 *          == null.
 * @run main NullLiteral
 */

public class NullLiteral {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        Object o = null;
        check(o == null, "null is the null reference");

        String s = null;
        int[] arr = null;
        Runnable r = null;
        check(s == null && arr == null && r == null,
                "null assignable to every reference type");

        Object co = (Object) null;
        String cs = (String) null;
        Runnable cr = (Runnable) null;
        check(co == null && cs == null && cr == null,
                "null castable to every reference type");

        check(null == null, "null == null");
        check(!(null != null), "!(null != null)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
