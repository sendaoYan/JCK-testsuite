/*
 * @test
 * @summary java.net.Authenticator conformance: abstract class hierarchy and members.
 * @run main AuthenticatorTest
 */


public class AuthenticatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.Authenticator");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("requestPasswordAuthentication"), "has method requestPasswordAuthentication");
        check(names.contains("setDefault"), "has method setDefault");        System.out.println("PASS (" + passed + " assertions)");
    }
}
