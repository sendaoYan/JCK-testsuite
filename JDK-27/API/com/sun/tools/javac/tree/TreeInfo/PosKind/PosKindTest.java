/*
 * @test
 * @summary com.sun.tools.javac.tree.TreeInfo.PosKind conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.tree:+open
 * @run main PosKindTest
 */
public class PosKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.tree.TreeInfo$PosKind", false, PosKindTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.tree.TreeInfo$PosKind"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 3, "enum constants count");
        check(ec.contains("END_POS"), "enum constant END_POS");
        check(ec.contains("FIRST_STAT_POS"), "enum constant FIRST_STAT_POS");
        check(ec.contains("START_POS"), "enum constant START_POS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
