/*
 * @test
 * @summary java.beans.XMLEncoder conformance: full contract.
 *          extends Encoder + implements AutoCloseable, both constructors
 *          (charset/declaration/indentation plus negative matrix),
 *          writeObject round-trip matrix (bean graphs, nested beans, List/Map
 *          order, primitive/String arrays, null property, XML escapes and
 *          char quoting, Date millis, shared-reference identity, class
 *          literals, public fields), header structure, flush/close lifecycle,
 *          owner environment, exception listener defaults, File vs stream
 *          output.
 * @run main XMLEncoderTest
 */

import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.Statement;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XMLEncoderTest {
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
    public static class Parent {
        private Bean child = new Bean();
        public Parent() {}
        public Bean getChild() { return child; }
        public void setChild(Bean c) { child = c; }
    }
    public static class Holder {
        private Bean a; private Bean b;
        public Holder() {}
        public Bean getA() { return a; }
        public void setA(Bean v) { a = v; }
        public Bean getB() { return b; }
        public void setB(Bean v) { b = v; }
    }
    public static class FieldBean {
        public int count = 0;
        public FieldBean() {}
    }

    static final java.util.List<Exception> captured = new java.util.ArrayList<Exception>();
    static final ExceptionListener CAPTURE = new ExceptionListener() {
        public void exceptionThrown(Exception e) { captured.add(e); }
    };

    static String encode(Object o) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.writeObject(o);
        enc.close();
        return baos.toString("UTF-8");
    }

    static void testStructure() {
        Class<?> c = XMLEncoder.class;
        check(Encoder.class.isAssignableFrom(c), "extends Encoder");
        check(AutoCloseable.class.isAssignableFrom(c), "implements AutoCloseable");
    }

    static void testWriteObject() throws Exception {
        String xml = encode("hello");
        check(xml.contains("hello"), "string encoded");

        check(encode(Integer.valueOf(42)).contains("42"), "Integer encoded");

        Bean bean = new Bean();
        bean.setX(42);
        bean.setS("hi");
        String beanXml = encode(bean);
        check(beanXml.contains("42") && beanXml.contains("hi"), "bean properties encoded");

        String arrXml = encode(new int[] { 1, 2, 3 });
        check(arrXml.contains("1") && arrXml.contains("2") && arrXml.contains("3"), "array encoded");

        List<Integer> list = new ArrayList<Integer>(Arrays.asList(1, 2, 3));
        check(encode(list).contains("1"), "List encoded");

        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("a", 1);
        check(encode(map).contains("a"), "Map encoded");

        String nullXml = encode(null);
        check(nullXml.contains("<null/>"), "null encoded as <null/>");
    }

    static void testOwner() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        check(enc.getOwner() == null, "default owner null");
        Object owner = new Object();
        enc.setOwner(owner);
        check(owner == enc.getOwner(), "setOwner/getOwner");
        enc.close();
    }

    static void testFourArgConstructor() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos, "UTF-8", true, 16);
        enc.writeObject("x");
        enc.flush();
        enc.close();
        String xml = baos.toString("UTF-8");
        check(xml.contains("x"), "four-arg constructor encodes");
        check(xml.contains("<?xml"), "xml declaration present");
    }

    static void testStatements() throws Exception {
        Bean bean = new Bean();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.writeStatement(new Statement(bean, "setX", new Object[] { Integer.valueOf(5) }));
        enc.writeExpression(new Expression(bean, "getX", new Object[0]));
        enc.close();
        String xml = baos.toString("UTF-8");
        check(xml.contains("5"), "writeStatement/writeExpression produce output");
    }

    static void testBeanGraphRoundTrip() throws Exception {
        Bean child = new Bean();
        child.setX(7);
        child.setS("abc");
        Parent parent = new Parent();
        parent.setChild(child);
        String xml = encode(parent);
        check(xml.contains("java.beans.XMLEncoderTest$Parent")
                || xml.contains("XMLEncoderTest$Parent"), "nested bean graph class name encoded");
        java.beans.XMLDecoder dec = new java.beans.XMLDecoder(
                new java.io.ByteArrayInputStream(xml.getBytes("UTF-8")));
        Parent back = (Parent) dec.readObject();
        dec.close();
        check(back != parent, "decoded parent is a new instance");
        check(back.getChild() != child, "decoded child is a new instance");
        check(back.getChild().getX() == 7, "nested child x round-trip");
        check("abc".equals(back.getChild().getS()), "nested child s round-trip");
    }

    static void testCollectionsRoundTrip() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("z"); list.add("a"); list.add("m");
        List<String> lback = (List<String>) decode(encode(list));
        check(lback.size() == 3, "List size round-trip");
        check("z".equals(lback.get(0)) && "a".equals(lback.get(1)) && "m".equals(lback.get(2)),
                "List element order preserved");

        Map<String, Integer> map = new LinkedHashMap<String, Integer>();
        map.put("k1", Integer.valueOf(1));
        map.put("k2", Integer.valueOf(2));
        Map<String, Integer> mback = (Map<String, Integer>) decode(encode(map));
        check(map.equals(mback), "Map round-trip equality");
        check(Integer.valueOf(1).equals(mback.get("k1")) && Integer.valueOf(2).equals(mback.get("k2")),
                "Map entries round-trip");
    }

    static void testArraysRoundTrip() throws Exception {
        String iaxml = encode(new int[] { 7, 8, 9 });
        check(iaxml.contains("<array") && iaxml.contains("length=\"3\""), "int[] array tag and length");
        int[] iaback = (int[]) decode(iaxml);
        check(iaback.length == 3, "int[] length round-trip");
        check(iaback[0] == 7 && iaback[1] == 8 && iaback[2] == 9, "int[] elements round-trip");

        String[] sa = new String[] { "p", "", "q" };
        String[] saback = (String[]) decode(encode(sa));
        check(saback.length == 3, "String[] length round-trip");
        check("p".equals(saback[0]) && "".equals(saback[1]) && "q".equals(saback[2]),
                "String[] elements with empty element round-trip");

        boolean[] ba = new boolean[] { true, false };
        boolean[] baback = (boolean[]) decode(encode(ba));
        check(baback.length == 2 && baback[0] && !baback[1], "boolean[] round-trip");
    }

    static void testNullAndEmptyProperty() throws Exception {
        Bean nb = new Bean();
        Bean nback = (Bean) decode(encode(nb));
        check(nback.getS() == null, "null property value round-trips as null");
        check(nback.getX() == 0, "default int property round-trips as 0");

        Bean eb = new Bean();
        eb.setS("");
        Bean eback = (Bean) decode(encode(eb));
        check("".equals(eback.getS()), "empty string property round-trip");
    }

    static void testEscapes() throws Exception {
        String tricky = "<>&'\"";
        String xml = encode(tricky);
        check(xml.contains("&lt;"), "less-than escaped as &lt;");
        check(xml.contains("&gt;"), "greater-than escaped as &gt;");
        check(xml.contains("&amp;"), "ampersand escaped as &amp;");
        check(xml.contains("&quot;"), "double quote escaped as &quot;");
        check(xml.contains("&apos;"), "apostrophe escaped as &apos;");
        check(!xml.contains("&#39;"), "numeric apostrophe escape is not used");
        check(tricky.equals(decode(xml)), "escaped string round-trip equality");

        String ctl = "a\r\nb\tc";
        String cxml = encode(ctl);
        check(cxml.contains("&#13;"), "carriage return escaped as &#13;");
        check(ctl.equals(decode(cxml)), "newline/tab/cr string round-trip");

        String nonBmp = "music 𝄞 note";
        check(nonBmp.equals(decode(encode(nonBmp))), "non-BMP surrogate pair round-trip");
    }

    static void testCharQuoting() throws Exception {
        String q1 = encode(Character.valueOf('\''));
        check(q1.contains("<char>&apos;</char>"), "char apostrophe quoted in <char> element");
        check(Character.valueOf('\'').equals(decode(q1)), "char apostrophe round-trip");

        String q2 = encode(Character.valueOf('<'));
        check(q2.contains("<char>&lt;</char>"), "char less-than quoted in <char> element");
        check(Character.valueOf('<').equals(decode(q2)), "char less-than round-trip");

        String q3 = encode(Character.valueOf('\u0000'));
        check(q3.contains("<char code=\"#0\"/>"), "XML-invalid char emitted as numeric code");
        check(Character.valueOf('\u0000').equals(decode(q3)), "XML-invalid char round-trip");
    }

    static void testDateRoundTrip() throws Exception {
        Date d = new Date(1234567890123L);
        String dxml = encode(d);
        check(dxml.contains("java.util.Date"), "Date object tag");
        check(dxml.contains("<long>1234567890123</long>"), "Date encoded as long millis");
        Date dback = (Date) decode(dxml);
        check(dback.getTime() == 1234567890123L, "Date millis round-trip");
    }

    static void testIdentityPreservation() throws Exception {
        Bean shared = new Bean();
        shared.setX(1);
        shared.setS("same");
        Holder h = new Holder();
        h.setA(shared);
        h.setB(shared);
        String hxml = encode(h);
        check(hxml.contains("idref"), "shared reference emitted as idref");
        Holder hback = (Holder) decode(hxml);
        check(hback.getA() == hback.getB(), "same object referenced twice decodes to same instance");
        check(hback.getA().getX() == 1 && "same".equals(hback.getA().getS()), "shared bean fields");
    }

    static void testClassLiteral() throws Exception {
        String cxml = encode(String.class);
        check(cxml.contains("<class>java.lang.String</class>"), "Class literal encoded");
        check(String.class == decode(cxml), "Class literal round-trip");
    }

    static void testHeader() throws Exception {
        String xml = encode("hdr");
        check(xml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"), "xml declaration with charset");
        check(xml.contains("<java version="), "java root element with version attribute");
        check(xml.contains("class=\"java.beans.XMLDecoder\""), "root element names decoder class");
        check(!xml.contains("<!DOCTYPE"), "no DOCTYPE line is emitted");
    }

    static void testExceptionListener() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        check(enc.getExceptionListener() != null, "default exception listener non-null");
        enc.setExceptionListener(CAPTURE);
        check(enc.getExceptionListener() == CAPTURE, "custom listener returned by get");
        enc.setExceptionListener(null);
        check(enc.getExceptionListener() != null, "null listener falls back to default");
        enc.close();
    }

    static void testFlushLifecycle() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.writeObject("hello");
        check(baos.size() == 0, "nothing written to the stream before flush");
        enc.flush();
        String mid = baos.toString("UTF-8");
        check(mid.contains("<?xml"), "preamble written by flush");
        check(mid.contains("hello"), "object value written by flush");
        check(!mid.contains("</java>"), "closing tag withheld until close");
        enc.close();
        String full = baos.toString("UTF-8");
        check(full.contains("</java>"), "closing tag written by close");
        byte[] before = baos.toByteArray();
        enc.writeObject("after-close");
        check(Arrays.equals(before, baos.toByteArray()),
                "writeObject after close neither throws nor changes output");
    }

    static void testFlushMultiObject() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.writeObject("first");
        enc.flush();
        enc.writeObject(Integer.valueOf(2));
        enc.flush();
        enc.close();
        java.beans.XMLDecoder dec = new java.beans.XMLDecoder(
                new java.io.ByteArrayInputStream(baos.toByteArray()));
        check("first".equals(dec.readObject()), "first flushed object decodes first");
        check(Integer.valueOf(2).equals(dec.readObject()), "second flushed object decodes second");
        try {
            dec.readObject();
            check(false, "expected ArrayIndexOutOfBoundsException past end");
        } catch (ArrayIndexOutOfBoundsException e) {
            check(true, "past end of flushed archive throws ArrayIndexOutOfBoundsException");
        }
        dec.close();
    }

    static void testOwnerEnvironment() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        Bean owner = new Bean();
        enc.setOwner(owner);
        enc.writeObject(owner);
        enc.close();
        String xml = baos.toString("UTF-8");
        check(xml.contains("property=\"owner\""), "owner reference emitted as owner property");
        Bean decodeOwner = new Bean();
        java.beans.XMLDecoder dec = new java.beans.XMLDecoder(
                new java.io.ByteArrayInputStream(baos.toByteArray()), decodeOwner);
        check(dec.readObject() == decodeOwner, "owner-referencing archive decodes to the set owner");
        dec.close();
    }

    static void testFileOutput() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"),
                "XMLEncoderTest-" + System.nanoTime());
        check(dir.mkdirs(), "temp dir created");
        File f = null;
        try {
            f = File.createTempFile("enc", ".xml", dir);
            FileOutputStream fo = new FileOutputStream(f);
            XMLEncoder enc = new XMLEncoder(fo);
            enc.writeObject("hello-file");
            enc.close();
            fo.close();
            byte[] fileBytes = readAll(f);
            check(Arrays.equals(fileBytes, encode("hello-file").getBytes("UTF-8")),
                    "File output identical to ByteArrayOutputStream output");
            java.beans.XMLDecoder dec = new java.beans.XMLDecoder(
                    new java.io.ByteArrayInputStream(fileBytes));
            check("hello-file".equals(dec.readObject()), "archive written to File decodes");
            dec.close();
        } finally {
            if (f != null) check(f.delete(), "temp file deleted");
            check(dir.delete(), "temp dir deleted");
        }
    }

    static void testCharsetVariants() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos, "US-ASCII", true, 0);
        enc.writeObject("café");
        enc.close();
        String axml = baos.toString("UTF-8");
        check(axml.contains("encoding=\"US-ASCII\""), "declaration reflects requested charset");
        check(axml.contains("<char code=\"#e9\"/>"), "non-ASCII char emitted as numeric code");
        check("café".equals(decode(axml)), "US-ASCII archive round-trips non-ASCII text");

        ByteArrayOutputStream baos16 = new ByteArrayOutputStream();
        XMLEncoder enc16 = new XMLEncoder(baos16, "UTF-16", true, 0);
        enc16.writeObject("hi16");
        enc16.close();
        check(new String(baos16.toByteArray(), "UTF-16").contains("encoding=\"UTF-16\""),
                "UTF-16 declaration emitted");
        java.beans.XMLDecoder d16 = new java.beans.XMLDecoder(
                new java.io.ByteArrayInputStream(baos16.toByteArray()));
        check("hi16".equals(d16.readObject()), "UTF-16 archive round-trips");
        d16.close();
    }

    static void testDeclarationAndIndentation() throws Exception {
        ByteArrayOutputStream b1 = new ByteArrayOutputStream();
        XMLEncoder e1 = new XMLEncoder(b1, "UTF-8", false, 0);
        e1.writeObject("x");
        e1.close();
        String x1 = b1.toString("UTF-8");
        check(x1.startsWith("<java"), "declaration=false suppresses xml declaration");
        check(!x1.contains("<?xml"), "declaration=false emits no xml declaration line");
        check(x1.contains("<string>x</string>"), "declaration=false still emits values");

        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        XMLEncoder e2 = new XMLEncoder(b2, "UTF-8", true, 2);
        e2.writeObject("x");
        e2.close();
        String x2 = b2.toString("UTF-8");
        check(x2.startsWith("  <?xml"), "initial indentation prefixes the document");
        check(x2.contains("<string>x</string>"), "indented document still emits values");
    }

    static void testConstructorNegatives() {
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new XMLEncoder(null);
                }}, "null output stream -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new XMLEncoder(new ByteArrayOutputStream(), "UTF-8", true, -1);
                }}, "negative indentation -> IllegalArgumentException");
        checkThrows(java.nio.charset.IllegalCharsetNameException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new XMLEncoder(new ByteArrayOutputStream(), "!!!bogus!!!", true, 0);
                }}, "illegal charset name -> IllegalCharsetNameException");
        checkThrows(java.nio.charset.UnsupportedCharsetException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new XMLEncoder(new ByteArrayOutputStream(), "NoSuchCharset-xyz", true, 0);
                }}, "unknown charset -> UnsupportedCharsetException");
    }

    static void testPublicFieldBean() throws Exception {
        FieldBean fb = new FieldBean();
        fb.count = 9;
        String fxml = encode(fb);
        check(fxml.contains("method=\"getField\""), "public field encoded via getField");
        FieldBean fback = (FieldBean) decode(fxml);
        check(fback.count == 9, "public field round-trip");
    }

    static Object decode(String xml) {
        java.beans.XMLDecoder dec = new java.beans.XMLDecoder(
                new java.io.ByteArrayInputStream(xml.getBytes()));
        try { return dec.readObject(); } finally { dec.close(); }
    }

    static byte[] readAll(File f) throws Exception {
        java.io.FileInputStream in = new java.io.FileInputStream(f);
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) b.write(buf, 0, n);
            return b.toByteArray();
        } finally { in.close(); }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testWriteObject();
        testOwner();
        testFourArgConstructor();
        testStatements();
        testBeanGraphRoundTrip();
        testCollectionsRoundTrip();
        testArraysRoundTrip();
        testNullAndEmptyProperty();
        testEscapes();
        testCharQuoting();
        testDateRoundTrip();
        testIdentityPreservation();
        testClassLiteral();
        testHeader();
        testExceptionListener();
        testFlushLifecycle();
        testFlushMultiObject();
        testOwnerEnvironment();
        testFileOutput();
        testCharsetVariants();
        testDeclarationAndIndentation();
        testConstructorNegatives();
        testPublicFieldBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
