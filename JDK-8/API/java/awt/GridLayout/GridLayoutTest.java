/*
 * @test
 * @summary java.awt.GridLayout conformance: class reflection.
 * @run main GridLayoutTest
 */

public class GridLayoutTest {
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
        Class<?> c = Class.forName("java.awt.GridLayout");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.LayoutManager", "java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getRows"), "method getRows");
        check(hasMethod(c, "setRows"), "method setRows");
        check(hasMethod(c, "getColumns"), "method getColumns");
        check(hasMethod(c, "setColumns"), "method setColumns");
        check(hasMethod(c, "getHgap"), "method getHgap");
        check(hasMethod(c, "setHgap"), "method setHgap");
        check(hasMethod(c, "getVgap"), "method getVgap");
        check(hasMethod(c, "setVgap"), "method setVgap");
        check(hasMethod(c, "addLayoutComponent"), "method addLayoutComponent");
        check(hasMethod(c, "removeLayoutComponent"), "method removeLayoutComponent");
        check(hasMethod(c, "preferredLayoutSize"), "method preferredLayoutSize");
        check(hasMethod(c, "minimumLayoutSize"), "method minimumLayoutSize");
        check(hasMethod(c, "layoutContainer"), "method layoutContainer");
        check(hasMethod(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
