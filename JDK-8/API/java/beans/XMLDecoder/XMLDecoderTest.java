/*
 * @test
 * @summary java.beans.XMLDecoder conformance: full contract.
 *          implements AutoCloseable, all five constructors, round-trip of
 *          bean/array/list/map/string/integer/null via XMLEncoder, setOwner /
 *          getOwner, exception listener, and readObject on empty/garbage input
 *          throws (ArrayIndexOutOfBoundsException), createHandler returns a
 *          SAX DefaultHandler.
 * @run main XMLDecoderTest
 */

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.helpers.DefaultHandler;

public class XMLDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public static class Bean {
        private int x;
        private String s;
        public Bean() {}
        public int getX() { return x; }
        public void setX(int v) { x = v; }
        public String getS() { return s; }
        public void setS(String v) { s = v; }
    }

    static byte[] encode(Object o) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.writeObject(o);
        enc.close();
        return baos.toByteArray();
    }
    static Object decode(byte[] xml) throws Exception {
        XMLDecoder dec = new XMLDecoder(new ByteArrayInputStream(xml));
        Object o = dec.readObject();
        dec.close();
        return o;
    }

    static void testStructure() {
        Class<?> c = XMLDecoder.class;
        check(!c.isInterface(), "concrete class");
        check(AutoCloseable.class.isAssignableFrom(c), "implements AutoCloseable");
    }

    static void testRoundTrip() throws Exception {
        Bean bean = new Bean();
        bean.setX(7);
        bean.setS("abc");
        Object o = decode(encode(bean));
        check(o instanceof Bean, "decoded Bean");
        check(((Bean) o).getX() == 7 && "abc".equals(((Bean) o).getS()), "decoded fields");

        check("hello".equals(decode(encode("hello"))), "string round-trip");
        check(Integer.valueOf(42).equals(decode(encode(Integer.valueOf(42)))), "Integer round-trip");
        check(Arrays.equals(new int[] { 1, 2, 3 }, (int[]) decode(encode(new int[] { 1, 2, 3 }))), "int[] round-trip");

        List<Integer> list = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
        Object lo = decode(encode(list));
        check(list.equals(lo), "List round-trip");

        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("a", 1);
        Object mo = decode(encode(map));
        check(map.equals(mo), "Map round-trip");

        check(decode(encode(null)) == null, "null round-trip");
    }

    static void testConstructorsAndOwner() throws Exception {
        byte[] xml = encode("x");
        Object owner = new Object();
        ExceptionListener l = new ExceptionListener() { public void exceptionThrown(Exception e) {} };

        XMLDecoder d1 = new XMLDecoder(new ByteArrayInputStream(xml));
        check("x".equals(d1.readObject()), "one-arg ctor read");
        d1.close();

        XMLDecoder d2 = new XMLDecoder(new ByteArrayInputStream(xml), owner);
        check(owner == d2.getOwner(), "two-arg ctor owner");
        d2.close();

        XMLDecoder d3 = new XMLDecoder(new ByteArrayInputStream(xml), owner, l);
        check(owner == d3.getOwner() && l == d3.getExceptionListener(), "three-arg ctor owner + listener");
        d3.close();

        XMLDecoder d4 = new XMLDecoder(new ByteArrayInputStream(xml), owner, l, XMLDecoderTest.class.getClassLoader());
        check(owner == d4.getOwner() && l == d4.getExceptionListener(), "four-arg ctor owner + listener");
        d4.close();

        org.xml.sax.InputSource is = new org.xml.sax.InputSource(new ByteArrayInputStream(xml));
        XMLDecoder d5 = new XMLDecoder(is);
        check("x".equals(d5.readObject()), "InputSource ctor read");
        d5.close();

        // setExceptionListener / setOwner
        XMLDecoder d6 = new XMLDecoder(new ByteArrayInputStream(xml));
        Object o2 = new Object();
        d6.setOwner(o2);
        check(o2 == d6.getOwner(), "setOwner");
        ExceptionListener l2 = new ExceptionListener() { public void exceptionThrown(Exception e) {} };
        d6.setExceptionListener(l2);
        check(l2 == d6.getExceptionListener(), "setExceptionListener");
        d6.close();

        DefaultHandler h = XMLDecoder.createHandler(owner, l, XMLDecoderTest.class.getClassLoader());
        check(h != null, "createHandler returns DefaultHandler");
    }

    static void testBadInput() {
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    XMLDecoder d = new XMLDecoder(new ByteArrayInputStream("".getBytes("UTF-8")));
                    d.readObject();
                }}, "empty input readObject -> ArrayIndexOutOfBoundsException");

        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    XMLDecoder d = new XMLDecoder(new ByteArrayInputStream("<java>garbage</java>".getBytes("UTF-8")));
                    d.readObject();
                }}, "garbage input readObject -> ArrayIndexOutOfBoundsException");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testRoundTrip();
        testConstructorsAndOwner();
        testBadInput();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
