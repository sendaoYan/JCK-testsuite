/*
 * @test
 * @summary java.beans.XMLEncoder conformance: white-box structure + black-box behavior.
 * @run main XMLEncoderTest
 */

import java.beans.Encoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;

public class XMLEncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class Bean {
        private int x; private String s;
        public Bean() {}
        public int getX() { return x; }
        public void setX(int v) { x = v; }
        public String getS() { return s; }
        public void setS(String v) { s = v; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.XMLEncoder");
        check(Encoder.class.isAssignableFrom(c), "extends Encoder");
    }

    static void testBlackBox() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        Bean b = new Bean(); b.setX(42); b.setS("hi");
        enc.writeObject(b);
        enc.close();
        String xml = baos.toString("UTF-8");
        check(xml.contains("42"), "xml contains x");
        check(xml.contains("hi"), "xml contains s");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
