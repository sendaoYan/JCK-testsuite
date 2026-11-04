/*
 * @test
 * @summary com.sun.nio.sctp.SctpServerChannel conformance: hierarchy and declared members via reflection.
 * @run main SctpServerChannelTest
 */

public class SctpServerChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.nio.sctp.SctpServerChannel", false, null);
        check(c.getName().equals("com.sun.nio.sctp.SctpServerChannel"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.spi.AbstractSelectableChannel"), "superclass java.nio.channels.spi.AbstractSelectableChannel");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("accept"), "declares accept");
        check(ms.contains("bind"), "declares bind");
        check(ms.contains("bindAddress"), "declares bindAddress");
        check(ms.contains("getAllLocalAddresses"), "declares getAllLocalAddresses");
        check(ms.contains("getOption"), "declares getOption");
        check(ms.contains("open"), "declares open");
        check(ms.contains("setOption"), "declares setOption");
        check(ms.contains("supportedOptions"), "declares supportedOptions");
        check(ms.contains("unbindAddress"), "declares unbindAddress");
        check(ms.contains("validOps"), "declares validOps");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
