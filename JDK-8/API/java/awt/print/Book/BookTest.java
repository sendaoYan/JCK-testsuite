/*
 * @test
 * @summary java.awt.print.Book conformance: class reflection.
 * @run main BookTest
 */

public class BookTest {
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
        Class<?> c = Class.forName("java.awt.print.Book");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.print.Pageable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getNumberOfPages"), "method getNumberOfPages");
        check(hasMethod(c, "getPageFormat"), "method getPageFormat");
        check(hasMethod(c, "getPrintable"), "method getPrintable");
        check(hasMethod(c, "setPage"), "method setPage");
        check(hasMethod(c, "append"), "method append");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
