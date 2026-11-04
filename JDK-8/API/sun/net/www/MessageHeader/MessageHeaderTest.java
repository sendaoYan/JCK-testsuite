/*
 * @test
 * @summary sun.net.www.MessageHeader conformance: class and members.
 * @run main MessageHeaderTest
 */

public class MessageHeaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.MessageHeader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.www.MessageHeader"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("add"), "declares method add");
        check(mnames.contains("canonicalID"), "declares method canonicalID");
        check(mnames.contains("filterAndAddHeaders"), "declares method filterAndAddHeaders");
        check(mnames.contains("filterNTLMResponses"), "declares method filterNTLMResponses");
        check(mnames.contains("findNextValue"), "declares method findNextValue");
        check(mnames.contains("findValue"), "declares method findValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
