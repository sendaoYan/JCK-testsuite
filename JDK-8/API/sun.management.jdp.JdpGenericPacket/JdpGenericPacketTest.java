/*
 * @test
 * @summary sun.management.jdp.JdpGenericPacket conformance: abstract class and members.
 * @run main JdpGenericPacketTest
 */

public class JdpGenericPacketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.jdp.JdpGenericPacket", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.jdp.JdpGenericPacket"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.management.jdp.JdpPacket"), "interface sun.management.jdp.JdpPacket");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("checkMagic"), "declares method checkMagic");
        check(mnames.contains("checkVersion"), "declares method checkVersion");
        check(mnames.contains("getMagic"), "declares method getMagic");
        check(mnames.contains("getVersion"), "declares method getVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
