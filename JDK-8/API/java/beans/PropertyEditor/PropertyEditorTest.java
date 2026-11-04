/*
 * @test
 * @summary java.beans.PropertyEditor conformance: interface reflection.
 * @run main PropertyEditorTest
 */

public class PropertyEditorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyEditor");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "addPropertyChangeListener"), "declares addPropertyChangeListener");
        check(hasMethod(c, "getAsText"), "declares getAsText");
        check(hasMethod(c, "getCustomEditor"), "declares getCustomEditor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
