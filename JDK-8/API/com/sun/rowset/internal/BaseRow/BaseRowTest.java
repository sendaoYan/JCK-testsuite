/*
 * @test
 * @summary com.sun.rowset.internal.BaseRow conformance: hierarchy and declared members via reflection.
 * @run main BaseRowTest
 */

public class BaseRowTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.internal.BaseRow", false, null);
        check(c.getName().equals("com.sun.rowset.internal.BaseRow"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        check(in.contains("java.lang.Cloneable"), "implements java.lang.Cloneable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getColumnObject"), "declares getColumnObject");
        check(ms.contains("getOrigRow"), "declares getOrigRow");
        check(ms.contains("setColumnObject"), "declares setColumnObject");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
