/*
 * @test
 * @summary java.security.PKCS12Attribute conformance: constructors and accessors.
 * @run main PKCS12AttributeTest
 */

import java.security.PKCS12Attribute;

public class PKCS12AttributeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        PKCS12Attribute attr = new PKCS12Attribute("1.2.840.113549.1.9.20", "hello");
        check("1.2.840.113549.1.9.20".equals(attr.getName()), "getName");
        check("hello".equals(attr.getValue()), "getValue");
        byte[] enc = attr.getEncoded();
        check(enc != null && enc.length > 0, "getEncoded non-empty");
        PKCS12Attribute attr2 = new PKCS12Attribute(enc);
        check(attr.equals(attr2), "equals from encoded");
        check(attr.hashCode() == attr2.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
