/*
 * @test
 * @summary java.awt.Desktop.Action conformance: enum reflection.
 * @run main ActionTest
 */

public class ActionTest {
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
        Class<?> c = Class.forName("java.awt.Desktop$Action");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "values"), "method values");
        check(hasMethod(c, "valueOf"), "method valueOf");
        Object[] consts = c.getEnumConstants();
        check(consts != null && consts.length == 5, "5 enum constants");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Object o : consts) names.add(o.toString());
        check(names.contains("OPEN"), "constant OPEN");
        check(names.contains("EDIT"), "constant EDIT");
        check(names.contains("PRINT"), "constant PRINT");
        check(names.contains("MAIL"), "constant MAIL");
        check(names.contains("BROWSE"), "constant BROWSE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
