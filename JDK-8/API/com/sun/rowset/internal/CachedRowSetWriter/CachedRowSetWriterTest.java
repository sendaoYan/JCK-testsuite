/*
 * @test
 * @summary com.sun.rowset.internal.CachedRowSetWriter conformance: hierarchy and declared members via reflection.
 * @run main CachedRowSetWriterTest
 */

public class CachedRowSetWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.internal.CachedRowSetWriter", false, null);
        check(c.getName().equals("com.sun.rowset.internal.CachedRowSetWriter"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.sql.rowset.spi.TransactionalWriter"), "implements javax.sql.rowset.spi.TransactionalWriter");
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("buildKeyDesc"), "declares buildKeyDesc");
        check(ms.contains("buildTableName"), "declares buildTableName");
        check(ms.contains("buildWhereClause"), "declares buildWhereClause");
        check(ms.contains("commit"), "declares commit");
        check(ms.contains("deleteOriginalRow"), "declares deleteOriginalRow");
        check(ms.contains("getReader"), "declares getReader");
        check(ms.contains("initSQLStatements"), "declares initSQLStatements");
        check(ms.contains("insertNewRow"), "declares insertNewRow");
        check(ms.contains("isPKNameValid"), "declares isPKNameValid");
        check(ms.contains("readObject"), "declares readObject");
        check(ms.contains("rollback"), "declares rollback");
        check(ms.contains("setReader"), "declares setReader");
        check(ms.contains("updateOriginalRow"), "declares updateOriginalRow");
        check(ms.contains("updateResolvedConflictToDB"), "declares updateResolvedConflictToDB");
        check(ms.contains("writeData"), "declares writeData");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
