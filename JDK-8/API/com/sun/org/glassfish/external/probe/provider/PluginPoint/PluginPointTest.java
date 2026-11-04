/*
 * @test
 * @summary com.sun.org.glassfish.external.probe.provider.PluginPoint conformance: reflection.
 * @run main PluginPointTest
 */

public class PluginPointTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.probe.provider.PluginPoint", false, PluginPointTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("PluginPoint"), "simple name PluginPoint");
        check(c.isEnum(), "is enum");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "values"), "method values");
        check(has(c, "valueOf"), "method valueOf");
        check(has(c, "getName"), "method getName");
        check(has(c, "getPath"), "method getPath");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
