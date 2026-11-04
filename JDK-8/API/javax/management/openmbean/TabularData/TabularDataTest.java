/*
 * @test
 * @summary javax.management.openmbean.TabularData conformance: interface reflection.
 * @run main TabularDataTest
 */

public class TabularDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.TabularData");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "calculateIndex"), "declares calculateIndex");
        check(hasMethod(c, "clear"), "declares clear");
        check(hasMethod(c, "containsKey"), "declares containsKey");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
