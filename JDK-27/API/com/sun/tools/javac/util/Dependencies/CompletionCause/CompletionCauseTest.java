/*
 * @test
 * @summary com.sun.tools.javac.util.Dependencies.CompletionCause conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main CompletionCauseTest
 */
public class CompletionCauseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.Dependencies$CompletionCause", false, CompletionCauseTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.Dependencies$CompletionCause"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.tools.javac.util.GraphUtils$DependencyKind"), "interface com.sun.tools.javac.util.GraphUtils$DependencyKind");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 9, "enum constants count");
        check(ec.contains("CLASS_READER"), "enum constant CLASS_READER");
        check(ec.contains("HEADER_PHASE"), "enum constant HEADER_PHASE");
        check(ec.contains("HIERARCHY_PHASE"), "enum constant HIERARCHY_PHASE");
        check(ec.contains("IMPORTS_PHASE"), "enum constant IMPORTS_PHASE");
        check(ec.contains("MEMBERS_PHASE"), "enum constant MEMBERS_PHASE");
        check(ec.contains("MEMBER_ENTER"), "enum constant MEMBER_ENTER");
        check(ec.contains("OTHER"), "enum constant OTHER");
        check(ec.contains("PERMITS_PHASE"), "enum constant PERMITS_PHASE");
        check(ec.contains("RECORD_PHASE"), "enum constant RECORD_PHASE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
