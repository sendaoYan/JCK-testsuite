/*
 * @test
 * @summary java.beans.DesignMode conformance: interface reflection.
 * @run main DesignModeTest
 */

public class DesignModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.DesignMode");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "isDesignTime"), "declares isDesignTime");
        check(hasMethod(c, "setDesignTime"), "declares setDesignTime");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
