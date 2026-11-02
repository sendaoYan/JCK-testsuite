/*
 * @test
 * @summary jdk.jpackage.internal.log.LoggerRole conformance: internal enum reflection.
 * @modules jdk.jpackage/jdk.jpackage.internal.log:+open
 * @run main LoggerRoleTest
 */
public class LoggerRoleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jpackage.internal.log.LoggerRole", false, LoggerRoleTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == true, "isEnum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getDeclaredMethods().length == 5, "5 declared methods");
        check(c.getDeclaredFields().length == 9, "9 declared fields");
        java.util.Set<String> ec = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) { if (f.isEnumConstant()) ec.add(f.getName()); }
        check(ec.size() == 6, "6 enum constants");
        check(ec.contains("COMMAND_LOGGER"), "enum constant COMMAND_LOGGER");
        check(ec.contains("ERROR_LOGGER"), "enum constant ERROR_LOGGER");
        check(ec.contains("PROGRESS_LOGGER"), "enum constant PROGRESS_LOGGER");
        check(ec.contains("RESOURCE_LOGGER"), "enum constant RESOURCE_LOGGER");
        check(ec.contains("SUMMARY_LOGGER"), "enum constant SUMMARY_LOGGER");
        check(ec.contains("TRACE_LOGGER"), "enum constant TRACE_LOGGER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
