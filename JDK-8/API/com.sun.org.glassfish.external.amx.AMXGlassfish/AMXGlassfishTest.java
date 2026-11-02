/*
 * @test
 * @summary com.sun.org.glassfish.external.amx.AMXGlassfish conformance: reflection.
 * @run main AMXGlassfishTest
 */

public class AMXGlassfishTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.amx.AMXGlassfish", false, AMXGlassfishTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AMXGlassfish"), "simple name AMXGlassfish");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getGlassfishVersion"), "method getGlassfishVersion");
        check(has(c, "amxJMXDomain"), "method amxJMXDomain");
        check(has(c, "amxSupportDomain"), "method amxSupportDomain");
        check(has(c, "dasName"), "method dasName");
        check(has(c, "dasConfig"), "method dasConfig");
        check(has(c, "domainRoot"), "method domainRoot");
        check(has(c, "monitoringRoot"), "method monitoringRoot");
        check(has(c, "serverMon"), "method serverMon");
        check(has(c, "serverMonForDAS"), "method serverMonForDAS");
        check(has(c, "newObjectName"), "method newObjectName");
        check(has(c, "getBootAMXMBeanObjectName"), "method getBootAMXMBeanObjectName");
        check(has(c, "invokeBootAMX"), "method invokeBootAMX");
        check(has(c, "listenForDomainRoot"), "method listenForDomainRoot");
        check(has(c, "waitAMXReady"), "method waitAMXReady");
        check(has(c, "listenForBootAMX"), "method listenForBootAMX");
        check(has(c, "bootAMX"), "method bootAMX");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
