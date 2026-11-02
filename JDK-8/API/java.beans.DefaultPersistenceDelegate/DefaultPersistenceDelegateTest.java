/*
 * @test
 * @summary java.beans.DefaultPersistenceDelegate conformance: concrete class reflection.
 * @run main DefaultPersistenceDelegateTest
 */

public class DefaultPersistenceDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.DefaultPersistenceDelegate");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.beans.PersistenceDelegate".equals(c.getSuperclass().getName()), "superclass java.beans.PersistenceDelegate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
