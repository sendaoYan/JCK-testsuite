/*
 * @test
 * @summary sun.net.www.protocol.http.Negotiator conformance: abstract class and members.
 * @run main NegotiatorTest
 */

public class NegotiatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.protocol.http.Negotiator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.protocol.http.Negotiator"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("finest"), "declares method finest");
        check(mnames.contains("firstToken"), "declares method firstToken");
        check(mnames.contains("getNegotiator"), "declares method getNegotiator");
        check(mnames.contains("nextToken"), "declares method nextToken");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
