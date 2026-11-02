/*
 * @test
 * @summary jdk.javadoc.internal.doclets.toolkit.util.DocFileIOException.Mode conformance: internal enum reflection.
 * @modules jdk.javadoc/jdk.javadoc.internal.doclets.toolkit.util:+open
 * @run main ModeTest
 */
public class ModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.internal.doclets.toolkit.util.DocFileIOException$Mode", false, ModeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 3, "3 declared methods");
        check(c.getDeclaredFields().length == 3, "3 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 2, "2 enum constants");
        check(ec.contains("READ"), "enum constant READ");
        check(ec.contains("WRITE"), "enum constant WRITE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
