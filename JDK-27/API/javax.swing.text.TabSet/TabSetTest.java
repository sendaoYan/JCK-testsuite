/*
 * @test
 * @summary javax.swing.text.TabSet conformance: white-box structure + black-box behavior.
 * @run main TabSetTest
 */

import java.lang.reflect.Modifier;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public class TabSetTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            check(type.isInstance(t), msg + " -> " + t.getClass().getName());
            return;
        }
        check(false, msg + " -> no exception");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.TabSet");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TabStop t10 = new TabStop(10f);
        TabStop t20 = new TabStop(20f, TabStop.ALIGN_CENTER, TabStop.LEAD_DOTS);
        TabStop[] arr = new TabStop[]{t10, t20};
        TabSet set = new TabSet(arr);

        check(set.getTabCount() == 2, "getTabCount");
        check(set.getTab(0) == t10, "getTab(0) identity");
        check(set.getTab(1) == t20, "getTab(1) identity");

        // Defensive copy of the input array.
        arr[0] = new TabStop(99f);
        check(set.getTab(0) == t10, "constructor copies input array");

        // getTabAfter / getTabIndexAfter.
        check(set.getTabAfter(5f) == t10, "getTabAfter(5)");
        check(set.getTabAfter(15f) == t20, "getTabAfter(15)");
        check(set.getTabAfter(25f) == null, "getTabAfter(25) null");
        check(set.getTabIndexAfter(5f) == 0, "getTabIndexAfter(5)");
        check(set.getTabIndexAfter(15f) == 1, "getTabIndexAfter(15)");
        check(set.getTabIndexAfter(25f) == -1, "getTabIndexAfter(25)");

        // getTabIndex uses object identity, not equality.
        check(set.getTabIndex(t10) == 0, "getTabIndex(t10)");
        check(set.getTabIndex(t20) == 1, "getTabIndex(t20)");
        check(set.getTabIndex(new TabStop(10f)) == -1, "getTabIndex identity-based");

        // Out of range access.
        expectThrows(() -> set.getTab(-1), IllegalArgumentException.class, "getTab(-1)");
        expectThrows(() -> set.getTab(2), IllegalArgumentException.class, "getTab(2)");

        // equals / hashCode.
        TabSet same = new TabSet(new TabStop[]{new TabStop(10f), new TabStop(20f, TabStop.ALIGN_CENTER, TabStop.LEAD_DOTS)});
        check(set.equals(set), "equals reflexive");
        check(set.equals(same), "equals same content");
        check(same.equals(set), "equals symmetric");
        check(set.hashCode() == same.hashCode(), "equal objects equal hash");
        check(!set.equals(null), "equals null false");
        check(!set.equals("not a TabSet"), "equals non-TabSet false");
        check(!set.equals(new TabSet(new TabStop[]{t10})), "equals different count false");
        check(!set.equals(new TabSet(new TabStop[]{t10, new TabStop(21f)})), "equals different stop false");

        // toString.
        check(set.toString().equals("[ tab @10.0 - center tab @20.0 (w/leaders) ]"), "toString");

        // Empty / null-backed TabSet.
        TabSet empty = new TabSet(null);
        check(empty.getTabCount() == 0, "null tabs count 0");
        check(empty.getTabAfter(0f) == null, "empty getTabAfter null");
        check(empty.getTabIndexAfter(0f) == -1, "empty getTabIndexAfter -1");
        check(empty.toString().equals("[  ]"), "empty toString");
        expectThrows(() -> empty.getTab(0), IllegalArgumentException.class, "empty getTab(0)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
