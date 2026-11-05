/*
 * @test
 * @summary javax.print.SimpleDoc conformance: white-box structure + black-box behavior.
 * @run main SimpleDocTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class SimpleDocTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.SimpleDoc");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "public final class");
        check(Doc.class.isAssignableFrom(c), "implements Doc");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SimpleDoc d = new SimpleDoc("hello", DocFlavor.STRING.TEXT_PLAIN, null);
        check(DocFlavor.STRING.TEXT_PLAIN.equals(d.getDocFlavor()), "getDocFlavor");
        check("hello".equals(d.getPrintData()), "getPrintData");
        check(d.getAttributes() == null, "null attributes");
        Reader r = d.getReaderForText();
        check(r != null, "getReaderForText non-null");
        StringBuilder sb = new StringBuilder();
        try { char[] buf = new char[8]; int n; while ((n = r.read(buf)) != -1) sb.append(buf, 0, n); } catch (IOException e) { throw new AssertionError(e); }
        check("hello".equals(sb.toString()), "reader reads text");
        check(d.getStreamForBytes() == null, "text doc has no byte stream");
        byte[] data = "abc".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        SimpleDoc b = new SimpleDoc(data, DocFlavor.BYTE_ARRAY.TEXT_PLAIN_US_ASCII, null);
        InputStream bs = b.getStreamForBytes();
        check(bs != null && bs.read() == (int) 'a', "byte stream reads");
        check(b.getReaderForText() == null, "byte doc has no reader");
        HashDocAttributeSet attrs = new HashDocAttributeSet();
        attrs.add(new DocumentName("n", Locale.ROOT));
        SimpleDoc d2 = new SimpleDoc("x", DocFlavor.STRING.TEXT_PLAIN, attrs);
        check("n".equals(((DocumentName) d2.getAttributes().get(DocumentName.class)).getValue()), "attributes roundtrip");
        boolean threw = false; try { d2.getAttributes().add(new DocumentName("y", Locale.ROOT)); } catch (UnmodifiableSetException e) { threw = true; }
        check(threw, "attributes unmodifiable");
        boolean threw2 = false; try { new SimpleDoc(null, DocFlavor.STRING.TEXT_PLAIN, null); } catch (IllegalArgumentException e) { threw2 = true; }
        check(threw2, "null data IAE");
        boolean threw3 = false; try { new SimpleDoc("x", null, null); } catch (IllegalArgumentException e) { threw3 = true; }
        check(threw3, "null flavor IAE");
        boolean threw4 = false; try { new SimpleDoc(new Integer(1), DocFlavor.STRING.TEXT_PLAIN, null); } catch (IllegalArgumentException e) { threw4 = true; }
        check(threw4, "wrong data type IAE");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: read loops / mismatch IAE / null NPE / attribute set isolation ----
    static void testContractDeep() throws Exception {
        javax.print.SimpleDoc sd = new javax.print.SimpleDoc("line1\nline2", javax.print.DocFlavor.STRING.TEXT_PLAIN, null);
        check(sd.getDocFlavor() == javax.print.DocFlavor.STRING.TEXT_PLAIN, "flavor same reference");
        check("line1\nline2".equals(sd.getPrintData()), "print data same reference for String");
        check(sd.getAttributes() == null, "null ctor attrs -> getAttributes() returns null");
        java.io.Reader r = sd.getReaderForText();
        StringBuilder sb = new StringBuilder();
        int ch; while ((ch = r.read()) != -1) sb.append((char) ch);
        check("line1\nline2".equals(sb.toString()), "getReaderForText full read-back [" + sb + "]");
        check(sd.getStreamForBytes() == null, "getStreamForBytes null for String rep class");
        byte[] pngHeader = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47};
        javax.print.SimpleDoc bd = new javax.print.SimpleDoc(pngHeader, javax.print.DocFlavor.BYTE_ARRAY.PNG, null);
        check(bd.getPrintData() == pngHeader, "byte data same reference");
        java.io.InputStream in = bd.getStreamForBytes();
        check(in.read() == 0x89 && in.read() == 0x50 && in.read() == 0x4E && in.read() == 0x47, "stream reads all 4 bytes in order");
        check(in.read() == -1, "stream exhausted");
        javax.print.attribute.HashDocAttributeSet das = new javax.print.attribute.HashDocAttributeSet();
        das.add(javax.print.attribute.standard.Chromaticity.MONOCHROME);
        javax.print.SimpleDoc withAttrs = new javax.print.SimpleDoc("x", javax.print.DocFlavor.STRING.TEXT_PLAIN, das);
        javax.print.attribute.DocAttributeSet view = withAttrs.getAttributes();
        check(view != das, "getAttributes returns view (not same reference)");
        check(view == withAttrs.getAttributes(), "view is cached (same reference on repeat calls)");
        check(view.containsValue(javax.print.attribute.standard.Chromaticity.MONOCHROME), "view sees source content");
        das.add(javax.print.attribute.standard.Sides.DUPLEX);
        check(view.containsKey(javax.print.attribute.standard.Sides.class), "live view: later source mutation visible");
        try { view.clear(); throw new AssertionError("expected UnmodifiableSetException on view write"); }
        catch (javax.print.attribute.UnmodifiableSetException e) { passed++; }
        expectIAE("null data", new Runnable() { public void run() { new javax.print.SimpleDoc(null, javax.print.DocFlavor.STRING.TEXT_PLAIN, null); } }, "null argument(s)");
        expectIAE("null flavor", new Runnable() { public void run() { new javax.print.SimpleDoc("x", null, null); } }, "null argument(s)");
        expectIAE("type mismatch", new Runnable() { public void run() { new javax.print.SimpleDoc("string data", javax.print.DocFlavor.BYTE_ARRAY.PNG, null); } }, "data is not of declared type");
        expectIAE("byte[] for String flavor", new Runnable() { public void run() { new javax.print.SimpleDoc(new byte[] {1}, javax.print.DocFlavor.STRING.TEXT_PLAIN, null); } }, "data is not of declared type");
        javax.print.SimpleDoc badCharset = new javax.print.SimpleDoc("x".getBytes("us-ascii"), new javax.print.DocFlavor("text/plain; charset=us-ascii", "[B"), null);
        check(badCharset.getStreamForBytes() != null, "matching charset stream OK");
        // Reader for byte-based doc is null (cannot produce chars)
        check(bd.getReaderForText() == null, "getReaderForText null for byte[] data");
        // getReaderForText for READER-backed doc passes the reader through
        java.io.StringReader sr = new java.io.StringReader("abc");
        javax.print.SimpleDoc rd = new javax.print.SimpleDoc(sr, new javax.print.DocFlavor("text/plain; charset=unicode", "java.io.Reader"), null);
        check(rd.getReaderForText() == sr || rd.getReaderForText().read() == 'a', "reader passthrough");
        check(rd.getStreamForBytes() == null, "getStreamForBytes null for Reader rep class");
        // CHAR_ARRAY doc
        javax.print.SimpleDoc cd = new javax.print.SimpleDoc(new char[] {'h', 'i'}, javax.print.DocFlavor.CHAR_ARRAY.TEXT_PLAIN, null);
        check(cd.getReaderForText().read() == 'h' && cd.getReaderForText().read() == 'i', "char array reader");
        check(cd.getPrintData() instanceof char[], "char[] data");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }

}
