/*
 * @test
 * @summary com.sun.tracing.dtrace.StabilityLevel conformance: hierarchy and declared members via reflection.
 * @run main StabilityLevelTest
 */

public class StabilityLevelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tracing.dtrace.StabilityLevel", false, null);
        check(c.getName().equals("com.sun.tracing.dtrace.StabilityLevel"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == true, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 8, "8 enum constants");
        java.util.Set<String> ec = new java.util.HashSet<String>();
        for (Object o : c.getEnumConstants()) ec.add(((java.lang.Enum<?>) o).name());
        check(ec.contains("INTERNAL"), "enum constant INTERNAL");
        check(ec.contains("PRIVATE"), "enum constant PRIVATE");
        check(ec.contains("OBSOLETE"), "enum constant OBSOLETE");
        check(ec.contains("EXTERNAL"), "enum constant EXTERNAL");
        check(ec.contains("UNSTABLE"), "enum constant UNSTABLE");
        check(ec.contains("EVOLVING"), "enum constant EVOLVING");
        check(ec.contains("STABLE"), "enum constant STABLE");
        check(ec.contains("STANDARD"), "enum constant STANDARD");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getEncoding"), "declares getEncoding");
        check(ms.contains("toDisplayString"), "declares toDisplayString");
        check(ms.contains("valueOf"), "declares valueOf");
        check(ms.contains("values"), "declares values");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
