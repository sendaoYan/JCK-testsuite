/*
 * @test
 * @summary com.sun.nio.file.SensitivityWatchEventModifier conformance: hierarchy and declared members via reflection.
 * @run main SensitivityWatchEventModifierTest
 */

public class SensitivityWatchEventModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.nio.file.SensitivityWatchEventModifier", false, null);
        check(c.getName().equals("com.sun.nio.file.SensitivityWatchEventModifier"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == true, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.nio.file.WatchEvent$Modifier"), "implements java.nio.file.WatchEvent$Modifier");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 3, "3 enum constants");
        java.util.Set<String> ec = new java.util.HashSet<String>();
        for (Object o : c.getEnumConstants()) ec.add(((java.lang.Enum<?>) o).name());
        check(ec.contains("HIGH"), "enum constant HIGH");
        check(ec.contains("MEDIUM"), "enum constant MEDIUM");
        check(ec.contains("LOW"), "enum constant LOW");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("sensitivityValueInSeconds"), "declares sensitivityValueInSeconds");
        check(ms.contains("valueOf"), "declares valueOf");
        check(ms.contains("values"), "declares values");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
