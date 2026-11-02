/*
 * @test
 * @summary com.sun.rowset.providers.RIOptimisticProvider conformance: hierarchy and declared members via reflection.
 * @run main RIOptimisticProviderTest
 */

public class RIOptimisticProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.providers.RIOptimisticProvider", false, null);
        check(c.getName().equals("com.sun.rowset.providers.RIOptimisticProvider"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sql.rowset.spi.SyncProvider"), "superclass javax.sql.rowset.spi.SyncProvider");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getDataSourceLock"), "declares getDataSourceLock");
        check(ms.contains("getProviderGrade"), "declares getProviderGrade");
        check(ms.contains("getProviderID"), "declares getProviderID");
        check(ms.contains("getRowSetReader"), "declares getRowSetReader");
        check(ms.contains("getRowSetWriter"), "declares getRowSetWriter");
        check(ms.contains("getVendor"), "declares getVendor");
        check(ms.contains("getVersion"), "declares getVersion");
        check(ms.contains("readObject"), "declares readObject");
        check(ms.contains("setDataSourceLock"), "declares setDataSourceLock");
        check(ms.contains("supportsUpdatableView"), "declares supportsUpdatableView");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
