/*
 * @test
 * @summary javax.sql.rowset.spi.SyncProvider conformance: abstract class hierarchy and members.
 * @run main SyncProviderTest
 */


public class SyncProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.spi.SyncProvider");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getDataSourceLock"), "has method getDataSourceLock");
        check(names.contains("getProviderGrade"), "has method getProviderGrade");
        check(names.contains("getProviderID"), "has method getProviderID");        System.out.println("PASS (" + passed + " assertions)");
    }
}
