/*
 * @test
 * @summary com.sun.jmx.remote.security.JMXSubjectDomainCombiner conformance: hierarchy and declared members via reflection.
 * @run main JMXSubjectDomainCombinerTest
 */

public class JMXSubjectDomainCombinerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.security.JMXSubjectDomainCombiner", false, null);
        check(c.getName().equals("com.sun.jmx.remote.security.JMXSubjectDomainCombiner"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.security.auth.SubjectDomainCombiner"), "superclass javax.security.auth.SubjectDomainCombiner");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("combine"), "declares combine");
        check(ms.contains("getContext"), "declares getContext");
        check(ms.contains("getDomainCombinerContext"), "declares getDomainCombinerContext");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
