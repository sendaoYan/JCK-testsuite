/*
 * @test
 * @summary com.sun.security.ntlm.Server conformance: hierarchy and declared members via reflection.
 * @run main ServerTest
 */

public class ServerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.ntlm.Server", false, null);
        check(c.getName().equals("com.sun.security.ntlm.Server"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.security.ntlm.NTLM"), "superclass com.sun.security.ntlm.NTLM");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("debug"), "declares debug");
        check(ms.contains("getPassword"), "declares getPassword");
        check(ms.contains("type2"), "declares type2");
        check(ms.contains("verify"), "declares verify");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
