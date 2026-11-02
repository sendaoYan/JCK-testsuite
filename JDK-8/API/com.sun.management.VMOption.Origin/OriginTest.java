/*
 * @test
 * @summary com.sun.management.VMOption.Origin conformance: hierarchy and declared members via reflection.
 * @run main OriginTest
 */

public class OriginTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.management.VMOption$Origin", false, null);
        check(c.getName().equals("com.sun.management.VMOption$Origin"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == true, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 7, "7 enum constants");
        java.util.Set<String> ec = new java.util.HashSet<String>();
        for (Object o : c.getEnumConstants()) ec.add(((java.lang.Enum<?>) o).name());
        check(ec.contains("DEFAULT"), "enum constant DEFAULT");
        check(ec.contains("VM_CREATION"), "enum constant VM_CREATION");
        check(ec.contains("ENVIRON_VAR"), "enum constant ENVIRON_VAR");
        check(ec.contains("CONFIG_FILE"), "enum constant CONFIG_FILE");
        check(ec.contains("MANAGEMENT"), "enum constant MANAGEMENT");
        check(ec.contains("ERGONOMIC"), "enum constant ERGONOMIC");
        check(ec.contains("OTHER"), "enum constant OTHER");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("valueOf"), "declares valueOf");
        check(ms.contains("values"), "declares values");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
