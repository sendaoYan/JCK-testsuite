/*
 * @test
 * @summary javax.sql.XADataSource conformance: interface hierarchy and members.
 * @run main XADataSourceTest
 */


public class XADataSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.XADataSource");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.sql.CommonDataSource");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getLogWriter"), "has method getLogWriter");
        check(names.contains("getLoginTimeout"), "has method getLoginTimeout");
        check(names.contains("getParentLogger"), "has method getParentLogger");        System.out.println("PASS (" + passed + " assertions)");
    }
}
