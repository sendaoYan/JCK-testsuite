/*
 * @test
 * @summary sun.management.jdp.JdpPacketReader conformance: class and members.
 * @run main JdpPacketReaderTest
 */

public class JdpPacketReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.jdp.JdpPacketReader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.jdp.JdpPacketReader"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getDiscoveryDataAsMap"), "declares method getDiscoveryDataAsMap");
        check(mnames.contains("getEntry"), "declares method getEntry");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
