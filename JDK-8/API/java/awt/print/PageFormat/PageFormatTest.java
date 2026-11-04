/*
 * @test
 * @summary java.awt.print.PageFormat conformance: class reflection.
 * @run main PageFormatTest
 */

public class PageFormatTest {
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
        Class<?> c = Class.forName("java.awt.print.PageFormat");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getImageableX"), "method getImageableX");
        check(hasMethod(c, "getImageableY"), "method getImageableY");
        check(hasMethod(c, "getImageableWidth"), "method getImageableWidth");
        check(hasMethod(c, "getImageableHeight"), "method getImageableHeight");
        check(hasMethod(c, "getPaper"), "method getPaper");
        check(hasMethod(c, "setPaper"), "method setPaper");
        check(hasMethod(c, "setOrientation"), "method setOrientation");
        check(hasMethod(c, "getOrientation"), "method getOrientation");
        check(hasMethod(c, "getMatrix"), "method getMatrix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
