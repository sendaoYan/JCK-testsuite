/*
 * @test
 * @summary java.beans.Visibility conformance: interface reflection.
 * @run main VisibilityTest
 */

public class VisibilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.Visibility");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "avoidingGui"), "declares avoidingGui");
        check(hasMethod(c, "dontUseGui"), "declares dontUseGui");
        check(hasMethod(c, "needsGui"), "declares needsGui");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
