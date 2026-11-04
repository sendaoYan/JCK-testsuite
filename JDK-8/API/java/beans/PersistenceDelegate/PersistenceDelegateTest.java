/*
 * @test
 * @summary java.beans.PersistenceDelegate conformance: abstract class reflection.
 * @run main PersistenceDelegateTest
 */

public class PersistenceDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.PersistenceDelegate");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "writeObject"), "declares writeObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
