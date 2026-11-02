/*
 * @test
 * @summary java.beans.XMLDecoder conformance: decode XML bytes back to objects.
 * @run main XMLDecoderTest
 */

import java.beans.*;

public class XMLDecoderTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try (XMLEncoder enc = new XMLEncoder(out)) {
            enc.writeObject("hello");
            enc.writeObject(42);
        }
        byte[] xml = out.toByteArray();
        try (XMLDecoder dec = new XMLDecoder(new java.io.ByteArrayInputStream(xml))) {
            Object o1 = dec.readObject();
            Object o2 = dec.readObject();
            check("hello".equals(o1), "decoded string");
            check(o2 instanceof Integer && ((Integer) o2).intValue() == 42, "decoded integer");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
