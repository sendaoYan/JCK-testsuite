/*
 * @test
 * @summary java.beans.PropertyChangeListener conformance: interface reflection.
 * @run main PropertyChangeListenerTest
 */

public class PropertyChangeListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyChangeListener");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.util.EventListener").isAssignableFrom(c), "implements/extends java.util.EventListener");
        check(hasMethod(c, "propertyChange"), "declares propertyChange");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
