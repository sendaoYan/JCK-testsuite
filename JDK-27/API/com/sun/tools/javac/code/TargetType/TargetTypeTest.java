/*
 * @test
 * @summary com.sun.tools.javac.code.TargetType conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main TargetTypeTest
 */
public class TargetTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.TargetType", false, TargetTypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.TargetType"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 23, "enum constants count");
        check(ec.contains("CAST"), "enum constant CAST");
        check(ec.contains("CLASS_EXTENDS"), "enum constant CLASS_EXTENDS");
        check(ec.contains("CLASS_TYPE_PARAMETER"), "enum constant CLASS_TYPE_PARAMETER");
        check(ec.contains("CLASS_TYPE_PARAMETER_BOUND"), "enum constant CLASS_TYPE_PARAMETER_BOUND");
        check(ec.contains("CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT"), "enum constant CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT");
        check(ec.contains("CONSTRUCTOR_REFERENCE"), "enum constant CONSTRUCTOR_REFERENCE");
        check(ec.contains("CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT"), "enum constant CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT");
        check(ec.contains("EXCEPTION_PARAMETER"), "enum constant EXCEPTION_PARAMETER");
        check(ec.contains("FIELD"), "enum constant FIELD");
        check(ec.contains("INSTANCEOF"), "enum constant INSTANCEOF");
        check(ec.contains("LOCAL_VARIABLE"), "enum constant LOCAL_VARIABLE");
        check(ec.contains("METHOD_FORMAL_PARAMETER"), "enum constant METHOD_FORMAL_PARAMETER");
        check(ec.contains("METHOD_INVOCATION_TYPE_ARGUMENT"), "enum constant METHOD_INVOCATION_TYPE_ARGUMENT");
        check(ec.contains("METHOD_RECEIVER"), "enum constant METHOD_RECEIVER");
        check(ec.contains("METHOD_REFERENCE"), "enum constant METHOD_REFERENCE");
        check(ec.contains("METHOD_REFERENCE_TYPE_ARGUMENT"), "enum constant METHOD_REFERENCE_TYPE_ARGUMENT");
        check(ec.contains("METHOD_RETURN"), "enum constant METHOD_RETURN");
        check(ec.contains("METHOD_TYPE_PARAMETER"), "enum constant METHOD_TYPE_PARAMETER");
        check(ec.contains("METHOD_TYPE_PARAMETER_BOUND"), "enum constant METHOD_TYPE_PARAMETER_BOUND");
        check(ec.contains("NEW"), "enum constant NEW");
        check(ec.contains("RESOURCE_VARIABLE"), "enum constant RESOURCE_VARIABLE");
        check(ec.contains("THROWS"), "enum constant THROWS");
        check(ec.contains("UNKNOWN"), "enum constant UNKNOWN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
