/*
 * @test
 * @summary com.sun.tools.javac.parser.Tokens.Comment.CommentStyle conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.parser:+open
 * @run main CommentStyleTest
 */
public class CommentStyleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.parser.Tokens$Comment$CommentStyle", false, CommentStyleTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.parser.Tokens$Comment$CommentStyle"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 4, "enum constants count");
        check(ec.contains("BLOCK"), "enum constant BLOCK");
        check(ec.contains("JAVADOC_BLOCK"), "enum constant JAVADOC_BLOCK");
        check(ec.contains("JAVADOC_LINE"), "enum constant JAVADOC_LINE");
        check(ec.contains("LINE"), "enum constant LINE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
