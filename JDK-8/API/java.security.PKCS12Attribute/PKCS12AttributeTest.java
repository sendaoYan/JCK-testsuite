/*
 * @test
 * @summary java.security.PKCS12Attribute conformance: constructors and getters.
 * @run main PKCS12AttributeTest
 */

public class PKCS12AttributeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.PKCS12Attribute a = new java.security.PKCS12Attribute("1.2.3.4", "value");
        check("1.2.3.4".equals(a.getName()), "getName");
        check("value".equals(a.getValue()), "getValue");
        check(a.getEncoded() != null, "getEncoded");
        byte[] der = a.getEncoded();
        java.security.PKCS12Attribute b = new java.security.PKCS12Attribute(der);
        check("1.2.3.4".equals(b.getName()), "byte[] ctor name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
