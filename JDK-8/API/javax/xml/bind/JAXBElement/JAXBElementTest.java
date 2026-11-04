/*
 * @test
 * @summary javax.xml.bind.JAXBElement conformance: JAXB element value holder.
 * @run main JAXBElementTest
 */


public class JAXBElementTest {
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
        javax.xml.namespace.QName qn = new javax.xml.namespace.QName("urn:test", "elem");
        javax.xml.bind.JAXBElement<String> el = new javax.xml.bind.JAXBElement<String>(qn, String.class, "value");
        check(el.getName().equals(qn), "name");
        check("value".equals(el.getValue()), "value");
        check(el.getDeclaredType() == String.class, "declared type");
        check(!el.isNil(), "not nil");
        el.setNil(true);
        check(el.isNil(), "set nil");
        check(el.isGlobalScope(), "global scope");
        javax.xml.bind.JAXBElement<String> el2 = new javax.xml.bind.JAXBElement<String>(qn, String.class, String.class, "value");
        check(!el2.isGlobalScope(), "scoped not global");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
