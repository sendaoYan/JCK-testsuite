/*
 * @test
 * @summary java.awt.Menu conformance: class reflection.
 * @run main MenuTest
 */

public class MenuTest {
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
        Class<?> c = Class.forName("java.awt.Menu");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.MenuItem"), "superclass java.awt.MenuItem");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.MenuContainer", "javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "isTearOff"), "method isTearOff");
        check(hasMethod(c, "getItemCount"), "method getItemCount");
        check(hasMethod(c, "countItems"), "method countItems");
        check(hasMethod(c, "getItem"), "method getItem");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "insert"), "method insert");
        check(hasMethod(c, "addSeparator"), "method addSeparator");
        check(hasMethod(c, "insertSeparator"), "method insertSeparator");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "removeAll"), "method removeAll");
        check(hasMethod(c, "paramString"), "method paramString");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
