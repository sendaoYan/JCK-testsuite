/*
 * @test
 * @summary java.beans.XMLDecoder conformance: white-box structure + black-box behavior.
 * @run main XMLDecoderTest
 */

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class XMLDecoderTest {
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
        Class<?> c = Class.forName("java.beans.XMLDecoder");
        check(!c.isInterface(), "is a class");
    }

    static void testBlackBox() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        Bean b = new Bean(); b.setX(7); b.setS("abc");
        enc.writeObject(b);
        enc.close();

        XMLDecoder dec = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()));
        Object o = dec.readObject();
        dec.close();
        check(o instanceof Bean, "decoded Bean");
        check(((Bean) o).getX() == 7 && "abc".equals(((Bean) o).getS()), "decoded fields");

        ExceptionListener l = new ExceptionListener() { public void exceptionThrown(Exception e) {} };
        XMLDecoder dec2 = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()), "owner", l);
        check("owner".equals(dec2.getOwner()), "owner ctor");
        check(l == dec2.getExceptionListener(), "exceptionListener ctor");
        dec2.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
