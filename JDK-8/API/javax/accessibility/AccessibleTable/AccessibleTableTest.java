/*
 * @test
 * @summary javax.accessibility.AccessibleTable conformance: interface reflection.
 * @run main AccessibleTableTest
 */

public class AccessibleTableTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleTable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleCaption"), "method getAccessibleCaption");
        check(hasMethod(c, "setAccessibleCaption"), "method setAccessibleCaption");
        check(hasMethod(c, "getAccessibleSummary"), "method getAccessibleSummary");
        check(hasMethod(c, "setAccessibleSummary"), "method setAccessibleSummary");
        check(hasMethod(c, "getAccessibleRowCount"), "method getAccessibleRowCount");
        check(hasMethod(c, "getAccessibleColumnCount"), "method getAccessibleColumnCount");
        check(hasMethod(c, "getAccessibleAt"), "method getAccessibleAt");
        check(hasMethod(c, "getAccessibleRowExtentAt"), "method getAccessibleRowExtentAt");
        check(hasMethod(c, "getAccessibleColumnExtentAt"), "method getAccessibleColumnExtentAt");
        check(hasMethod(c, "getAccessibleRowHeader"), "method getAccessibleRowHeader");
        check(hasMethod(c, "setAccessibleRowHeader"), "method setAccessibleRowHeader");
        check(hasMethod(c, "getAccessibleColumnHeader"), "method getAccessibleColumnHeader");
        check(hasMethod(c, "setAccessibleColumnHeader"), "method setAccessibleColumnHeader");
        check(hasMethod(c, "getAccessibleRowDescription"), "method getAccessibleRowDescription");
        check(hasMethod(c, "setAccessibleRowDescription"), "method setAccessibleRowDescription");
        check(hasMethod(c, "getAccessibleColumnDescription"), "method getAccessibleColumnDescription");
        check(hasMethod(c, "setAccessibleColumnDescription"), "method setAccessibleColumnDescription");
        check(hasMethod(c, "isAccessibleSelected"), "method isAccessibleSelected");
        check(hasMethod(c, "isAccessibleRowSelected"), "method isAccessibleRowSelected");
        check(hasMethod(c, "isAccessibleColumnSelected"), "method isAccessibleColumnSelected");
        check(hasMethod(c, "getSelectedAccessibleRows"), "method getSelectedAccessibleRows");
        check(hasMethod(c, "getSelectedAccessibleColumns"), "method getSelectedAccessibleColumns");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
