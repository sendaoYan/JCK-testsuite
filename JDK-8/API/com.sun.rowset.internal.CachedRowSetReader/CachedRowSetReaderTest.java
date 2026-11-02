/*
 * @test
 * @summary com.sun.rowset.internal.CachedRowSetReader conformance: hierarchy and declared members via reflection.
 * @run main CachedRowSetReaderTest
 */

public class CachedRowSetReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.internal.CachedRowSetReader", false, null);
        check(c.getName().equals("com.sun.rowset.internal.CachedRowSetReader"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.sql.RowSetReader"), "implements javax.sql.RowSetReader");
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("connect"), "declares connect");
        check(ms.contains("decodeParams"), "declares decodeParams");
        check(ms.contains("getCloseConnection"), "declares getCloseConnection");
        check(ms.contains("readData"), "declares readData");
        check(ms.contains("readObject"), "declares readObject");
        check(ms.contains("reset"), "declares reset");
        check(ms.contains("setStartPosition"), "declares setStartPosition");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
