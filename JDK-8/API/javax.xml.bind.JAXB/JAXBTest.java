/*
 * @test
 * @summary javax.xml.bind.JAXB conformance: final class and static marshal/unmarshal methods.
 * @run main JAXBTest
 */


public class JAXBTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        check(java.lang.reflect.Modifier.isFinal(javax.xml.bind.JAXB.class.getModifiers()), "final class");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : javax.xml.bind.JAXB.class.getMethods()) names.add(m.getName());
        check(names.contains("marshal"), "has marshal");
        check(names.contains("unmarshal"), "has unmarshal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
