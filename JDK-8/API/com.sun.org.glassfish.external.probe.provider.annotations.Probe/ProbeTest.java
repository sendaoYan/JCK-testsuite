/*
 * @test
 * @summary com.sun.org.glassfish.external.probe.provider.annotations.Probe conformance: reflection.
 * @run main ProbeTest
 */

public class ProbeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.glassfish.external.probe.provider.annotations.Probe", false, ProbeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Probe"), "simple name Probe");
        check(c.isAnnotation(), "is annotation");
        check(c.isInterface(), "annotation is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("java.lang.annotation.Annotation"), "interface java.lang.annotation.Annotation");
        check(has(c, "name"), "method name");
        check(has(c, "hidden"), "method hidden");
        check(has(c, "self"), "method self");
        check(has(c, "providerName"), "method providerName");
        check(has(c, "moduleName"), "method moduleName");
        check(has(c, "stateful"), "method stateful");
        check(has(c, "profileNames"), "method profileNames");
        check(has(c, "statefulReturn"), "method statefulReturn");
        check(has(c, "statefulException"), "method statefulException");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
