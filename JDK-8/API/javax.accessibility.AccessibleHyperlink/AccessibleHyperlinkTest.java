/*
 * @test
 * @summary javax.accessibility.AccessibleHyperlink conformance: abstract class reflection.
 * @run main AccessibleHyperlinkTest
 */

public class AccessibleHyperlinkTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleHyperlink");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.AccessibleAction"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "isValid"), "method isValid");
        check(hasMethod(c, "getAccessibleActionCount"), "method getAccessibleActionCount");
        check(hasMethod(c, "doAccessibleAction"), "method doAccessibleAction");
        check(hasMethod(c, "getAccessibleActionDescription"), "method getAccessibleActionDescription");
        check(hasMethod(c, "getAccessibleActionObject"), "method getAccessibleActionObject");
        check(hasMethod(c, "getAccessibleActionAnchor"), "method getAccessibleActionAnchor");
        check(hasMethod(c, "getStartIndex"), "method getStartIndex");
        check(hasMethod(c, "getEndIndex"), "method getEndIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
