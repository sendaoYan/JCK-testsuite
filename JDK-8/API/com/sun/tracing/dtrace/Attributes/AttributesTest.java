/*
 * @test
 * @summary com.sun.tracing.dtrace.Attributes conformance: hierarchy and declared members via reflection.
 * @run main AttributesTest
 */

public class AttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tracing.dtrace.Attributes", false, null);
        check(c.getName().equals("com.sun.tracing.dtrace.Attributes"), "binary name");
        check(c.isAnnotation(), "is annotation");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("data"), "declares data");
        check(ms.contains("dependency"), "declares dependency");
        check(ms.contains("name"), "declares name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
