/*
 * @test
 * @summary com.sun.naming.internal.VersionHelper conformance: hierarchy and declared members via reflection.
 * @run main VersionHelperTest
 */

public class VersionHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.naming.internal.VersionHelper", false, null);
        check(c.getName().equals("com.sun.naming.internal.VersionHelper"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getContextClassLoader"), "declares getContextClassLoader");
        check(ms.contains("getJavaHomeLibStream"), "declares getJavaHomeLibStream");
        check(ms.contains("getJndiProperties"), "declares getJndiProperties");
        check(ms.contains("getJndiProperty"), "declares getJndiProperty");
        check(ms.contains("getResourceAsStream"), "declares getResourceAsStream");
        check(ms.contains("getResources"), "declares getResources");
        check(ms.contains("getUrlArray"), "declares getUrlArray");
        check(ms.contains("getVersionHelper"), "declares getVersionHelper");
        check(ms.contains("loadClass"), "declares loadClass");
        check(ms.contains("loadClassWithoutInit"), "declares loadClassWithoutInit");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
