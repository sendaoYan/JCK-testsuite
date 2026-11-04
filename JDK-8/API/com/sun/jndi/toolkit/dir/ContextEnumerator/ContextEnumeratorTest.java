/*
 * @test
 * @summary com.sun.jndi.toolkit.dir.ContextEnumerator conformance: hierarchy and declared members via reflection.
 * @run main ContextEnumeratorTest
 */

public class ContextEnumeratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.dir.ContextEnumerator", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.dir.ContextEnumerator"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.NamingEnumeration"), "implements javax.naming.NamingEnumeration");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("close"), "declares close");
        check(ms.contains("getImmediateChildren"), "declares getImmediateChildren");
        check(ms.contains("getNextChild"), "declares getNextChild");
        check(ms.contains("getNextDescendant"), "declares getNextDescendant");
        check(ms.contains("hasMore"), "declares hasMore");
        check(ms.contains("hasMoreChildren"), "declares hasMoreChildren");
        check(ms.contains("hasMoreDescendants"), "declares hasMoreDescendants");
        check(ms.contains("hasMoreElements"), "declares hasMoreElements");
        check(ms.contains("newEnumerator"), "declares newEnumerator");
        check(ms.contains("next"), "declares next");
        check(ms.contains("nextElement"), "declares nextElement");
        check(ms.contains("prepNextChild"), "declares prepNextChild");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
