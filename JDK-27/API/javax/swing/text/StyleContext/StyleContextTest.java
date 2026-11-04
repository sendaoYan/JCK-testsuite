/*
 * @test
 * @summary javax.swing.text.StyleContext conformance: style table + AttributeContext interning + fonts/colors + serialization (headless-safe).
 * @run main StyleContextTest
 */

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class StyleContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface Throwing { void run() throws Throwable; }
    static void expectEx(Class<? extends Throwable> ex, Throwing r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + ": expected " + ex.getName() + " but got " + t, t);
        }
        throw new AssertionError(m + ": expected " + ex.getName() + " but no exception thrown");
    }

    static Vector<String> names(StyleContext sc) {
        Vector<String> v = new Vector<String>();
        java.util.Enumeration<?> e = sc.getStyleNames();
        while (e.hasMoreElements()) v.add(String.valueOf(e.nextElement()));
        return v;
    }

    static void testDefaultContext() {
        StyleContext def = StyleContext.getDefaultStyleContext();
        check(def != null, "default context non-null");
        check(StyleContext.getDefaultStyleContext() == def, "default context singleton");
        check("default".equals(StyleContext.DEFAULT_STYLE), "DEFAULT_STYLE name");
        check(def.getStyle(StyleContext.DEFAULT_STYLE) != null, "default style exists");
        check(names(def).contains(StyleContext.DEFAULT_STYLE), "default in style names");
    }

    static void testStyleTable() {
        StyleContext sc = new StyleContext();
        check(sc.getStyle("nope") == null, "missing style null");
        check(names(sc).contains(StyleContext.DEFAULT_STYLE), "fresh context has default style");
        check(names(sc).size() == 1, "fresh context has exactly the default style");

        Style a = sc.addStyle("a", null);
        check(a != null, "addStyle returns style");
        check("a".equals(a.getName()), "style name");
        check(a instanceof StyleContext.NamedStyle, "style is NamedStyle");
        check(sc.getStyle("a") == a, "getStyle returns same instance");

        Style anon = sc.addStyle(null, null);
        check(anon != null, "anonymous style created");
        check(anon.getName() == null, "anonymous style name null");
        check(names(sc).size() == 2, "anonymous not registered (default + a only)");

        Style b = sc.addStyle("b", a);
        check(b.getResolveParent() == a, "parent recorded");
        // attribute resolution flows through the parent chain
        StyleConstants.setBold(a, true);
        check(StyleConstants.isBold(sc.getStyle("b")), "resolve through parent");
        check(!sc.getStyle("b").isDefined(StyleConstants.Bold), "parent value not local");

        sc.removeStyle("b");
        check(sc.getStyle("b") == null, "removed style gone");
        check(names(sc).contains("a"), "other style kept");
        sc.removeStyle("missing"); // no exception
        sc.removeStyle(null);      // no exception

        expectEx(NullPointerException.class, () -> sc.getStyle(null), "getStyle(null) NPE");
        // anonymous styles are fully usable attribute sets
        Style usable = sc.addStyle(null, null);
        usable.addAttribute("uk", "uv");
        check("uv".equals(usable.getAttribute("uk")), "anonymous style usable");
    }

    static void testChangeEvents() {
        StyleContext sc = new StyleContext();
        int[] fired = { 0 };
        sc.addChangeListener(e -> fired[0]++);
        check(sc.getChangeListeners().length == 1, "one change listener registered");
        int before = fired[0];
        Style s = sc.addStyle("ev", null);
        check(fired[0] == before + 1, "addStyle fires context change");
        sc.removeStyle("ev");
        check(fired[0] == before + 2, "removeStyle fires context change");
        // mutating an individual style does not fire the context-level event
        int before2 = fired[0];
        s.addAttribute("k", "v");
        check(fired[0] == before2, "style mutation does not fire context change");
        sc.removeChangeListener(e -> { });
        // removing a different listener instance leaves the original registered
        sc.addChangeListener(e -> fired[0]++);
        check(sc.getChangeListeners().length == 2, "listener add after remove");
    }

    static void testAttributeContext() {
        StyleContext sc = new StyleContext();
        AttributeSet empty = sc.getEmptySet();
        check(empty == SimpleAttributeSet.EMPTY, "empty set is SimpleAttributeSet.EMPTY");
        check(sc.getEmptySet() == empty, "empty set is stable");

        AttributeSet a = sc.addAttribute(empty, "k", "v");
        check(a.getAttributeCount() == 1, "context addAttribute count");
        check("v".equals(a.getAttribute("k")), "context addAttribute value");
        // interned: the same operation yields the same immutable instance
        check(a == sc.addAttribute(empty, "k", "v"), "interned identical");

        AttributeSet b = sc.addAttribute(a, "k", "v2");
        check("v2".equals(b.getAttribute("k")), "overwrite through context");
        check("v".equals(a.getAttribute("k")), "original immutable");

        AttributeSet r = sc.removeAttribute(b, "k");
        check(r.getAttributeCount() == 0, "context removeAttribute to empty");

        SimpleAttributeSet two = new SimpleAttributeSet();
        two.addAttribute("x", "1");
        two.addAttribute("y", "2");
        AttributeSet merged = sc.addAttributes(sc.addAttribute(empty, "x", "1"), two);
        check(merged.getAttributeCount() == 2, "addAttributes merge count");
        AttributeSet dropped = sc.removeAttributes(merged, java.util.Collections.enumeration(java.util.Arrays.asList("x")));
        check(dropped.getAttributeCount() == 1, "removeAttributes(Enumeration)");
        check("2".equals(dropped.getAttribute("y")), "surviving entry");
        AttributeSet dropped2 = sc.removeAttributes(merged, two);
        check(dropped2.getAttributeCount() == 0, "removeAttributes(AttributeSet)");

        sc.reclaim(empty); // must be a no-op, never throws
        check(true, "reclaim no-op");
    }

    static void testFontsAndColors() {
        StyleContext sc = new StyleContext();
        // same key returns the cached instance
        Font f1 = sc.getFont("Monospaced", Font.PLAIN, 12);
        Font f2 = sc.getFont("Monospaced", Font.PLAIN, 12);
        check(f1 == f2, "font cache identity");
        Font f3 = sc.getFont("Monospaced", Font.BOLD, 14);
        check(f3 != f1, "different key different font");
        check(f3.getSize() == 14 && f3.isBold(), "font(style,size) derived");

        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setBold(a, true);
        StyleConstants.setItalic(a, true);
        StyleConstants.setFontSize(a, 18);
        Font fa = sc.getFont(a);
        check(fa.getSize() == 18, "font(attrs) size");
        check(fa.isBold() && fa.isItalic(), "font(attrs) style");

        SimpleAttributeSet sup = new SimpleAttributeSet();
        StyleConstants.setFontSize(sup, 18);
        StyleConstants.setSuperscript(sup, true);
        check(sc.getFont(sup).getSize() == 16, "superscript shrinks size by 2");
        SimpleAttributeSet sub = new SimpleAttributeSet();
        StyleConstants.setFontSize(sub, 18);
        StyleConstants.setSubscript(sub, true);
        check(sc.getFont(sub).getSize() == 16, "subscript shrinks size by 2");

        check(sc.getForeground(new SimpleAttributeSet()).equals(Color.black), "default foreground");
        check(sc.getBackground(new SimpleAttributeSet()).equals(Color.black), "default background");
        SimpleAttributeSet colored = new SimpleAttributeSet();
        StyleConstants.setForeground(colored, Color.red);
        StyleConstants.setBackground(colored, Color.blue);
        check(sc.getForeground(colored) == Color.red, "foreground from attrs");
        check(sc.getBackground(colored) == Color.blue, "background from attrs");
    }

    static void testNamedStyleBehavior() {
        StyleContext sc = new StyleContext();
        Style s = sc.addStyle("ns", null);
        int[] fired = { 0 };
        s.addChangeListener(e -> fired[0]++);
        s.addAttribute("k", "v");
        s.addAttributes(new SimpleAttributeSet());
        s.removeAttribute("k");
        check(fired[0] == 3, "each mutation fires exactly one change event");
        check(((StyleContext.NamedStyle) s).getChangeListeners().length == 1, "style listener list");

        // getName reads the NameAttribute entry
        check("ns".equals(s.getName()), "getName from NameAttribute");
        s.addAttribute(StyleConstants.NameAttribute, "renamed");
        check("renamed".equals(s.getName()), "rename via NameAttribute");
        // setName(null) is a no-op
        ((StyleContext.NamedStyle) s).setName(null);
        check("renamed".equals(s.getName()), "setName(null) no-op");
        ((StyleContext.NamedStyle) s).setName("third");
        check("third".equals(s.getName()), "setName updates");

        AttributeSet copy = s.copyAttributes();
        check(copy instanceof StyleContext.NamedStyle, "copyAttributes returns NamedStyle");
        s.addAttribute("late", "1");
        check(copy.getAttribute("late") == null, "copy independent");

        check(s.toString().contains("NamedStyle"), "toString mentions NamedStyle");
        check(s.toString().contains("third"), "toString contains name");
    }

    static void testStaticAttributeKeys() {
        // StyleConstants keys are pre-registered for serialization
        StyleContext.registerStaticAttributeKey(StyleConstants.Bold); // idempotent
        Object ioFormat = StyleContext.getStaticAttributeKey(StyleConstants.Bold);
        check(String.valueOf(ioFormat).endsWith(".bold"), "io format ends with toString");
        check(StyleContext.getStaticAttribute(ioFormat) == StyleConstants.Bold, "registered key round trip");
        Object custom = new Object();
        StyleContext.registerStaticAttributeKey(custom);
        check(StyleContext.getStaticAttribute(StyleContext.getStaticAttributeKey(custom)) == custom, "custom key round trip");
        check(StyleContext.getStaticAttribute("unregistered") == null, "unregistered lookup null");
        check(StyleContext.getStaticAttribute(null) == null, "null lookup null");
    }

    static void testAttributeSetStream() throws Exception {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setFontSize(a, 9);
        a.addAttribute("str", "sv");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StyleContext.writeAttributeSet(new ObjectOutputStream(bos), a);
        SimpleAttributeSet rd = new SimpleAttributeSet();
        StyleContext.readAttributeSet(new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())), rd);
        check(StyleConstants.getFontSize(rd) == 9, "static stream round trip size");
        check("sv".equals(rd.getAttribute("str")), "static stream round trip custom");
        check(rd.getAttributeCount() == 2, "static stream round trip count");
    }

    static void testSerialization() throws Exception {
        StyleContext sc = new StyleContext();
        Style st = sc.addStyle("ser", null);
        StyleConstants.setFontSize(st, 22);
        StyleConstants.setBold(st, true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(sc);
        oos.close();
        StyleContext r = (StyleContext) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(r != sc, "round-trip distinct");
        Style rs = r.getStyle("ser");
        check(rs != null, "style survives round trip");
        check(StyleConstants.getFontSize(rs) == 22, "round trip font size");
        check(StyleConstants.isBold(rs), "round trip bold");
        check("ser".equals(rs.getName()), "round trip name");
    }

    public static void main(String[] args) throws Exception {
        testDefaultContext();
        testStyleTable();
        testChangeEvents();
        testAttributeContext();
        testFontsAndColors();
        testNamedStyleBehavior();
        testStaticAttributeKeys();
        testAttributeSetStream();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
