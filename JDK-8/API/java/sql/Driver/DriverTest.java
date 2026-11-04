/*
 * @test
 * @summary java.sql.Driver conformance: interface hierarchy and members.
 * @run main DriverTest
 */


public class DriverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.Driver");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("acceptsURL"), "has method acceptsURL");
        check(names.contains("connect"), "has method connect");
        check(names.contains("getMajorVersion"), "has method getMajorVersion");        System.out.println("PASS (" + passed + " assertions)");
    }
}
