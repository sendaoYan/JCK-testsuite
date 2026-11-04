/*
 * @test
 * @summary com.sun.tools.javac.code.Kinds.Kind conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main KindTest
 */
public class KindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Kinds$Kind", false, KindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Kinds$Kind"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 17, "enum constants count");
        check(ec.contains("ABSENT_MTH"), "enum constant ABSENT_MTH");
        check(ec.contains("ABSENT_TYP"), "enum constant ABSENT_TYP");
        check(ec.contains("ABSENT_VAR"), "enum constant ABSENT_VAR");
        check(ec.contains("AMBIGUOUS"), "enum constant AMBIGUOUS");
        check(ec.contains("BAD_RESTRICTED_TYPE"), "enum constant BAD_RESTRICTED_TYPE");
        check(ec.contains("ERR"), "enum constant ERR");
        check(ec.contains("HIDDEN"), "enum constant HIDDEN");
        check(ec.contains("MDL"), "enum constant MDL");
        check(ec.contains("MTH"), "enum constant MTH");
        check(ec.contains("NIL"), "enum constant NIL");
        check(ec.contains("PCK"), "enum constant PCK");
        check(ec.contains("POLY"), "enum constant POLY");
        check(ec.contains("STATICERR"), "enum constant STATICERR");
        check(ec.contains("TYP"), "enum constant TYP");
        check(ec.contains("VAR"), "enum constant VAR");
        check(ec.contains("WRONG_MTH"), "enum constant WRONG_MTH");
        check(ec.contains("WRONG_MTHS"), "enum constant WRONG_MTHS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
