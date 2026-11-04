/*
 * @test
 * @summary java.awt.Label conformance: class reflection.
 * @run main LabelTest
 */

public class LabelTest {
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
        Class<?> c = Class.forName("java.awt.Label");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getAlignment"), "method getAlignment");
        check(hasMethod(c, "setAlignment"), "method setAlignment");
        check(hasMethod(c, "getText"), "method getText");
        check(hasMethod(c, "setText"), "method setText");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
