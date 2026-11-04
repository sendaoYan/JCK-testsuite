/*
 * @test
 * @summary JLS 7.1 (Java SE 8): members of different packages may have the same
 *          simple name; they are distinct types reflected by distinct fully
 *          qualified names.
 * @compile a/Thing.java b/Thing.java Main.java
 * @run main Main
 */

public class Main {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        a.Thing ta = new a.Thing();
        b.Thing tb = new b.Thing();
        check(ta.id == 1, "a.Thing field");
        check(tb.id == 2, "b.Thing field");

        String fa = ta.getClass().getName();
        String fb = tb.getClass().getName();
        check("a.Thing".equals(fa), "a.Thing fully qualified name: " + fa);
        check("b.Thing".equals(fb), "b.Thing fully qualified name: " + fb);
        check(!fa.equals(fb), "distinct types have distinct fully qualified names");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
