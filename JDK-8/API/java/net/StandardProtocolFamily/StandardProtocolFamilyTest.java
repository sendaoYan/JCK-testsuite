/*
 * @test
 * @summary java.net.StandardProtocolFamily conformance: enum constants and members.
 * @run main StandardProtocolFamilyTest
 */


public class StandardProtocolFamilyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.StandardProtocolFamily");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Enum.class, "enum superclass");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "enum constant count");
        check(((java.lang.Enum<?>) ec[0]).name().equals("INET"), "constant INET");
        check(((java.lang.Enum<?>) ec[1]).name().equals("INET6"), "constant INET6");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.net.ProtocolFamily");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");        System.out.println("PASS (" + passed + " assertions)");
    }
}
