/*
 * @test
 * @summary com.sun.security.jgss.InquireSecContextPermission conformance: hierarchy and declared members via reflection.
 * @run main InquireSecContextPermissionTest
 */

public class InquireSecContextPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.jgss.InquireSecContextPermission", false, null);
        check(c.getName().equals("com.sun.security.jgss.InquireSecContextPermission"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.BasicPermission"), "superclass java.security.BasicPermission");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());

        System.out.println("PASS (" + passed + " assertions)");
    }
}
