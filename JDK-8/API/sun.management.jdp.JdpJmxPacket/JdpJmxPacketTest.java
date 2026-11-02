/*
 * @test
 * @summary sun.management.jdp.JdpJmxPacket conformance: class and members.
 * @run main JdpJmxPacketTest
 */

public class JdpJmxPacketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.jdp.JdpJmxPacket", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.jdp.JdpJmxPacket"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.jdp.JdpGenericPacket"), "superclass sun.management.jdp.JdpGenericPacket");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.management.jdp.JdpPacket"), "interface sun.management.jdp.JdpPacket");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("getBroadcastInterval"), "declares method getBroadcastInterval");
        check(mnames.contains("getId"), "declares method getId");
        check(mnames.contains("getInstanceName"), "declares method getInstanceName");
        check(mnames.contains("getJmxServiceUrl"), "declares method getJmxServiceUrl");
        check(mnames.contains("getMainClass"), "declares method getMainClass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
