/*
 * @test
 * @summary com.sun.tools.javac.jvm.ClassFile.Version conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.jvm:+open
 * @run main VersionTest
 */
public class VersionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.jvm.ClassFile$Version", false, VersionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.jvm.ClassFile$Version"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 25, "enum constants count");
        check(ec.contains("V45_3"), "enum constant V45_3");
        check(ec.contains("V48"), "enum constant V48");
        check(ec.contains("V49"), "enum constant V49");
        check(ec.contains("V50"), "enum constant V50");
        check(ec.contains("V51"), "enum constant V51");
        check(ec.contains("V52"), "enum constant V52");
        check(ec.contains("V53"), "enum constant V53");
        check(ec.contains("V54"), "enum constant V54");
        check(ec.contains("V55"), "enum constant V55");
        check(ec.contains("V56"), "enum constant V56");
        check(ec.contains("V57"), "enum constant V57");
        check(ec.contains("V58"), "enum constant V58");
        check(ec.contains("V59"), "enum constant V59");
        check(ec.contains("V60"), "enum constant V60");
        check(ec.contains("V61"), "enum constant V61");
        check(ec.contains("V62"), "enum constant V62");
        check(ec.contains("V63"), "enum constant V63");
        check(ec.contains("V64"), "enum constant V64");
        check(ec.contains("V65"), "enum constant V65");
        check(ec.contains("V66"), "enum constant V66");
        check(ec.contains("V67"), "enum constant V67");
        check(ec.contains("V68"), "enum constant V68");
        check(ec.contains("V69"), "enum constant V69");
        check(ec.contains("V70"), "enum constant V70");
        check(ec.contains("V71"), "enum constant V71");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
