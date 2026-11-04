/*
 * @test
 * @summary java.awt.AWTEvent conformance: abstract class reflection.
 * @run main AWTEventTest
 */

public class AWTEventTest {
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
        Class<?> c = Class.forName("java.awt.AWTEvent");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "superclass java.util.EventObject");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setSource"), "method setSource");
        check(hasMethod(c, "getID"), "method getID");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "paramString"), "method paramString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
