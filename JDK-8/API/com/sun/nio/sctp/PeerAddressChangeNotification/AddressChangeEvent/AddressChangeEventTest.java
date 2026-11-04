/*
 * @test
 * @summary com.sun.nio.sctp.PeerAddressChangeNotification.AddressChangeEvent conformance: hierarchy and declared members via reflection.
 * @run main AddressChangeEventTest
 */

public class AddressChangeEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.nio.sctp.PeerAddressChangeNotification$AddressChangeEvent", false, null);
        check(c.getName().equals("com.sun.nio.sctp.PeerAddressChangeNotification$AddressChangeEvent"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == true, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 6, "6 enum constants");
        java.util.Set<String> ec = new java.util.HashSet<String>();
        for (Object o : c.getEnumConstants()) ec.add(((java.lang.Enum<?>) o).name());
        check(ec.contains("ADDR_AVAILABLE"), "enum constant ADDR_AVAILABLE");
        check(ec.contains("ADDR_UNREACHABLE"), "enum constant ADDR_UNREACHABLE");
        check(ec.contains("ADDR_REMOVED"), "enum constant ADDR_REMOVED");
        check(ec.contains("ADDR_ADDED"), "enum constant ADDR_ADDED");
        check(ec.contains("ADDR_MADE_PRIMARY"), "enum constant ADDR_MADE_PRIMARY");
        check(ec.contains("ADDR_CONFIRMED"), "enum constant ADDR_CONFIRMED");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("valueOf"), "declares valueOf");
        check(ms.contains("values"), "declares values");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
