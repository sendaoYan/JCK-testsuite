/*
 * @test
 * @summary java.awt.Choice.AccessibleAWTChoice conformance: class reflection.
 * @run main AccessibleAWTChoiceTest
 */

public class AccessibleAWTChoiceTest {
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
        Class<?> c = Class.forName("java.awt.Choice$AccessibleAWTChoice");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component$AccessibleAWTComponent"), "superclass java.awt.Component$AccessibleAWTComponent");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.AccessibleAction"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getAccessibleAction"), "method getAccessibleAction");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        check(hasMethod(c, "getAccessibleActionCount"), "method getAccessibleActionCount");
        check(hasMethod(c, "getAccessibleActionDescription"), "method getAccessibleActionDescription");
        check(hasMethod(c, "doAccessibleAction"), "method doAccessibleAction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
