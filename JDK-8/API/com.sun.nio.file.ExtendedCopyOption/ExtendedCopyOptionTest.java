/*
 * @test
 * @summary com.sun.nio.file.ExtendedCopyOption conformance: hierarchy and declared members via reflection.
 * @run main ExtendedCopyOptionTest
 */

public class ExtendedCopyOptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.nio.file.ExtendedCopyOption", false, null);
        check(c.getName().equals("com.sun.nio.file.ExtendedCopyOption"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == true, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.nio.file.CopyOption"), "implements java.nio.file.CopyOption");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 1, "1 enum constants");
        java.util.Set<String> ec = new java.util.HashSet<String>();
        for (Object o : c.getEnumConstants()) ec.add(((java.lang.Enum<?>) o).name());
        check(ec.contains("INTERRUPTIBLE"), "enum constant INTERRUPTIBLE");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("valueOf"), "declares valueOf");
        check(ms.contains("values"), "declares values");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
