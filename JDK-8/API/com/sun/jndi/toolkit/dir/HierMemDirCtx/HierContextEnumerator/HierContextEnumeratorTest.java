/*
 * @test
 * @summary com.sun.jndi.toolkit.dir.HierMemDirCtx.HierContextEnumerator conformance: hierarchy and declared members via reflection.
 * @run main HierContextEnumeratorTest
 */

public class HierContextEnumeratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.dir.HierMemDirCtx$HierContextEnumerator", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.dir.HierMemDirCtx$HierContextEnumerator"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jndi.toolkit.dir.ContextEnumerator"), "superclass com.sun.jndi.toolkit.dir.ContextEnumerator");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getImmediateChildren"), "declares getImmediateChildren");
        check(ms.contains("newEnumerator"), "declares newEnumerator");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
