/*
 * @test
 * @summary java.awt.Cursor conformance: class reflection.
 * @run main CursorTest
 */

public class CursorTest {
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
        Class<?> c = Class.forName("java.awt.Cursor");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getPredefinedCursor"), "method getPredefinedCursor");
        check(hasMethod(c, "getSystemCustomCursor"), "method getSystemCustomCursor");
        check(hasMethod(c, "getDefaultCursor"), "method getDefaultCursor");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
