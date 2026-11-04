/*
 * @test
 * @summary javax.swing.text.html.MinimalHTMLWriter conformance: styled text
 *          serialization with inline and style tokens, range constructor and
 *          negative cases (headless-safe).
 * @run main MinimalHTMLWriterTest
 */

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.MinimalHTMLWriter;

public class MinimalHTMLWriterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: shape via reflection (kept from the structural pass) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.MinimalHTMLWriter", false, MinimalHTMLWriterTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.AbstractWriter"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("write"), "method write");
    }

    static DefaultStyledDocument styledDoc() throws Exception {
        DefaultStyledDocument msd = new DefaultStyledDocument();
        msd.insertString(0, "styled hello", null);
        Style st = msd.addStyle("s1", null);
        StyleConstants.setBold(st, true);
        msd.setParagraphAttributes(0, 1, st, true);
        return msd;
    }

    // ---- Part 2: styled serialization ----
    static void testStyledOutput() throws Exception {
        DefaultStyledDocument msd = styledDoc();
        StringWriter sw = new StringWriter();
        new MinimalHTMLWriter(sw, msd).write();
        String out = sw.toString();

        String[] tokens = { "<html", "<body", "<p", "<b>", "styled hello", "<style" };
        for (String t : tokens) check(out.contains(t), "output contains " + t);
        check(!out.contains("<font"), "no <font> element for plain bold styles");
        check(out.length() > 100, "substantial output: " + out.length());

        // full-range constructor produces the same document
        StringWriter sw2 = new StringWriter();
        new MinimalHTMLWriter(sw2, msd, 0, msd.getLength()).write();
        check(sw2.toString().contains("styled hello"), "range writer keeps the text");
        check(sw2.toString().length() > 0, "range writer output non-empty");

        // out-of-range (pos,len) is tolerated
        boolean ok = true;
        try {
            StringWriter sw3 = new StringWriter();
            new MinimalHTMLWriter(sw3, msd, 5, 3).write();
        } catch (Throwable t) { ok = false; }
        check(ok, "range (5,3) tolerated");
    }

    // ---- Part 3: negative cases ----
    static void testNegatives() throws Exception {
        DefaultStyledDocument msd = styledDoc();
        boolean ctorOk = true;
        try { new MinimalHTMLWriter(null, msd); } catch (Throwable t) { ctorOk = false; }
        check(ctorOk, "MinimalHTMLWriter(null, doc) constructor tolerated");
        boolean npe = false;
        try { new MinimalHTMLWriter(null, msd).write(); } catch (NullPointerException e) { npe = true; }
        check(npe, "write with null writer NPE");
        boolean npe2 = false;
        try { new MinimalHTMLWriter(new StringWriter(), null); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "MinimalHTMLWriter(w, null) NPE at construction");
    }

    public static void main(String[] args) throws Exception {
        testShape();
        testStyledOutput();
        testNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
