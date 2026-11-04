/*
 * @test
 * @summary com.sun.tools.javac.code.Symbol.OperatorSymbol.AccessCode conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main AccessCodeTest
 */
public class AccessCodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Symbol$OperatorSymbol$AccessCode", false, AccessCodeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Symbol$OperatorSymbol$AccessCode"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 8, "enum constants count");
        check(ec.contains("ASSIGN"), "enum constant ASSIGN");
        check(ec.contains("DEREF"), "enum constant DEREF");
        check(ec.contains("FIRSTASGOP"), "enum constant FIRSTASGOP");
        check(ec.contains("POSTDEC"), "enum constant POSTDEC");
        check(ec.contains("POSTINC"), "enum constant POSTINC");
        check(ec.contains("PREDEC"), "enum constant PREDEC");
        check(ec.contains("PREINC"), "enum constant PREINC");
        check(ec.contains("UNKNOWN"), "enum constant UNKNOWN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
