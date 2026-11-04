/*
 * @test
 * @summary javax.swing.text.SimpleAttributeSet conformance: full MutableAttributeSet contract + resolve parent + EMPTY + equals/clone/serialization (headless-safe).
 * @run main SimpleAttributeSetTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;

public class SimpleAttributeSetTest {
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

    static void testBasic() {
        SimpleAttributeSet a = new SimpleAttributeSet();
        check(a.isEmpty(), "new set empty");
        check(a.getAttributeCount() == 0, "new set count 0");
        check(a.getResolveParent() == null, "new set no resolve parent");
        check(!a.isDefined("k"), "not defined");
        check(a.getAttribute("k") == null, "missing attribute null");
        check(a.getAttribute(null) == null, "null key null");

        a.addAttribute("k", "v");
        check(!a.isEmpty(), "not empty after add");
        check(a.getAttributeCount() == 1, "count 1");
        check("v".equals(a.getAttribute("k")), "get");
        check(a.isDefined("k"), "defined");

        // same key overwrites
        a.addAttribute("k", "v2");
        check(a.getAttributeCount() == 1, "overwrite keeps count");
        check("v2".equals(a.getAttribute("k")), "overwritten value");

        a.removeAttribute("k");
        check(a.getAttributeCount() == 0, "count 0 after remove");
        check(a.getAttribute("k") == null, "removed value null");
        a.removeAttribute("k"); // removing again is a no-op
        check(a.getAttributeCount() == 0, "double remove no-op");
        a.removeAttribute("missing");
        check(a.getAttributeCount() == 0, "remove missing no-op");
    }

    static void testCopyConstructor() {
        SimpleAttributeSet src = new SimpleAttributeSet();
        src.addAttribute("a", "1");
        src.addAttribute("b", "2");
        SimpleAttributeSet copy = new SimpleAttributeSet(src);
        check(copy.getAttributeCount() == 2, "copy ctor copies entries");
        check("1".equals(copy.getAttribute("a")), "copy a");
        check("2".equals(copy.getAttribute("b")), "copy b");
        src.addAttribute("c", "3");
        check(copy.getAttribute("c") == null, "copy independent of source");
    }

    static void testQueries() {
        SimpleAttributeSet a = new SimpleAttributeSet();
        a.addAttribute("a", "1");
        a.addAttribute("b", "2");

        check(a.containsAttribute("a", "1"), "containsAttribute match");
        check(!a.containsAttribute("a", "2"), "containsAttribute value mismatch");
        check(!a.containsAttribute("z", "1"), "containsAttribute missing key");

        SimpleAttributeSet sub = new SimpleAttributeSet();
        sub.addAttribute("a", "1");
        check(a.containsAttributes(sub), "containsAttributes subset");
        check(!sub.containsAttributes(a), "superset not contained");
        check(a.containsAttributes(a), "containsAttributes self");
        check(new SimpleAttributeSet().containsAttributes(new SimpleAttributeSet()), "empty in empty");

        check(a.isEqual(a), "isEqual self");
        SimpleAttributeSet same = new SimpleAttributeSet();
        same.addAttribute("a", "1");
        same.addAttribute("b", "2");
        check(a.isEqual(same), "isEqual same content");
        check(!a.isEqual(sub), "isEqual different count");
        sub.addAttribute("a", "X");
        SimpleAttributeSet cnt = new SimpleAttributeSet();
        cnt.addAttribute("a", "1");
        cnt.addAttribute("z", "9");
        check(!a.isEqual(cnt), "isEqual same count different content");

        // names enumeration
        Vector<Object> names = new Vector<Object>();
        for (Object n : Collections_list(a)) names.add(n);
        check(names.size() == 2, "names count");
        check(names.contains("a") && names.contains("b"), "names content");
    }

    static java.util.List<?> Collections_list(AttributeSet a) {
        Vector<Object> v = new Vector<Object>();
        java.util.Enumeration<?> e = a.getAttributeNames();
        while (e.hasMoreElements()) v.add(e.nextElement());
        return v;
    }

    static void testResolveParent() {
        SimpleAttributeSet parent = new SimpleAttributeSet();
        parent.addAttribute("pk", "pv");
        parent.addAttribute("shared", "fromParent");

        SimpleAttributeSet child = new SimpleAttributeSet();
        child.addAttribute("own", "ov");
        child.setResolveParent(parent);

        check(child.getResolveParent() == parent, "resolve parent identity");
        // The resolve-parent link itself is stored as an attribute entry.
        check(child.getAttributeCount() == 2, "count includes resolve entry");
        check(!child.isEmpty(), "resolve entry makes set non-empty");
        // Lookups fall through to the parent.
        check("pv".equals(child.getAttribute("pk")), "lookup falls through to parent");
        check(!child.isDefined("pk"), "parent attribute not defined locally");
        check(child.isDefined("own"), "own attribute defined");
        check(child.containsAttribute("pk", "pv"), "containsAttribute through parent");
        check(child.containsAttributes(parent), "containsAttributes through parent");

        // Local value shadows parent value.
        child.addAttribute("shared", "ownValue");
        check("ownValue".equals(child.getAttribute("shared")), "local shadows parent");

        // isEqual asymmetry with resolve entries: counts match and the resolved
        // value satisfies containsAttributes in one direction only.
        SimpleAttributeSet child2 = new SimpleAttributeSet();
        child2.setResolveParent(parent);
        SimpleAttributeSet flat = new SimpleAttributeSet();
        flat.addAttribute("pk", "pv");
        check(child2.getAttributeCount() == 1, "resolve-only child count 1");
        check(flat.getAttributeCount() == 1, "flat count 1");
        check(child2.isEqual(flat), "resolve-only child.isEqual(flat) true");
        check(!flat.isEqual(child2), "flat.isEqual(child) false (resolve entry value)");
        check(child2.equals(flat), "equals delegates to isEqual");
        check(child.hashCode() == child.hashCode(), "hashCode stable");

        child.setResolveParent(null);
        check(child.getResolveParent() == null, "null parent clears link");
        check(child.getAttribute("pk") == null, "no fallthrough after clear");
    }

    static void testRemoveVariants() {
        SimpleAttributeSet m = new SimpleAttributeSet();
        m.addAttribute("a", "1");
        m.addAttribute("b", "2");
        m.addAttribute("c", "3");
        Vector<Object> names = new Vector<Object>();
        names.add("a");
        names.add("c");
        m.removeAttributes(names.elements());
        check(m.getAttributeCount() == 1, "removeAttributes(Enumeration) count");
        check("2".equals(m.getAttribute("b")), "b survives enumeration removal");

        // removeAttributes(AttributeSet) removes only entries whose values match.
        m.addAttribute("a", "1");
        SimpleAttributeSet mismatch = new SimpleAttributeSet();
        mismatch.addAttribute("a", "WRONG");
        mismatch.addAttribute("b", "2");
        m.removeAttributes(mismatch);
        check("1".equals(m.getAttribute("a")), "value mismatch keeps entry");
        check(m.getAttribute("b") == null, "value match removes entry");

        // removeAttributes(this) clears the set.
        SimpleAttributeSet self = new SimpleAttributeSet();
        self.addAttribute("x", "1");
        self.removeAttributes(self);
        check(self.getAttributeCount() == 0, "removeAttributes(this) clears");

        // addAttributes(null) throws NPE.
        expectEx(NullPointerException.class, () -> new SimpleAttributeSet().addAttributes(null), "addAttributes(null) NPE");
    }

    static void testEmpty() {
        AttributeSet e = SimpleAttributeSet.EMPTY;
        check(e.getAttributeCount() == 0, "EMPTY count 0");
        check(!e.isDefined("k"), "EMPTY not defined");
        check(e.getAttribute("k") == null, "EMPTY get null");
        check(!e.containsAttribute("k", "v"), "EMPTY containsAttribute false");
        check(e.containsAttributes(new SimpleAttributeSet()), "EMPTY containsAttributes empty");
        check(!e.containsAttributes(newAttr("k", "v")), "EMPTY containsAttributes non-empty false");
        check(e.isEqual(new SimpleAttributeSet()), "EMPTY isEqual empty");
        check(!e.isEqual(newAttr("k", "v")), "EMPTY isEqual non-empty false");
        check(e.copyAttributes() == e, "EMPTY copy is itself");
        check(e.getResolveParent() == null, "EMPTY no resolve parent");
        check(e.equals(new SimpleAttributeSet()), "EMPTY equals empty set");
        check(new SimpleAttributeSet().equals(e), "empty set equals EMPTY");
        check(!e.equals(newAttr("k", "v")), "EMPTY not equals non-empty");
        check(e.hashCode() == 0, "EMPTY hashCode 0");
        check(!e.getAttributeNames().hasMoreElements(), "EMPTY names empty");
    }

    static SimpleAttributeSet newAttr(Object k, Object v) {
        SimpleAttributeSet s = new SimpleAttributeSet();
        s.addAttribute(k, v);
        return s;
    }

    static void testEqualsHashCodeToString() {
        SimpleAttributeSet a = newAttr("k", "v");
        SimpleAttributeSet b = newAttr("k", "v");
        SimpleAttributeSet c = newAttr("k", "other");
        check(a.equals(b), "equal sets");
        check(a.hashCode() == b.hashCode(), "equal hashCodes");
        check(!a.equals(c), "different values not equal");
        check(a.equals(a), "reflexive");
        check(!a.equals(null), "null not equal");
        check(!a.equals("string"), "other type not equal");
        // A SimpleAttributeSet equals any AttributeSet implementation with the same content.
        javax.swing.text.StyleContext sc = new javax.swing.text.StyleContext();
        AttributeSet small = sc.addAttribute(sc.getEmptySet(), "k", "v");
        check(a.equals(small), "cross-implementation equals (Simple vs Small)");
        check(small.equals(a), "cross-implementation equals symmetric");
        // toString lists name=value pairs.
        String s = a.toString();
        check(s.contains("k=v"), "toString contains pair");
        check(new SimpleAttributeSet().toString().equals(""), "empty toString");
    }

    static void testCloneAndSerialization() throws Exception {
        SimpleAttributeSet a = newAttr("k", "v");
        a.setResolveParent(newAttr("p", "q"));

        Object o = a.clone();
        check(o instanceof SimpleAttributeSet, "clone type");
        SimpleAttributeSet cl = (SimpleAttributeSet) o;
        check(cl != a, "clone distinct");
        check(cl.getAttributeCount() == a.getAttributeCount(), "clone same count");
        check("v".equals(cl.getAttribute("k")), "clone same value");
        cl.addAttribute("extra", "e");
        check(a.getAttribute("extra") == null, "clone structurally independent");

        AttributeSet copy = a.copyAttributes();
        check(copy != a, "copy distinct");
        check(copy.getAttributeCount() == a.getAttributeCount(), "copy same count");
        check(copy.isEqual(a), "copy isEqual");
        a.addAttribute("later", "l");
        check(copy.getAttribute("later") == null, "copy unaffected by later mutation");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(a);
        oos.close();
        SimpleAttributeSet r = (SimpleAttributeSet) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(r != a, "round-trip distinct");
        check("v".equals(r.getAttribute("k")), "round-trip value");
        check(r.getResolveParent() != null, "round-trip keeps resolve parent");
        check("q".equals(r.getResolveParent().getAttribute("p")), "round-trip parent content");
        r.addAttribute("post", "1");
        check("1".equals(r.getAttribute("post")), "round-trip still mutable");

        // EMPTY serializes into an empty set.
        bos = new ByteArrayOutputStream();
        new ObjectOutputStream(bos).writeObject(SimpleAttributeSet.EMPTY);
        AttributeSet re = (AttributeSet) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(re.getAttributeCount() == 0, "EMPTY round-trip empty");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testCopyConstructor();
        testQueries();
        testResolveParent();
        testRemoveVariants();
        testEmpty();
        testEqualsHashCodeToString();
        testCloneAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
