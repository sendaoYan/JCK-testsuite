/*
 * @test
 * @summary com.sun.tools.javac.comp.CompileStates.CompileState conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.comp:+open
 * @run main CompileStateTest
 */
public class CompileStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.comp.CompileStates$CompileState", false, CompileStateTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.comp.CompileStates$CompileState"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 12, "enum constants count");
        check(ec.contains("ATTR"), "enum constant ATTR");
        check(ec.contains("ENTER"), "enum constant ENTER");
        check(ec.contains("FLOW"), "enum constant FLOW");
        check(ec.contains("GENERATE"), "enum constant GENERATE");
        check(ec.contains("INIT"), "enum constant INIT");
        check(ec.contains("LOWER"), "enum constant LOWER");
        check(ec.contains("PARSE"), "enum constant PARSE");
        check(ec.contains("PROCESS"), "enum constant PROCESS");
        check(ec.contains("TRANSPATTERNS"), "enum constant TRANSPATTERNS");
        check(ec.contains("TRANSTYPES"), "enum constant TRANSTYPES");
        check(ec.contains("UNLAMBDA"), "enum constant UNLAMBDA");
        check(ec.contains("WARN"), "enum constant WARN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
