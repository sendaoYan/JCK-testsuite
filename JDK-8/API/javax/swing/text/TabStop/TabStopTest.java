/*
 * @test
 * @summary javax.swing.text.TabStop conformance: constants / constructors / equals-hashCode / serialization (headless-safe).
 * @run main TabStopTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.text.TabStop;

public class TabStopTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testConstants() {
        check(TabStop.ALIGN_LEFT == 0, "ALIGN_LEFT 0");
        check(TabStop.ALIGN_RIGHT == 1, "ALIGN_RIGHT 1");
        check(TabStop.ALIGN_CENTER == 2, "ALIGN_CENTER 2");
        check(TabStop.ALIGN_DECIMAL == 4, "ALIGN_DECIMAL 4");
        check(TabStop.ALIGN_BAR == 5, "ALIGN_BAR 5");
        check(TabStop.LEAD_NONE == 0, "LEAD_NONE 0");
        check(TabStop.LEAD_DOTS == 1, "LEAD_DOTS 1");
        check(TabStop.LEAD_HYPHENS == 2, "LEAD_HYPHENS 2");
        check(TabStop.LEAD_UNDERLINE == 3, "LEAD_UNDERLINE 3");
        check(TabStop.LEAD_THICKLINE == 4, "LEAD_THICKLINE 4");
        check(TabStop.LEAD_EQUALS == 5, "LEAD_EQUALS 5");
    }

    static void testConstructors() {
        TabStop a = new TabStop(10.5f);
        check(a.getPosition() == 10.5f, "single-arg position");
        check(a.getAlignment() == TabStop.ALIGN_LEFT, "single-arg default align left");
        check(a.getLeader() == TabStop.LEAD_NONE, "single-arg default leader none");

        TabStop b = new TabStop(72f, TabStop.ALIGN_DECIMAL, TabStop.LEAD_DOTS);
        check(b.getPosition() == 72f, "three-arg position");
        check(b.getAlignment() == TabStop.ALIGN_DECIMAL, "three-arg alignment");
        check(b.getLeader() == TabStop.LEAD_DOTS, "three-arg leader");

        // No validation: negative position and out-of-range alignment/leader are stored as-is.
        TabStop c = new TabStop(-5f, 12345, -99);
        check(c.getPosition() == -5f, "negative position accepted");
        check(c.getAlignment() == 12345, "arbitrary alignment accepted");
        check(c.getLeader() == -99, "arbitrary leader accepted");

        TabStop d = new TabStop(Float.NaN);
        check(Float.isNaN(d.getPosition()), "NaN position stored");
    }

    static void testEqualsHashCode() {
        TabStop a = new TabStop(10f, TabStop.ALIGN_RIGHT, TabStop.LEAD_DOTS);
        TabStop b = new TabStop(10f, TabStop.ALIGN_RIGHT, TabStop.LEAD_DOTS);
        TabStop c = new TabStop(11f, TabStop.ALIGN_RIGHT, TabStop.LEAD_DOTS);
        TabStop d = new TabStop(10f, TabStop.ALIGN_LEFT, TabStop.LEAD_DOTS);
        TabStop e = new TabStop(10f, TabStop.ALIGN_RIGHT, TabStop.LEAD_NONE);
        check(a.equals(b), "equal TabStops");
        check(a.hashCode() == b.hashCode(), "equal hashCodes");
        check(!a.equals(c), "different position not equal");
        check(!a.equals(d), "different alignment not equal");
        check(!a.equals(e), "different leader not equal");
        check(a.equals(a), "reflexive");
        check(!a.equals(null), "not equal null");
        check(!a.equals(new Object()), "not equal other type");
    }

    static void testToString() {
        TabStop left = new TabStop(10f);
        TabStop right = new TabStop(10f, TabStop.ALIGN_RIGHT, TabStop.LEAD_DOTS);
        check(left.toString().contains("tab @10.0"), "left toString contains position");
        check(right.toString().contains("right"), "right toString contains alignment");
        check(right.toString().contains("w/leaders"), "leader toString marker");
    }

    static void testSerialization() throws Exception {
        TabStop t = new TabStop(37.5f, TabStop.ALIGN_CENTER, TabStop.LEAD_HYPHENS);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(t);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TabStop r = (TabStop) ois.readObject();
        check(r != t, "round-trip distinct instance");
        check(r.getPosition() == 37.5f, "round-trip position");
        check(r.getAlignment() == TabStop.ALIGN_CENTER, "round-trip alignment");
        check(r.getLeader() == TabStop.LEAD_HYPHENS, "round-trip leader");
        check(r.equals(t), "round-trip equals");
        check(r.hashCode() == t.hashCode(), "round-trip hashCode");
    }

    public static void main(String[] args) throws Exception {
        testConstants();
        testConstructors();
        testEqualsHashCode();
        testToString();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
