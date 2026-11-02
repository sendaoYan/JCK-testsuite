/*
 * @test
 * @summary com.sun.tools.javac.tree.JCTree.JCMemberReference.ReferenceKind conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.tree:+open
 * @run main ReferenceKindTest
 */
public class ReferenceKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.tree.JCTree$JCMemberReference$ReferenceKind", false, ReferenceKindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.tree.JCTree$JCMemberReference$ReferenceKind"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 7, "enum constants count");
        check(ec.contains("ARRAY_CTOR"), "enum constant ARRAY_CTOR");
        check(ec.contains("BOUND"), "enum constant BOUND");
        check(ec.contains("IMPLICIT_INNER"), "enum constant IMPLICIT_INNER");
        check(ec.contains("STATIC"), "enum constant STATIC");
        check(ec.contains("SUPER"), "enum constant SUPER");
        check(ec.contains("TOPLEVEL"), "enum constant TOPLEVEL");
        check(ec.contains("UNBOUND"), "enum constant UNBOUND");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
