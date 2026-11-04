/*
 * @test
 * @summary javax.swing.UIDefaults conformance: defaults map, lazy/active values, typed getters (headless-safe).
 * @run main UIDefaultsTest
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class UIDefaultsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown");
    }

    static void testBasics() {
        UIDefaults d = new UIDefaults();
        check(d.size() == 0, "empty size");
        d.put("k", "v");
        check("v".equals(d.get("k")), "put/get");
        check(d.size() == 1, "size");
        UIDefaults v = new UIDefaults(new Object[]{"k1", "v1", "k2", "v2"});
        check("v1".equals(v.get("k1")), "varargs ctor pair 1");
        check("v2".equals(v.get("k2")), "varargs ctor pair 2");
        check(v.size() == 2, "varargs ctor size");
        d.put("k", "v3");
        check("v3".equals(d.get("k")), "overwrite");
        check(d.containsKey("k"), "containsKey true");
        check(!d.containsKey("absent"), "containsKey false");
        check(d.get("absent") == null, "get absent null");
        check("v3".equals(d.remove("k")), "remove returns old value");
        check(!d.containsKey("k"), "removed key gone");
        check(d.remove("absent") == null, "remove absent returns null");
        checkThrows(NullPointerException.class, () -> d.put(null, "x"));
    }

    static void testPropertyEvents() {
        UIDefaults d = new UIDefaults();
        final List<PropertyChangeEvent> evts = new ArrayList<PropertyChangeEvent>();
        d.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { evts.add(e); }
        });
        d.put("kx", "v1");
        check(evts.size() == 1 && "kx".equals(evts.get(0).getPropertyName()),
                "single put fires event named after the key");
        evts.clear();
        d.putDefaults(new Object[]{"dk", "dv"});
        check(evts.size() == 1 && "UIDefaults".equals(evts.get(0).getPropertyName()),
                "putDefaults fires one UIDefaults event");
        evts.clear();
        // listener removal: add then remove explicitly, no further events
        final boolean[] fired = {false};
        java.beans.PropertyChangeListener l = new java.beans.PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { fired[0] = true; }
        };
        UIDefaults d2 = new UIDefaults();
        d2.addPropertyChangeListener(l);
        d2.removePropertyChangeListener(l);
        d2.put("q", 1);
        check(!fired[0], "removed listener not notified");
    }

    static void testLazyAndActive() {
        UIDefaults d = new UIDefaults();
        final Object marker = new Object();
        final int[] lazyCalls = {0};
        d.put("lazy", new UIDefaults.LazyValue() {
            public Object createValue(UIDefaults table) { lazyCalls[0]++; return marker; }
        });
        check(d.get("lazy") == marker, "LazyValue resolved on get");
        d.get("lazy");
        d.get("lazy");
        check(lazyCalls[0] == 1, "LazyValue created exactly once, then cached");
        check(d.get("lazy") == marker, "cached value returned on later gets");
        final int[] activeCalls = {0};
        d.put("active", new UIDefaults.ActiveValue() {
            public Object createValue(UIDefaults table) { activeCalls[0]++; return "AV" + activeCalls[0]; }
        });
        check("AV1".equals(d.get("active")), "ActiveValue computed on get");
        check("AV2".equals(d.get("active")), "ActiveValue recomputed on next get");
        check(activeCalls[0] == 2, "ActiveValue not cached");
    }

    static void testTypedGetters() {
        UIDefaults d = new UIDefaults();
        Font font = new FontUIResource("Dialog", Font.PLAIN, 11);
        d.put("font", font);
        check(d.getFont("font") == font, "getFont");
        check(d.getFont("missing") == null, "getFont missing null");
        d.put("fontWrong", "x");
        check(d.getFont("fontWrong") == null, "getFont wrong type null");
        Color color = new ColorUIResource(1, 2, 3);
        d.put("color", color);
        check(d.getColor("color") == color, "getColor");
        check(d.getColor("missing") == null, "getColor missing null");
        d.put("colorWrong", "x");
        check(d.getColor("colorWrong") == null, "getColor wrong type null");
        Insets insets = new InsetsUIResource(1, 2, 3, 4);
        d.put("insets", insets);
        check(d.getInsets("insets") == insets, "getInsets");
        check(d.getInsets("missing") == null, "getInsets missing null");
        d.put("insetsWrong", "x");
        check(d.getInsets("insetsWrong") == null, "getInsets wrong type null");
        Dimension dim = new DimensionUIResource(7, 8);
        d.put("dim", dim);
        check(d.getDimension("dim") == dim, "getDimension");
        check(d.getDimension("missing") == null, "getDimension missing null");
        d.put("str", "s");
        check("s".equals(d.getString("str")), "getString");
        check(d.getString("missing") == null, "getString missing null");
        d.put("strWrong", Integer.valueOf(5));
        check(d.getString("strWrong") == null, "getString wrong type null");
        d.put("i", Integer.valueOf(42));
        check(d.getInt("i") == 42, "getInt");
        check(d.getInt("missing") == 0, "getInt missing 0");
        d.put("iWrong", "x");
        check(d.getInt("iWrong") == 0, "getInt wrong type 0");
        d.put("b", Boolean.TRUE);
        check(d.getBoolean("b"), "getBoolean");
        check(!d.getBoolean("missing"), "getBoolean missing false");
        d.put("bWrong", "x");
        check(!d.getBoolean("bWrong"), "getBoolean wrong type false");
        BorderUIResource border = new BorderUIResource(new EmptyBorder(1, 2, 3, 4));
        d.put("border", border);
        check(d.getBorder("border") == border, "getBorder");
        check(d.getBorder("missing") == null, "getBorder missing null");
        IconUIResource icon = new IconUIResource(new javax.swing.ImageIcon());
        d.put("icon", icon);
        check(d.getIcon("icon") == icon, "getIcon");
        check(d.getIcon("missing") == null, "getIcon missing null");
        d.put("iconWrong", "x");
        check(d.getIcon("iconWrong") == null, "getIcon wrong type null");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testBasics();
        testPropertyEvents();
        testLazyAndActive();
        testTypedGetters();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
