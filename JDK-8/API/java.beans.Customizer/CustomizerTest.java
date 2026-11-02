/*
 * @test
 * @summary java.beans.Customizer conformance: interface reflection.
 * @run main CustomizerTest
 */

public class CustomizerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.Customizer");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "addPropertyChangeListener"), "declares addPropertyChangeListener");
        check(hasMethod(c, "removePropertyChangeListener"), "declares removePropertyChangeListener");
        check(hasMethod(c, "setObject"), "declares setObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
