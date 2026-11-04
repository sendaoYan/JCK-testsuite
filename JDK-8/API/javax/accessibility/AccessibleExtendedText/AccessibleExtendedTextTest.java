/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedText conformance: interface reflection.
 * @run main AccessibleExtendedTextTest
 */

public class AccessibleExtendedTextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedText");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getTextRange"), "method getTextRange");
        check(hasMethod(c, "getTextSequenceAt"), "method getTextSequenceAt");
        check(hasMethod(c, "getTextSequenceAfter"), "method getTextSequenceAfter");
        check(hasMethod(c, "getTextSequenceBefore"), "method getTextSequenceBefore");
        check(hasMethod(c, "getTextBounds"), "method getTextBounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
