/*
 * @test
 * @summary com.sun.tools.javac.code.Types.MostSpecificReturnCheck conformance: internal enum reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main MostSpecificReturnCheckTest
 */
public class MostSpecificReturnCheckTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Types$MostSpecificReturnCheck", false, MostSpecificReturnCheckTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Types$MostSpecificReturnCheck"), "name");
        check(c.isEnum(), "enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) ec.add(f.getName());
        check(ec.size() == 2, "enum constants count");
        check(ec.contains("BASIC"), "enum constant BASIC");
        check(ec.contains("RTS"), "enum constant RTS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
