/*
 * @test
 * @summary java.awt.GridBagLayout conformance: class reflection.
 * @run main GridBagLayoutTest
 */

public class GridBagLayoutTest {
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
        Class<?> c = Class.forName("java.awt.GridBagLayout");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.LayoutManager2", "java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "setConstraints"), "method setConstraints");
        check(hasMethod(c, "getConstraints"), "method getConstraints");
        check(hasMethod(c, "getLayoutOrigin"), "method getLayoutOrigin");
        check(hasMethod(c, "getLayoutDimensions"), "method getLayoutDimensions");
        check(hasMethod(c, "getLayoutWeights"), "method getLayoutWeights");
        check(hasMethod(c, "location"), "method location");
        check(hasMethod(c, "addLayoutComponent"), "method addLayoutComponent");
        check(hasMethod(c, "removeLayoutComponent"), "method removeLayoutComponent");
        check(hasMethod(c, "preferredLayoutSize"), "method preferredLayoutSize");
        check(hasMethod(c, "minimumLayoutSize"), "method minimumLayoutSize");
        check(hasMethod(c, "maximumLayoutSize"), "method maximumLayoutSize");
        check(hasMethod(c, "getLayoutAlignmentX"), "method getLayoutAlignmentX");
        check(hasMethod(c, "getLayoutAlignmentY"), "method getLayoutAlignmentY");
        check(hasMethod(c, "invalidateLayout"), "method invalidateLayout");
        check(hasMethod(c, "layoutContainer"), "method layoutContainer");
        check(hasMethod(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
