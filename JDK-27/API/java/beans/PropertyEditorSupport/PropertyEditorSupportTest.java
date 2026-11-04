/*
 * @test
 * @summary java.beans.PropertyEditorSupport conformance: both ctors (default source is
 *          self, null source NPE), setSource/getSource incl. null, setValue/getValue
 *          identity, default getAsText (null -> null, toString otherwise), default
 *          setAsText (only while value is a String, else IllegalArgumentException with
 *          the text as message), "???" init string, null tags/custom-editor/paint
 *          defaults, paintValue(null,null) no-op, PropertyChangeListener add/remove/fire
 *          counting and event field contract (source = getSource(), null name/old/new),
 *          add-twice/remove-once semantics, fire with null source -> IAE, subclass
 *          overriding the full text contract, instance independence, not Serializable.
 * @run main PropertyEditorSupportTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

public class PropertyEditorSupportTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyEditorSupport");
        check(PropertyEditor.class.isAssignableFrom(c), "implements PropertyEditor");
        check(!c.isInterface(), "is a class");
        check(c.getSuperclass() == Object.class, "direct subclass of Object");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) names.add(m.getName());
        }
        for (String n : new String[]{"setValue", "getValue", "isPaintable", "paintValue",
                "getJavaInitializationString", "getAsText", "setAsText", "getTags",
                "getCustomEditor", "supportsCustomEditor", "addPropertyChangeListener",
                "removePropertyChangeListener", "firePropertyChange", "getSource", "setSource"}) {
            check(names.contains(n), "declares public " + n);
        }
        check(c.getConstructors().length == 2, "exactly two public constructors");
    }

    static void testLegacyBlackBox() {
        // legacy checks kept from earlier revisions of this test
        PropertyEditorSupport pes = new PropertyEditorSupport();
        pes.setValue("hello");
        check("hello".equals(pes.getValue()), "getValue");
        pes.setSource("src");
        check("src".equals(pes.getSource()), "getSource");
        check(!pes.isPaintable(), "not paintable");
        check(!pes.supportsCustomEditor(), "no custom editor");

        PropertyEditorSupport pes2 = new PropertyEditorSupport();
        check(pes2.getValue() == null, "value null default");
        check(pes2.getAsText() == null, "asText null default");
        check(!pes2.isPaintable(), "not paintable");
        check(!pes2.supportsCustomEditor(), "no custom editor");
        check(pes2.getTags() == null, "tags null");
        check("???".equals(pes2.getJavaInitializationString()), "javaInit ???");
        check(pes2.getCustomEditor() == null, "customEditor null");

        pes2.setValue("x");
        check("x".equals(pes2.getAsText()), "asText of String value");
        pes2.setAsText("y");
        check("y".equals(pes2.getValue()), "setAsText sets value");

        pes2.setValue(Integer.valueOf(5));
        check("5".equals(pes2.getAsText()), "asText of Integer value");
        boolean threw = false;
        try { pes2.setAsText("z"); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "setAsText non-String throws IllegalArgumentException");

        pes2.setSource("src");
        check("src".equals(pes2.getSource()), "getSource/setSource");

        final int[] cnt = new int[1];
        pes2.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { cnt[0]++; }
        });
        pes2.firePropertyChange();
        check(cnt[0] == 1, "firePropertyChange fires");
    }

    static void testCtorAndSource() throws Exception {
        PropertyEditorSupport dflt = new PropertyEditorSupport();
        check(dflt.getSource() == dflt, "default ctor uses the editor itself as source");
        Object src = new Object();
        PropertyEditorSupport withSrc = new PropertyEditorSupport(src);
        check(withSrc.getSource() == src, "source ctor stores source by identity");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new PropertyEditorSupport(null); }
        }, "source ctor rejects null");
        PropertyEditorSupport m = new PropertyEditorSupport();
        Object other = new Object();
        m.setSource(other);
        check(m.getSource() == other, "setSource replaces source");
        m.setSource(m);
        check(m.getSource() == m, "setSource accepts the editor itself");
        m.setSource(null);
        check(m.getSource() == null, "setSource accepts null (recorded impl behavior)");
        m.setSource(m);
    }

    static void testValueAndTextContract() throws Exception {
        PropertyEditorSupport p = new PropertyEditorSupport();
        check(p.getValue() == null, "value null default");
        check(p.getAsText() == null, "getAsText of null value is null");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new PropertyEditorSupport().setAsText("x"); }
        }, "setAsText on null value rejected");
        p.setValue("a");
        p.setAsText("b");
        check("b".equals(p.getValue()), "default setAsText stores the String itself");
        p.setAsText("c");
        check("c".equals(p.getValue()), "repeated setAsText keeps working while value is String");
        p.setValue(Integer.valueOf(5));
        check("5".equals(p.getAsText()), "getAsText uses value.toString()");
        try {
            p.setAsText("bad");
            check(false, "setAsText with non-String value must throw");
        } catch (IllegalArgumentException iae) {
            check("bad".equals(iae.getMessage()), "IAE message is the rejected text");
        }
        Object marker = new Object() { public String toString() { return "MARK"; } };
        p.setValue(marker);
        check(p.getValue() == marker, "identity round-trip for arbitrary object");
        check("MARK".equals(p.getAsText()), "getAsText delegates to toString");
        p.setValue("s");
        p.setAsText(null);
        check(p.getValue() == null, "setAsText(null) stores null while value is String");
        check("???".equals(p.getJavaInitializationString()), "init string stays ??? with value set");
        check("???".equals(p.getJavaInitializationString()), "init string constant across calls");
    }

    static void testFlagsAndPainting() throws Exception {
        PropertyEditorSupport p = new PropertyEditorSupport();
        check(!p.isPaintable(), "isPaintable false");
        check(!p.supportsCustomEditor(), "supportsCustomEditor false");
        check(p.getTags() == null, "getTags null");
        check(p.getCustomEditor() == null, "getCustomEditor null");
        p.setValue(Integer.valueOf(3));
        check(!p.isPaintable(), "isPaintable false with value");
        p.paintValue(null, null);
        check(true, "paintValue(null, null) is a no-op even headless");
        check("???".equals(p.getJavaInitializationString()), "init ???");
    }

    static void testPropertyChangeSemantics() throws Exception {
        final Object src = new Object();
        final PropertyEditorSupport p = new PropertyEditorSupport(src);
        final int[] cnt = new int[1];
        final String[] f = new String[5];
        PropertyChangeListener l = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                cnt[0]++;
                f[0] = String.valueOf(e.getSource() == src);
                f[1] = String.valueOf(e.getPropertyName());
                f[2] = String.valueOf(e.getOldValue());
                f[3] = String.valueOf(e.getNewValue());
                f[4] = String.valueOf(e.getPropagationId());
            }
        };
        p.firePropertyChange();
        check(cnt[0] == 0, "fire with no listeners is a no-op");
        p.addPropertyChangeListener(l);
        p.setValue("one");
        check(cnt[0] == 1, "setValue fires one event");
        check("true".equals(f[0]), "event source is getSource()");
        check("null".equals(f[1]) && "null".equals(f[2]) && "null".equals(f[3]),
                "event carries null name/old/new");
        check("null".equals(f[4]), "propagationId null");
        p.setValue("one");
        check(cnt[0] == 2, "setValue fires even for an equal value");
        p.setValue(null);
        check(cnt[0] == 3, "setValue(null) fires as well");
        p.firePropertyChange();
        check(cnt[0] == 4, "explicit firePropertyChange");
        p.addPropertyChangeListener(l);
        cnt[0] = 0;
        p.firePropertyChange();
        check(cnt[0] == 2, "same listener added twice is notified twice");
        p.removePropertyChangeListener(l);
        cnt[0] = 0;
        p.firePropertyChange();
        check(cnt[0] == 1, "one removal leaves one notification");
        p.removePropertyChangeListener(l);
        cnt[0] = 0;
        p.firePropertyChange();
        check(cnt[0] == 0, "second removal stops notifications");
        p.removePropertyChangeListener(l);
        p.removePropertyChangeListener(null);
        p.removePropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {}
        });
        check(true, "removing never-added or null listeners is a no-op");
        PropertyEditorSupport q = new PropertyEditorSupport();
        q.addPropertyChangeListener(l);
        p.firePropertyChange();
        check(cnt[0] == 0, "listener lists are per-instance");
        p.addPropertyChangeListener(null);
        check(true, "addPropertyChangeListener(null) does not throw (recorded impl behavior)");
        p.removePropertyChangeListener(null);
        check(true, "removePropertyChangeListener(null) no-op");
    }

    static void testFireWithNullSource() throws Exception {
        final PropertyEditorSupport p = new PropertyEditorSupport();
        p.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {}
        });
        p.setSource(null);
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Throwable { p.firePropertyChange(); }
        }, "fire with null source rejected by PropertyChangeEvent (\"null source\")");
        p.setSource(p);
    }

    static void testSubclassContract() throws Exception {
        class CodeEditor extends PropertyEditorSupport {
            CodeEditor(Object source) { super(source); }
            private String code = "";
            @Override public String getAsText() { return code; }
            @Override public void setAsText(String text) { code = text; firePropertyChange(); }
            @Override public String[] getTags() { return new String[]{"A", "B"}; }
            @Override public String getJavaInitializationString() { return "Code(" + code + ")"; }
            @Override public boolean isPaintable() { return true; }
        }
        Object src = new Object();
        CodeEditor ce = new CodeEditor(src);
        PropertyEditor asIfc = ce;
        asIfc.setAsText("K1");
        check("K1".equals(asIfc.getAsText()), "subclass text contract through interface reference");
        check("Code(K1)".equals(asIfc.getJavaInitializationString()), "subclass init string");
        String[] tags = asIfc.getTags();
        check(tags != null && tags.length == 2 && "A".equals(tags[0]), "subclass tags");
        check(asIfc.isPaintable(), "subclass can declare paintable");
        check(!asIfc.supportsCustomEditor(), "custom editor default unchanged");
        final int[] cnt = new int[1];
        final Object[] seen = new Object[1];
        ce.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { cnt[0]++; seen[0] = e.getSource(); }
        });
        ce.setAsText("K2");
        check(cnt[0] == 1, "subclass firePropertyChange notifies");
        check(seen[0] == src, "event source is the ctor-supplied source");
        ce.setValue("raw");
        check("raw".equals(ce.getValue()), "inherited setValue works beside overrides");
        check("K2".equals(ce.getAsText()), "getAsText override wins over inherited default");
    }

    static void testInstanceIndependence() throws Exception {
        PropertyEditorSupport a = new PropertyEditorSupport();
        PropertyEditorSupport b = new PropertyEditorSupport();
        a.setValue("1");
        b.setValue("2");
        check("1".equals(a.getValue()) && "2".equals(b.getValue()), "values independent");
        check(a.getSource() != b.getSource(), "default sources are distinct objects");
        a.setSource("A");
        check("A".equals(a.getSource()) && b.getSource() == b, "setSource independent");
        final int[] ca = new int[1];
        final int[] cb = new int[1];
        a.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { ca[0]++; }
        });
        b.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { cb[0]++; }
        });
        a.setValue("1b");
        check(ca[0] == 1 && cb[0] == 0, "listener sets independent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testLegacyBlackBox();
        testCtorAndSource();
        testValueAndTextContract();
        testFlagsAndPainting();
        testPropertyChangeSemantics();
        testFireWithNullSource();
        testSubclassContract();
        testInstanceIndependence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
