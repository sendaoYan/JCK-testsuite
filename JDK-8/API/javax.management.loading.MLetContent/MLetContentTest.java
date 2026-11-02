/*
 * @test
 * @summary javax.management.loading.MLetContent conformance: concrete class reflection.
 * @run main MLetContentTest
 */

public class MLetContentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.loading.MLetContent");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "getCode"), "declares getCode");
        check(hasMethod(c, "getCodeBase"), "declares getCodeBase");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
