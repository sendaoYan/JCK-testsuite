/*
 * @test
 * @summary com.sun.tools.javac.code.TypeTag conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main TypeTagTest
 */
public class TypeTagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.TypeTag", false, TypeTagTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.TypeTag"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 24, "enum constants count");
        check(ec.contains("ARRAY"), "enum constant ARRAY");
        check(ec.contains("BOOLEAN"), "enum constant BOOLEAN");
        check(ec.contains("BOT"), "enum constant BOT");
        check(ec.contains("BYTE"), "enum constant BYTE");
        check(ec.contains("CHAR"), "enum constant CHAR");
        check(ec.contains("CLASS"), "enum constant CLASS");
        check(ec.contains("DEFERRED"), "enum constant DEFERRED");
        check(ec.contains("DOUBLE"), "enum constant DOUBLE");
        check(ec.contains("ERROR"), "enum constant ERROR");
        check(ec.contains("FLOAT"), "enum constant FLOAT");
        check(ec.contains("FORALL"), "enum constant FORALL");
        check(ec.contains("INT"), "enum constant INT");
        check(ec.contains("LONG"), "enum constant LONG");
        check(ec.contains("METHOD"), "enum constant METHOD");
        check(ec.contains("MODULE"), "enum constant MODULE");
        check(ec.contains("NONE"), "enum constant NONE");
        check(ec.contains("PACKAGE"), "enum constant PACKAGE");
        check(ec.contains("SHORT"), "enum constant SHORT");
        check(ec.contains("TYPEVAR"), "enum constant TYPEVAR");
        check(ec.contains("UNDETVAR"), "enum constant UNDETVAR");
        check(ec.contains("UNINITIALIZED_OBJECT"), "enum constant UNINITIALIZED_OBJECT");
        check(ec.contains("UNINITIALIZED_THIS"), "enum constant UNINITIALIZED_THIS");
        check(ec.contains("VOID"), "enum constant VOID");
        check(ec.contains("WILDCARD"), "enum constant WILDCARD");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
