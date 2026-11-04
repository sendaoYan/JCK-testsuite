/*
 * @test
 * @summary sun.nio.ch.PollArrayWrapper conformance: class and members.
 * @run main PollArrayWrapperTest
 */

public class PollArrayWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.PollArrayWrapper", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.PollArrayWrapper"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.ch.AbstractPollArrayWrapper"), "superclass sun.nio.ch.AbstractPollArrayWrapper");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addEntry"), "declares method addEntry");
        check(mnames.contains("free"), "declares method free");
        check(mnames.contains("grow"), "declares method grow");
        check(mnames.contains("initInterrupt"), "declares method initInterrupt");
        check(mnames.contains("interrupt"), "declares method interrupt");
        check(mnames.contains("poll"), "declares method poll");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
