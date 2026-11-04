/*
 * @test
 * @summary java.awt.im.InputMethodRequests conformance: interface reflection.
 * @run main InputMethodRequestsTest
 */

public class InputMethodRequestsTest {
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
        Class<?> c = Class.forName("java.awt.im.InputMethodRequests");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getTextLocation"), "method getTextLocation");
        check(hasMethod(c, "getLocationOffset"), "method getLocationOffset");
        check(hasMethod(c, "getInsertPositionOffset"), "method getInsertPositionOffset");
        check(hasMethod(c, "getCommittedText"), "method getCommittedText");
        check(hasMethod(c, "getCommittedTextLength"), "method getCommittedTextLength");
        check(hasMethod(c, "cancelLatestCommittedText"), "method cancelLatestCommittedText");
        check(hasMethod(c, "getSelectedText"), "method getSelectedText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
