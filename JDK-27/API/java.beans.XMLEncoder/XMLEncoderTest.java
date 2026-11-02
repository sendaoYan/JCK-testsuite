/*
 * @test
 * @summary java.beans.XMLEncoder conformance: encode objects to XML bytes.
 * @run main XMLEncoderTest
 */

import java.beans.*;

public class XMLEncoderTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try (XMLEncoder enc = new XMLEncoder(out)) {
            enc.writeObject("hello");
            enc.writeObject(42);
        }
        byte[] xml = out.toByteArray();
        check(xml.length > 0, "encoded non-empty");
        String s = new String(xml, "UTF-8");
        check(s.contains("<string>"), "contains string element");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
