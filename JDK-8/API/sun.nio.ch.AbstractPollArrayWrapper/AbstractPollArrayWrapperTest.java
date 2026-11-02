/*
 * @test
 * @summary sun.nio.ch.AbstractPollArrayWrapper conformance: abstract class and members.
 * @run main AbstractPollArrayWrapperTest
 */

public class AbstractPollArrayWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.AbstractPollArrayWrapper", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.AbstractPollArrayWrapper"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getDescriptor"), "declares method getDescriptor");
        check(mnames.contains("getEventOps"), "declares method getEventOps");
        check(mnames.contains("getReventOps"), "declares method getReventOps");
        check(mnames.contains("putDescriptor"), "declares method putDescriptor");
        check(mnames.contains("putEventOps"), "declares method putEventOps");
        check(mnames.contains("putReventOps"), "declares method putReventOps");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
