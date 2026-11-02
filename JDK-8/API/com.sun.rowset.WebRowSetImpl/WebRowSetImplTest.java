/*
 * @test
 * @summary com.sun.rowset.WebRowSetImpl conformance: hierarchy and declared members via reflection.
 * @run main WebRowSetImplTest
 */

public class WebRowSetImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.WebRowSetImpl", false, null);
        check(c.getName().equals("com.sun.rowset.WebRowSetImpl"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.rowset.CachedRowSetImpl"), "superclass com.sun.rowset.CachedRowSetImpl");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.sql.rowset.WebRowSet"), "implements javax.sql.rowset.WebRowSet");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("readObject"), "declares readObject");
        check(ms.contains("readXml"), "declares readXml");
        check(ms.contains("writeXml"), "declares writeXml");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
