/*
 * @test
 * @summary com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent conformance: hierarchy and declared members via reflection.
 * @run main AssocChangeEventTest
 */

public class AssocChangeEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.nio.sctp.AssociationChangeNotification$AssocChangeEvent", false, null);
        check(c.getName().equals("com.sun.nio.sctp.AssociationChangeNotification$AssocChangeEvent"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == true, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 5, "5 enum constants");
        java.util.Set<String> ec = new java.util.HashSet<String>();
        for (Object o : c.getEnumConstants()) ec.add(((java.lang.Enum<?>) o).name());
        check(ec.contains("COMM_UP"), "enum constant COMM_UP");
        check(ec.contains("COMM_LOST"), "enum constant COMM_LOST");
        check(ec.contains("RESTART"), "enum constant RESTART");
        check(ec.contains("SHUTDOWN"), "enum constant SHUTDOWN");
        check(ec.contains("CANT_START"), "enum constant CANT_START");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("valueOf"), "declares valueOf");
        check(ms.contains("values"), "declares values");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
