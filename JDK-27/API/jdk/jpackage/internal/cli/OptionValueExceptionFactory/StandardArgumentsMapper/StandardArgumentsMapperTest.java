/*
 * @test
 * @summary jdk.jpackage.internal.cli.OptionValueExceptionFactory.StandardArgumentsMapper conformance: internal enum reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.cli:+open
 * @run main StandardArgumentsMapperTest
 */
public class StandardArgumentsMapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.cli.OptionValueExceptionFactory$StandardArgumentsMapper", false, StandardArgumentsMapperTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 1, "1 direct interfaces");
        check(c.getDeclaredMethods().length == 8, "8 declared methods");
        check(c.getDeclaredFields().length == 6, "6 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 4, "4 enum constants");
        check(ec.contains("NAME_AND_VALUE"), "enum constant NAME_AND_VALUE");
        check(ec.contains("NONE"), "enum constant NONE");
        check(ec.contains("VALUE"), "enum constant VALUE");
        check(ec.contains("VALUE_AND_NAME"), "enum constant VALUE_AND_NAME");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
