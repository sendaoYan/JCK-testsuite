/*
 * @test
 * @summary com.sun.security.auth.module.UnixSystem conformance: hierarchy and declared members via reflection.
 * @run main UnixSystemTest
 */

public class UnixSystemTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.auth.module.UnixSystem", false, null);
        check(c.getName().equals("com.sun.security.auth.module.UnixSystem"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getGid"), "declares getGid");
        check(ms.contains("getGroups"), "declares getGroups");
        check(ms.contains("getUid"), "declares getUid");
        check(ms.contains("getUnixInfo"), "declares getUnixInfo");
        check(ms.contains("getUsername"), "declares getUsername");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
