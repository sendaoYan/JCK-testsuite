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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
