/*
 * @test
 * @summary com.sun.tools.javac.code.Source conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main SourceTest
 */
public class SourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Source", false, SourceTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Source"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 26, "enum constants count");
        check(ec.contains("JDK10"), "enum constant JDK10");
        check(ec.contains("JDK11"), "enum constant JDK11");
        check(ec.contains("JDK12"), "enum constant JDK12");
        check(ec.contains("JDK13"), "enum constant JDK13");
        check(ec.contains("JDK14"), "enum constant JDK14");
        check(ec.contains("JDK15"), "enum constant JDK15");
        check(ec.contains("JDK16"), "enum constant JDK16");
        check(ec.contains("JDK17"), "enum constant JDK17");
        check(ec.contains("JDK18"), "enum constant JDK18");
        check(ec.contains("JDK19"), "enum constant JDK19");
        check(ec.contains("JDK1_2"), "enum constant JDK1_2");
        check(ec.contains("JDK1_3"), "enum constant JDK1_3");
        check(ec.contains("JDK1_4"), "enum constant JDK1_4");
        check(ec.contains("JDK20"), "enum constant JDK20");
        check(ec.contains("JDK21"), "enum constant JDK21");
        check(ec.contains("JDK22"), "enum constant JDK22");
        check(ec.contains("JDK23"), "enum constant JDK23");
        check(ec.contains("JDK24"), "enum constant JDK24");
        check(ec.contains("JDK25"), "enum constant JDK25");
        check(ec.contains("JDK26"), "enum constant JDK26");
        check(ec.contains("JDK27"), "enum constant JDK27");
        check(ec.contains("JDK5"), "enum constant JDK5");
        check(ec.contains("JDK6"), "enum constant JDK6");
        check(ec.contains("JDK7"), "enum constant JDK7");
        check(ec.contains("JDK8"), "enum constant JDK8");
        check(ec.contains("JDK9"), "enum constant JDK9");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
