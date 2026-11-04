/*
 * @test
 * @summary java.awt.event.InputMethodEvent conformance: class reflection.
 * @run main InputMethodEventTest
 */

public class InputMethodEventTest {
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
        Class<?> c = Class.forName("java.awt.event.InputMethodEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.AWTEvent"), "superclass java.awt.AWTEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getText"), "method getText");
        check(hasMethod(c, "getCommittedCharacterCount"), "method getCommittedCharacterCount");
        check(hasMethod(c, "getCaret"), "method getCaret");
        check(hasMethod(c, "getVisiblePosition"), "method getVisiblePosition");
        check(hasMethod(c, "consume"), "method consume");
        check(hasMethod(c, "isConsumed"), "method isConsumed");
        check(hasMethod(c, "getWhen"), "method getWhen");
        check(hasMethod(c, "paramString"), "method paramString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
