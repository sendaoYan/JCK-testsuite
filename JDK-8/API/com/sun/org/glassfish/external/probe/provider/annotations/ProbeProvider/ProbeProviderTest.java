/*
 * @test
 * @summary com.sun.org.glassfish.external.probe.provider.annotations.ProbeProvider conformance: reflection.
 * @run main ProbeProviderTest
 */

public class ProbeProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.probe.provider.annotations.ProbeProvider", false, ProbeProviderTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("ProbeProvider"), "simple name ProbeProvider");
        check(c.isAnnotation(), "is annotation");
        check(c.isInterface(), "annotation is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.annotation.Annotation"), "interface java.lang.annotation.Annotation");
        check(has(c, "providerName"), "method providerName");
        check(has(c, "moduleProviderName"), "method moduleProviderName");
        check(has(c, "moduleName"), "method moduleName");
        check(has(c, "probeProviderName"), "method probeProviderName");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
