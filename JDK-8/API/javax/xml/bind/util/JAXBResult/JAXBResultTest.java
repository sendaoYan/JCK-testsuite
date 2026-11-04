/*
 * @test
 * @summary javax.xml.bind.util.JAXBResult conformance: extends SAXResult with getResult.
 * @run main JAXBResultTest
 */


public class JAXBResultTest {
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
        check(javax.xml.bind.util.JAXBResult.class.getSuperclass().getName().equals("javax.xml.transform.sax.SAXResult"), "extends SAXResult");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : javax.xml.bind.util.JAXBResult.class.getMethods()) names.add(m.getName());
        check(names.contains("getResult"), "has getResult");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
