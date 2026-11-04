/*
 * @test
 * @summary javax.swing.text.TabSet conformance: constructor copy / lookup / equals-hashCode / serialization (headless-safe).
 * @run main TabSetTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public class TabSetTest {
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

    static void testConstructor() {
        TabStop[] arr = { new TabStop(10f), new TabStop(20f), new TabStop(30f) };
        TabSet s = new TabSet(arr);
        check(s.getTabCount() == 3, "count 3");
        check(s.getTab(0).getPosition() == 10f, "tab0");
        check(s.getTab(2).getPosition() == 30f, "tab2");

        // Defensive copy: mutating the source array does not affect the TabSet.
        arr[0] = new TabStop(999f);
        check(s.getTab(0).getPosition() == 10f, "defensive copy");

        // null array is legal -> empty TabSet.
        TabSet empty = new TabSet(null);
        check(empty.getTabCount() == 0, "null array empty");
        expectEx(IllegalArgumentException.class, () -> empty.getTab(0), "empty getTab IAE");
        check(empty.getTabAfter(5f) == null, "empty getTabAfter null");
        check(empty.getTabIndexAfter(5f) == -1, "empty getTabIndexAfter -1");
    }

    static void testGetTabBounds() {
        TabSet s = new TabSet(new TabStop[] { new TabStop(10f), new TabStop(20f), new TabStop(30f) });
        expectEx(IllegalArgumentException.class, () -> s.getTab(-1), "getTab -1 IAE");
        expectEx(IllegalArgumentException.class, () -> s.getTab(3), "getTab 3 IAE");
        expectEx(IllegalArgumentException.class, () -> s.getTab(100), "getTab 100 IAE");
    }

    static void testLookup() {
        TabStop t10 = new TabStop(10f);
        TabStop t20 = new TabStop(20f);
        TabStop t30 = new TabStop(30f);
        TabSet s = new TabSet(new TabStop[] { t10, t20, t30 });

        check(s.getTabAfter(5f) == t10, "getTabAfter 5 -> 10");
        // A tab exactly at the requested location is itself returned ("at or after").
        check(s.getTabAfter(10f) == t10, "getTabAfter 10 -> tab at 10");
        check(s.getTabAfter(10.001f) == t20, "getTabAfter 10.001 -> 20");
        check(s.getTabAfter(19.9f) == t20, "getTabAfter 19.9 -> 20");
        check(s.getTabAfter(-5f) == t10, "getTabAfter -5 -> first tab");
        check(s.getTabAfter(30f) == t30, "getTabAfter 30 -> tab at 30");
        check(s.getTabAfter(30.001f) == null, "getTabAfter 30.001 -> null");
        check(s.getTabAfter(40f) == null, "getTabAfter 40 -> null");

        check(s.getTabIndexAfter(5f) == 0, "getTabIndexAfter 5 -> 0");
        check(s.getTabIndexAfter(10f) == 0, "getTabIndexAfter 10 -> 0 (tab at location)");
        check(s.getTabIndexAfter(10.001f) == 1, "getTabIndexAfter 10.001 -> 1");
        check(s.getTabIndexAfter(30f) == 2, "getTabIndexAfter 30 -> 2 (tab at location)");
        check(s.getTabIndexAfter(30.001f) == -1, "getTabIndexAfter 30.001 -> -1");

        // getTabIndex compares by identity (==), not equals.
        check(s.getTabIndex(t10) == 0, "getTabIndex identity 0");
        check(s.getTabIndex(t30) == 2, "getTabIndex identity 2");
        check(s.getTabIndex(new TabStop(20f)) == -1, "getTabIndex equal-but-not-identical -1");
        check(s.getTabIndex(null) == -1, "getTabIndex null -1");
    }

    static void testEqualsHashCode() {
        TabSet a = new TabSet(new TabStop[] { new TabStop(10f), new TabStop(20f) });
        TabSet b = new TabSet(new TabStop[] { new TabStop(10f), new TabStop(20f) });
        TabSet c = new TabSet(new TabStop[] { new TabStop(10f) });
        TabSet d = new TabSet(new TabStop[] { new TabStop(10f), new TabStop(21f) });
        check(a.equals(b), "equal TabSets");
        check(a.hashCode() == b.hashCode(), "equal hashCodes");
        check(a.equals(a), "reflexive");
        check(!a.equals(c), "different count not equal");
        check(!a.equals(d), "different tab not equal");
        check(!a.equals(null), "not equal null");
        check(!a.equals("x"), "not equal other type");
        // hashCode is cached; calling twice returns the same value.
        check(a.hashCode() == a.hashCode(), "hashCode cached/stable");
    }

    static void testToString() {
        TabSet s = new TabSet(new TabStop[] { new TabStop(10f), new TabStop(20f) });
        String str = s.toString();
        check(str.startsWith("[ "), "toString starts [");
        check(str.endsWith(" ]"), "toString ends ]");
        check(str.contains("tab @10.0"), "toString contains first tab");
        check(str.contains("tab @20.0"), "toString contains second tab");
        check(new TabSet(null).toString().equals("[  ]"), "empty toString");
    }

    static void testSerialization() throws Exception {
        TabSet s = new TabSet(new TabStop[] { new TabStop(10f), new TabStop(20f, TabStop.ALIGN_DECIMAL, TabStop.LEAD_DOTS) });
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(s);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TabSet r = (TabSet) ois.readObject();
        check(r != s, "round-trip distinct instance");
        check(r.equals(s), "round-trip equals");
        check(r.getTabCount() == 2, "round-trip count");
        check(r.getTab(1).getPosition() == 20f, "round-trip tab position");
        check(r.getTab(1).getAlignment() == TabStop.ALIGN_DECIMAL, "round-trip tab alignment");
        check(r.getTab(1).getLeader() == TabStop.LEAD_DOTS, "round-trip tab leader");
    }

    public static void main(String[] args) throws Exception {
        testConstructor();
        testGetTabBounds();
        testLookup();
        testEqualsHashCode();
        testToString();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
