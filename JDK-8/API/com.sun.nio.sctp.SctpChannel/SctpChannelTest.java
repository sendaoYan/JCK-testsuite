/*
 * @test
 * @summary com.sun.nio.sctp.SctpChannel conformance: hierarchy and declared members via reflection.
 * @run main SctpChannelTest
 */

public class SctpChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.nio.sctp.SctpChannel", false, null);
        check(c.getName().equals("com.sun.nio.sctp.SctpChannel"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.spi.AbstractSelectableChannel"), "superclass java.nio.channels.spi.AbstractSelectableChannel");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("association"), "declares association");
        check(ms.contains("bind"), "declares bind");
        check(ms.contains("bindAddress"), "declares bindAddress");
        check(ms.contains("connect"), "declares connect");
        check(ms.contains("finishConnect"), "declares finishConnect");
        check(ms.contains("getAllLocalAddresses"), "declares getAllLocalAddresses");
        check(ms.contains("getOption"), "declares getOption");
        check(ms.contains("getRemoteAddresses"), "declares getRemoteAddresses");
        check(ms.contains("isConnectionPending"), "declares isConnectionPending");
        check(ms.contains("open"), "declares open");
        check(ms.contains("receive"), "declares receive");
        check(ms.contains("send"), "declares send");
        check(ms.contains("setOption"), "declares setOption");
        check(ms.contains("shutdown"), "declares shutdown");
        check(ms.contains("supportedOptions"), "declares supportedOptions");
        check(ms.contains("unbindAddress"), "declares unbindAddress");
        check(ms.contains("validOps"), "declares validOps");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
