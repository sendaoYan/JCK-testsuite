/*
 * @test
 * @summary com.sun.security.ntlm.Client conformance: hierarchy and declared members via reflection.
 * @run main ClientTest
 */

public class ClientTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.ntlm.Client", false, null);
        check(c.getName().equals("com.sun.security.ntlm.Client"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.security.ntlm.NTLM"), "superclass com.sun.security.ntlm.NTLM");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("debug"), "declares debug");
        check(ms.contains("dispose"), "declares dispose");
        check(ms.contains("getDomain"), "declares getDomain");
        check(ms.contains("type1"), "declares type1");
        check(ms.contains("type3"), "declares type3");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
