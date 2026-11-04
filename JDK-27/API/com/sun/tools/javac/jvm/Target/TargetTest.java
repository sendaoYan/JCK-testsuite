/*
 * @test
 * @summary com.sun.tools.javac.jvm.Target conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.jvm:+open
 * @run main TargetTest
 */
public class TargetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.jvm.Target", false, TargetTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.jvm.Target"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 27, "enum constants count");
        check(ec.contains("JDK1_1"), "enum constant JDK1_1");
        check(ec.contains("JDK1_10"), "enum constant JDK1_10");
        check(ec.contains("JDK1_11"), "enum constant JDK1_11");
        check(ec.contains("JDK1_12"), "enum constant JDK1_12");
        check(ec.contains("JDK1_13"), "enum constant JDK1_13");
        check(ec.contains("JDK1_14"), "enum constant JDK1_14");
        check(ec.contains("JDK1_15"), "enum constant JDK1_15");
        check(ec.contains("JDK1_16"), "enum constant JDK1_16");
        check(ec.contains("JDK1_17"), "enum constant JDK1_17");
        check(ec.contains("JDK1_18"), "enum constant JDK1_18");
        check(ec.contains("JDK1_19"), "enum constant JDK1_19");
        check(ec.contains("JDK1_2"), "enum constant JDK1_2");
        check(ec.contains("JDK1_20"), "enum constant JDK1_20");
        check(ec.contains("JDK1_21"), "enum constant JDK1_21");
        check(ec.contains("JDK1_22"), "enum constant JDK1_22");
        check(ec.contains("JDK1_23"), "enum constant JDK1_23");
        check(ec.contains("JDK1_24"), "enum constant JDK1_24");
        check(ec.contains("JDK1_25"), "enum constant JDK1_25");
        check(ec.contains("JDK1_26"), "enum constant JDK1_26");
        check(ec.contains("JDK1_27"), "enum constant JDK1_27");
        check(ec.contains("JDK1_3"), "enum constant JDK1_3");
        check(ec.contains("JDK1_4"), "enum constant JDK1_4");
        check(ec.contains("JDK1_5"), "enum constant JDK1_5");
        check(ec.contains("JDK1_6"), "enum constant JDK1_6");
        check(ec.contains("JDK1_7"), "enum constant JDK1_7");
        check(ec.contains("JDK1_8"), "enum constant JDK1_8");
        check(ec.contains("JDK1_9"), "enum constant JDK1_9");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
