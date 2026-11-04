/*
 * @test
 * @summary java.beans.VetoableChangeListenerProxy conformance: concrete class reflection.
 * @run main VetoableChangeListenerProxyTest
 */

public class VetoableChangeListenerProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.VetoableChangeListenerProxy");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.util.EventListenerProxy".equals(c.getSuperclass().getName()), "superclass java.util.EventListenerProxy");
        check(Class.forName("java.beans.VetoableChangeListener").isAssignableFrom(c), "implements/extends java.beans.VetoableChangeListener");
        check(hasMethod(c, "getPropertyName"), "declares getPropertyName");
        check(hasMethod(c, "vetoableChange"), "declares vetoableChange");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
