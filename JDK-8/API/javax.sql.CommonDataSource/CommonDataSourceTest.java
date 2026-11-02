/*
 * @test
 * @summary javax.sql.CommonDataSource conformance: interface hierarchy and members.
 * @run main CommonDataSourceTest
 */


public class CommonDataSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.CommonDataSource");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getLogWriter"), "has method getLogWriter");
        check(names.contains("getLoginTimeout"), "has method getLoginTimeout");
        check(names.contains("getParentLogger"), "has method getParentLogger");        System.out.println("PASS (" + passed + " assertions)");
    }
}
