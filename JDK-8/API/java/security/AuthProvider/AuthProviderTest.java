/*
 * @test
 * @summary java.security.AuthProvider conformance: abstract class hierarchy and key methods.
 * @run main AuthProviderTest
 */

public class AuthProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.AuthProvider");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.security.Provider"), "extends java.security.Provider");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("login"), "declares login");
        check(ms.contains("logout"), "declares logout");
        check(ms.contains("setCallbackHandler"), "declares setCallbackHandler");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
