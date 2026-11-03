/*
 * @test
 * @summary java.security.PKCS12Attribute conformance: white-box structure + black-box behavior.
 * @run main PKCS12AttributeTest
 */

import java.security.PKCS12Attribute;
import java.security.*;
import java.util.*;

public class PKCS12AttributeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        PKCS12Attribute attr = new PKCS12Attribute("1.2.840.113549.1.9.20", "hello");
        check("1.2.840.113549.1.9.20".equals(attr.getName()), "getName");
        check("hello".equals(attr.getValue()), "getValue");
        byte[] enc = attr.getEncoded();
        check(enc != null && enc.length > 0, "getEncoded non-empty");
        PKCS12Attribute attr2 = new PKCS12Attribute(enc);
        check(attr.equals(attr2), "equals from encoded");
        check(attr.hashCode() == attr2.hashCode(), "hashCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        PKCS12Attribute a1 = new PKCS12Attribute("1.2.840.113549.1.9.20", "JCK FriendlyName");
        check(a1.getName().equals("1.2.840.113549.1.9.20"), "name");
        check(a1.getValue().equals("JCK FriendlyName"), "value");
        check(a1.equals(new PKCS12Attribute("1.2.840.113549.1.9.20", "JCK FriendlyName")), "equals");
        check(!a1.equals(new PKCS12Attribute("1.2.840.113549.1.9.20", "other")), "not equals");
        check(a1.hashCode() == new PKCS12Attribute("1.2.840.113549.1.9.20", "JCK FriendlyName").hashCode(), "hashCode");
        check(a1.toString().contains("1.2.840.113549.1.9.20") && a1.toString().contains("JCK FriendlyName"), "toString");
        byte[] der = a1.getEncoded();
        check(der != null && der.length > 0, "encoded");
        // DER round trip through the byte[] constructor
        PKCS12Attribute a2 = new PKCS12Attribute(der);
        check(a2.equals(a1), "DER round trip equals");
        check(a2.getName().equals(a1.getName()), "round trip name");
        check(a2.getValue().equals(a1.getValue()), "round trip value");
        // second well-known attribute: localKeyId 1.2.840.113549.1.9.21
        PKCS12Attribute ld = new PKCS12Attribute("1.2.840.113549.1.9.21", "1234");
        check(ld.getName().equals("1.2.840.113549.1.9.21"), "localKey id");
        check(ld.getValue().equals("1234"), "localKey value");
        // null arguments rejected
        try { new PKCS12Attribute(null, "v"); check(false, "null name NPE"); }
        catch (NullPointerException npe) { check(true, "null name NPE"); }
        try { new PKCS12Attribute("1.2.3", (String) null); check(false, "null value NPE"); }
        catch (NullPointerException npe) { check(true, "null value NPE"); }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
