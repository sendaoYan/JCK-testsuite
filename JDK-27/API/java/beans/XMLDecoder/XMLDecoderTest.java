/*
 * @test
 * @summary java.beans.XMLDecoder conformance: full contract.
 *          implements AutoCloseable, all five constructors, round-trip of
 *          bean/array/list/map/string/integer/null via XMLEncoder, setOwner /
 *          getOwner, owner environment resolution, exception listener capture
 *          (truncated XML, unknown class, fault-tolerant continuation),
 *          decoding of hand-written literal XML (primitives, collections,
 *          arrays, class literals, multiple roots), readObject past end,
 *          close-then-read semantics, repeated independent decoding.
 * @run main XMLDecoderTest
 */

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.SAXParseException;
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

    static final List<Exception> captured = new ArrayList<Exception>();
    static final ExceptionListener CAPTURE = new ExceptionListener() {
        public void exceptionThrown(Exception e) { captured.add(e); }
    };

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
    static Object decodeLiteral(String xml) {
        XMLDecoder dec = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
        try { return dec.readObject(); }
        finally { dec.close(); }
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

        long[] la = new long[] { 9L, -1L };
        long[] laback = (long[]) decode(encode(la));
        check(laback.length == 2 && laback[0] == 9L && laback[1] == -1L, "long[] round-trip");

        double dv = ((Double) decode(encode(Double.valueOf(1.5)))).doubleValue();
        check(dv == 1.5, "Double round-trip");
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

    static void testLiteralPrimitives() {
        check("hello".equals(decodeLiteral("<java><string>hello</string></java>")), "literal string");
        Object i = decodeLiteral("<java><int>42</int></java>");
        check(i instanceof Integer && ((Integer) i).intValue() == 42, "literal int");
        Object b = decodeLiteral("<java><boolean>true</boolean></java>");
        check(Boolean.TRUE.equals(b), "literal boolean");
        Object lg = decodeLiteral("<java><long>1234567890123</long></java>");
        check(lg instanceof Long && ((Long) lg).longValue() == 1234567890123L, "literal long");
        Object sh = decodeLiteral("<java><short>300</short></java>");
        check(sh instanceof Short && ((Short) sh).shortValue() == (short) 300, "literal short");
        Object by = decodeLiteral("<java><byte>7</byte></java>");
        check(by instanceof Byte && ((Byte) by).byteValue() == (byte) 7, "literal byte");
        Object ch = decodeLiteral("<java><char>x</char></java>");
        check(Character.valueOf('x').equals(ch), "literal char");
        Object fl = decodeLiteral("<java><float>2.5</float></java>");
        check(fl instanceof Float && ((Float) fl).floatValue() == 2.5f, "literal float");
        Object db = decodeLiteral("<java><double>1.5</double></java>");
        check(db instanceof Double && ((Double) db).doubleValue() == 1.5, "literal double");
        check("".equals(decodeLiteral("<java><string></string></java>")), "literal empty string");
        check(" spaced ".equals(decodeLiteral("<java><string> spaced </string></java>")),
                "literal string whitespace preserved");
        check(decodeLiteral("<java><null/></java>") == null, "literal null");
    }

    static void testLiteralCollections() {
        Object list = decodeLiteral("<java><object class=\"java.util.ArrayList\">"
                + "<void method=\"add\"><string>a</string></void>"
                + "<void method=\"add\"><string>b</string></void>"
                + "</object></java>");
        check(list instanceof List, "literal ArrayList type");
        check("a".equals(((List<?>) list).get(0)) && "b".equals(((List<?>) list).get(1)),
                "literal ArrayList add-method order");
        check(((List<?>) list).size() == 2, "literal ArrayList size");
    }

    static void testLiteralArray() {
        Object arr = decodeLiteral("<java><array class=\"java.lang.String\" length=\"2\">"
                + "<void index=\"0\"><string>a</string></void>"
                + "<void index=\"1\"><string>b</string></void>"
                + "</array></java>");
        check(arr instanceof String[], "literal array type");
        String[] sa = (String[]) arr;
        check(sa.length == 2, "literal array length");
        check("a".equals(sa[0]) && "b".equals(sa[1]), "literal array indexed elements");
    }

    static void testLiteralClassAndMultipleRoots() {
        check(String.class == decodeLiteral("<java><class>java.lang.String</class></java>"),
                "literal class reference");
        XMLDecoder d = new XMLDecoder(new ByteArrayInputStream(
                "<java><int>1</int><string>two</string></java>".getBytes()));
        check(Integer.valueOf(1).equals(d.readObject()), "multiple roots first object");
        check("two".equals(d.readObject()), "multiple roots second object");
        try {
            d.readObject();
            check(false, "expected ArrayIndexOutOfBoundsException past last root");
        } catch (ArrayIndexOutOfBoundsException e) {
            check(true, "readObject past last root throws ArrayIndexOutOfBoundsException");
        }
        d.close();
    }

    static void testTruncatedXmlCapturedByListener() {
        captured.clear();
        XMLDecoder d = new XMLDecoder(new ByteArrayInputStream(
                "<java><string>unclosed".getBytes()), null, CAPTURE);
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { d.readObject(); }},
                "truncated XML readObject -> ArrayIndexOutOfBoundsException");
        check(captured.size() >= 1, "truncated XML reported to exception listener");
        check(captured.get(0) instanceof SAXParseException, "truncated XML yields SAXParseException");
        d.close();
    }

    static void testUnknownClassCapturedByListener() {
        captured.clear();
        XMLDecoder d = new XMLDecoder(new ByteArrayInputStream(
                "<java><object class=\"no.such.Clazz\"/></java>".getBytes()), null, CAPTURE);
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { d.readObject(); }},
                "unknown class readObject -> ArrayIndexOutOfBoundsException");
        check(captured.size() >= 1, "unknown class reported to exception listener");
        check(captured.get(0) instanceof ClassNotFoundException,
                "unknown class yields ClassNotFoundException");
        d.close();
    }

    static void testFaultTolerantContinuation() {
        captured.clear();
        XMLDecoder d = new XMLDecoder(new ByteArrayInputStream(
                "<java><object class=\"no.such.Clazz\"/><int>5</int></java>".getBytes()),
                null, CAPTURE);
        check(Integer.valueOf(5).equals(d.readObject()),
                "objects after a failed element still decode");
        check(captured.size() >= 1, "failed element reported while decoding continues");
        d.close();
    }

    static void testCloseThenRead() throws Exception {
        byte[] xml = encode("q");
        XMLDecoder d = new XMLDecoder(new ByteArrayInputStream(xml));
        d.close();
        check("q".equals(d.readObject()), "readObject still returns after close");
        d.close();
        check(true, "close is idempotent");
    }

    static void testNullInputStream() {
        XMLDecoder d = new XMLDecoder((InputStream) null);
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { d.readObject(); }},
                "null input stream readObject -> ArrayIndexOutOfBoundsException");
        d.close();
        check(true, "close on null-input decoder does not throw");
    }

    static void testTwoDecodersIndependent() throws Exception {
        Bean bean = new Bean();
        bean.setX(11);
        bean.setS("indep");
        byte[] xml = encode(bean);
        XMLDecoder da = new XMLDecoder(new ByteArrayInputStream(xml));
        XMLDecoder db = new XMLDecoder(new ByteArrayInputStream(xml));
        Bean a = (Bean) da.readObject();
        Bean b = (Bean) db.readObject();
        check(a != b, "two decoders produce independent instances");
        check(a.getX() == b.getX() && a.getS().equals(b.getS()),
                "two decoders produce equal instances");
        da.close();
        db.close();
    }

    static void testInputSourceVariants() {
        XMLDecoder d1 = new XMLDecoder(new org.xml.sax.InputSource(
                new StringReader("<java><int>9</int></java>")));
        check(Integer.valueOf(9).equals(d1.readObject()), "InputSource from character stream");
        d1.close();

        XMLDecoder d2 = new XMLDecoder(new org.xml.sax.InputSource(
                new ByteArrayInputStream("<java><int>8</int></java>".getBytes())));
        check(Integer.valueOf(8).equals(d2.readObject()), "InputSource from byte stream");
        check(d2.getOwner() == null, "InputSource constructor defaults owner to null");
        d2.close();
    }

    static void testListenerDefaults() throws Exception {
        XMLDecoder d1 = new XMLDecoder(new ByteArrayInputStream(encode("x")));
        check(d1.getExceptionListener() != null, "default exception listener non-null");
        d1.setExceptionListener(null);
        check(d1.getExceptionListener() != null, "setExceptionListener(null) falls back to default");
        d1.close();

        XMLDecoder d2 = new XMLDecoder(new ByteArrayInputStream(encode("x")), null, null);
        check(d2.getExceptionListener() != null, "null listener in constructor falls back to default");
        d2.close();

        XMLDecoder d3 = new XMLDecoder(new ByteArrayInputStream(encode("x")));
        check(d3.getOwner() == null, "default owner is null");
        Object owner = new Object();
        d3.setOwner(owner);
        check(owner == d3.getOwner(), "setOwner/getOwner round-trip");
        d3.close();
    }

    static void testOwnerEnvironment() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        Bean owner = new Bean();
        enc.setOwner(owner);
        enc.writeObject(owner);
        enc.close();
        Bean decodeOwner = new Bean();
        XMLDecoder d = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()), decodeOwner);
        Object o = d.readObject();
        check(o instanceof Bean, "owner-referencing archive decodes a bean");
        check(o == decodeOwner, "owner reference resolves to the decoder owner instance");
        d.close();
    }

    static void testFlushedArchive() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.writeObject("first");
        enc.flush();
        enc.writeObject(Integer.valueOf(2));
        enc.flush();
        enc.close();
        XMLDecoder d = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()));
        check("first".equals(d.readObject()), "first flushed object read first");
        check(Integer.valueOf(2).equals(d.readObject()), "second flushed object read second");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() throws Exception { d.readObject(); }},
                "no more flushed objects -> ArrayIndexOutOfBoundsException");
        d.close();
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testRoundTrip();
        testConstructorsAndOwner();
        testBadInput();
        testLiteralPrimitives();
        testLiteralCollections();
        testLiteralArray();
        testLiteralClassAndMultipleRoots();
        testTruncatedXmlCapturedByListener();
        testUnknownClassCapturedByListener();
        testFaultTolerantContinuation();
        testCloseThenRead();
        testNullInputStream();
        testTwoDecodersIndependent();
        testInputSourceVariants();
        testListenerDefaults();
        testOwnerEnvironment();
        testFlushedArchive();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
