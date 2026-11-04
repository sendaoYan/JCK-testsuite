/*
 * @test
 * @summary java.awt.color.ICC_Profile conformance: class reflection.
 * @run main ICC_ProfileTest
 */

public class ICC_ProfileTest {
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
        Class<?> c = Class.forName("java.awt.color.ICC_Profile");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "getMajorVersion"), "method getMajorVersion");
        check(hasMethod(c, "getMinorVersion"), "method getMinorVersion");
        check(hasMethod(c, "getProfileClass"), "method getProfileClass");
        check(hasMethod(c, "getColorSpaceType"), "method getColorSpaceType");
        check(hasMethod(c, "getPCSType"), "method getPCSType");
        check(hasMethod(c, "write"), "method write");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "setData"), "method setData");
        check(hasMethod(c, "getNumComponents"), "method getNumComponents");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
