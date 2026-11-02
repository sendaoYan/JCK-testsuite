/*
 * @test
 * @summary jdk.jpackage.internal.util.PListReader.Raw.Type conformance: internal enum reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.util:+open
 * @run main TypeTest
 */
public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.util.PListReader$Raw$Type", false, TypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 4, "4 declared methods");
        check(c.getDeclaredFields().length == 7, "7 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 6, "6 enum constants");
        check(ec.contains("BOOLEAN"), "enum constant BOOLEAN");
        check(ec.contains("DATA"), "enum constant DATA");
        check(ec.contains("DATE"), "enum constant DATE");
        check(ec.contains("INTEGER"), "enum constant INTEGER");
        check(ec.contains("REAL"), "enum constant REAL");
        check(ec.contains("STRING"), "enum constant STRING");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
