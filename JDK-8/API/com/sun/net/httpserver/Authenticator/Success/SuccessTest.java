/*
 * @test
 * @summary com.sun.net.httpserver.Authenticator.Success conformance: hierarchy and declared members via reflection.
 * @run main SuccessTest
 */

public class SuccessTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.Authenticator$Success", false, null);
        check(c.getName().equals("com.sun.net.httpserver.Authenticator$Success"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.net.httpserver.Authenticator$Result"), "superclass com.sun.net.httpserver.Authenticator$Result");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getPrincipal"), "declares getPrincipal");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
