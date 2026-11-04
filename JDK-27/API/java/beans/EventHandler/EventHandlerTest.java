/*
 * @test
 * @summary java.beans.EventHandler conformance: ctor/getter contract, eager NPE validation
 *          (target/action/listenerInterface), dynamic-proxy nature, Object method handling
 *          on proxies, nullary action wiring for all listener methods, listenerMethodName
 *          filtering (non-matching methods are no-ops), event property extraction
 *          (plain/is/getter-by-name/empty-string/dot-path), action forms (direct method,
 *          setter name, property-name fallback with extracted arg, qualified a.b action),
 *          java.beans PropertyChangeListener integration, RuntimeException matrix at
 *          dispatch time, action return value pass-through.
 * @run main EventHandlerTest
 */

import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class EventHandlerTest {
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

    // ---- custom listener/event/sample types (no AWT involved) ----
    public static class Src {
        public String text = "s0";
        public boolean enabled = true;
        public String getText() { return text; }
        public void setText(String t) { text = t; }
        public boolean isEnabled() { return enabled; }
    }
    public static class Evt {
        private final String name; private final String tag; private final Src source;
        public Evt(String name, String tag, Src source) { this.name = name; this.tag = tag; this.source = source; }
        public String getName() { return name; }
        public String getTag() { return tag; }
        public Src getSource() { return source; }
    }
    public interface Lsn {
        Object first(Evt e);
        void second(Evt e);
    }
    public static class Tgt {
        public String got = "none";
        public int count;
        public Src sub = new Src();
        public Src getSub() { return sub; }
        public void fire() { count++; }
        public void record(String s) { got = s; count++; }
        public void recordEvent(Evt e) { got = "evt:" + e.getTag(); }
        public void takeBool(boolean b) { got = "b:" + b; }
        public void setEnabled(boolean b) { got = "enabled:" + b; }
        public void setLabel(String s) { got = "L:" + s; }
        public void label(String s) { got = "lbl:" + s; }
        public String giveBack() { return "back"; }
    }
    public static class SetterOnly {
        public String got = "none";
        public void setLabel(String s) { got = "L:" + s; }
    }
    public static class Boom {
        public final RuntimeException token = new IllegalStateException("boom");
        public void record(String s) { throw token; }
    }
    public static class Target {
        boolean fired;
        public void fire() { fired = true; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.EventHandler");
        check(java.lang.reflect.InvocationHandler.class.isAssignableFrom(c), "implements InvocationHandler");
        check(!c.isInterface(), "is a class");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) names.add(m.getName());
        }
        for (String n : new String[]{"invoke", "getTarget", "getAction", "getEventPropertyName",
                "getListenerMethodName", "create"}) {
            check(names.contains(n), "declares public " + n);
        }
        int creates = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("create") && java.lang.reflect.Modifier.isStatic(m.getModifiers())) creates++;
        }
        check(creates == 3, "three static create overloads, got " + creates);
    }

    static void testCtorAndGetters() throws Exception {
        EventHandler eh = new EventHandler(new StringBuilder(), "append", "getSource", "actionPerformed");
        check(eh.getTarget() instanceof StringBuilder, "target");
        check("append".equals(eh.getAction()), "action");
        check("getSource".equals(eh.getEventPropertyName()), "event property name");
        check("actionPerformed".equals(eh.getListenerMethodName()), "listener method name");

        Target t = new Target();
        EventHandler eh2 = new EventHandler(t, "fire", null, null);
        check("fire".equals(eh2.getAction()), "getAction");
        check(t == eh2.getTarget(), "getTarget");
        check(eh2.getEventPropertyName() == null, "eventPropertyName null default");
        check(eh2.getListenerMethodName() == null, "listenerMethodName null default");
    }

    static void testCtorValidation() throws Exception {
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new EventHandler(null, "a", null, null); }
        }, "ctor null target");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new EventHandler(new Object(), null, null, null); }
        }, "ctor null action");
        final Target t = new Target();
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { EventHandler.create(null, t, "fire"); }
        }, "create null listenerInterface");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { EventHandler.create(Lsn.class, null, "fire"); }
        }, "create null target");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { EventHandler.create(Lsn.class, t, null); }
        }, "create null action");
        // empty action string is accepted at creation time (fails only at dispatch)
        Object p = EventHandler.create(Lsn.class, t, "", null, "second");
        check(p != null, "empty action accepted at creation");
    }

    static void testProxyNature() throws Exception {
        Target t = new Target();
        java.awt.event.ActionListener al = (java.awt.event.ActionListener)
            EventHandler.create(java.awt.event.ActionListener.class, t, "fire");
        al.actionPerformed(null);
        check(t.fired, "create proxy invokes target action");

        Tgt tg = new Tgt();
        Lsn l = EventHandler.create(Lsn.class, tg, "fire");
        check(l instanceof Lsn, "proxy implements listener interface");
        check(Proxy.isProxyClass(l.getClass()), "proxy is a java.lang.reflect.Proxy");
        InvocationHandler h = Proxy.getInvocationHandler(l);
        check(h instanceof EventHandler, "invocation handler is the EventHandler");
        Lsn l2 = EventHandler.create(Lsn.class, new Tgt(), "fire");
        check(l != l2, "each create returns a distinct proxy");
        check(l.getClass() == l2.getClass(), "same listener interface yields same proxy class");
        check(tg == ((EventHandler) h).getTarget(), "handler carries target");
    }

    static void testObjectMethods() throws Exception {
        Object p = EventHandler.create(Lsn.class, new Tgt(), "fire");
        check(p.equals(p), "proxy equals itself");
        check(!p.equals(null), "proxy not equal to null");
        check(!p.equals(new Object()), "proxy not equal to unrelated object");
        check(p.hashCode() == System.identityHashCode(p), "hashCode is identity based");
        check(p.toString() != null, "toString handled");
        check(p.toString().contains("@"), "toString has standard form");
        Object q = EventHandler.create(Lsn.class, new Tgt(), "fire");
        check(!p.equals(q), "distinct proxies not equal");
    }

    static void testNullaryActionAllMethods() throws Exception {
        Tgt tg = new Tgt();
        Lsn l = EventHandler.create(Lsn.class, tg, "fire");
        l.second(new Evt("a", "t", null));
        l.first(new Evt("b", "t", null));
        check(tg.count == 2, "null listenerMethodName wires all interface methods");
        check(l.first(new Evt("c", "t", null)) == null, "void action yields null return");
    }

    static void testMethodFilter() throws Exception {
        Tgt tg = new Tgt();
        Lsn l = EventHandler.create(Lsn.class, tg, "fire", null, "second");
        l.second(new Evt("a", "t", null));
        check(tg.count == 1, "named listener method triggers action");
        Object r = l.first(new Evt("b", "t", null));
        check(r == null, "non-matching method is a no-op returning null");
        check(tg.count == 1, "non-matching method leaves target untouched");
        l.second(new Evt("c", "t", null));
        check(tg.count == 2, "matching method fires again");
    }

    static void testPropertyExtraction() throws Exception {
        Tgt tg = new Tgt();
        Lsn byProp = EventHandler.create(Lsn.class, tg, "record", "name", "second");
        byProp.second(new Evt("propVal", "t", null));
        check("propVal".equals(tg.got), "get<Prop> accessor used for extraction");

        Tgt tg2 = new Tgt();
        Lsn byMethod = EventHandler.create(Lsn.class, tg2, "record", "getName", "second");
        byMethod.second(new Evt("byMethod", "t", null));
        check("byMethod".equals(tg2.got), "method-name form of eventPropertyName works");

        Tgt tg3 = new Tgt();
        Lsn byTag = EventHandler.create(Lsn.class, tg3, "record", "tag", "second");
        byTag.second(new Evt("n", "theTag", null));
        check("theTag".equals(tg3.got), "second property of the same event");

        Tgt tg4 = new Tgt();
        Lsn whole = EventHandler.create(Lsn.class, tg4, "recordEvent", "", "second");
        whole.second(new Evt("n", "theTag", null));
        check("evt:theTag".equals(tg4.got), "empty eventPropertyName passes the event object itself");

        Tgt tg5 = new Tgt();
        Lsn dotted = EventHandler.create(Lsn.class, tg5, "record", "source.text", "second");
        Src src = new Src();
        src.text = "deep";
        dotted.second(new Evt("n", "t", src));
        check("deep".equals(tg5.got), "dot-path applied left to right");

        Tgt tg6 = new Tgt();
        Lsn isForm = EventHandler.create(Lsn.class, tg6, "takeBool", "source.enabled", "second");
        isForm.second(new Evt("n", "t", new Src()));
        check("b:true".equals(tg6.got), "is<Prop> accessor used when get<Prop> absent");

        Tgt tg7 = new Tgt();
        Lsn boolSet = EventHandler.create(Lsn.class, tg7, "enabled", "source.enabled", "second");
        boolSet.second(new Evt("n", "t", new Src()));
        check("enabled:true".equals(tg7.got), "extracted Boolean drives boolean setter");
    }

    static void testActionForms() throws Exception {
        Tgt tg = new Tgt();
        EventHandler.create(Lsn.class, tg, "label", "name", "second").second(new Evt("X", "t", null));
        check("lbl:X".equals(tg.got), "action matching a plain method name");

        Tgt tg2 = new Tgt();
        EventHandler.create(Lsn.class, tg2, "setLabel", "name", "second").second(new Evt("Y", "t", null));
        check("L:Y".equals(tg2.got), "action naming the setter method directly");

        SetterOnly so = new SetterOnly();
        EventHandler.create(Lsn.class, so, "label", "name", "second").second(new Evt("Z", "t", null));
        check("L:Z".equals(so.got), "property-name action falls back to set<Capitalized> with extracted arg");

        Tgt tg3 = new Tgt();
        EventHandler.create(Lsn.class, tg3, "sub.text", "name", "second").second(new Evt("W", "t", null));
        check("W".equals(tg3.sub.text), "qualified action navigates target then applies final property");

        Tgt tg4 = new Tgt();
        Object r = EventHandler.create(Lsn.class, tg4, "giveBack", null, "first").first(new Evt("n", "t", null));
        check("back".equals(r), "action return value passed back through the proxy");
    }

    static void testQualifiedActionMutatesHandler() throws Exception {
        Tgt tg = new Tgt();
        EventHandler h = new EventHandler(tg, "sub.text", "name", "second");
        Evt e = new Evt("M", "t", null);
        h.invoke(new Object(), Lsn.class.getMethod("second", Evt.class), new Object[]{e});
        check("M".equals(tg.sub.text), "direct invoke() dispatches like the proxy");
        check("text".equals(h.getAction()), "after qualified dispatch action shrinks to final name");
        check(h.getTarget() == tg.sub, "after qualified dispatch target becomes the intermediate object");
    }

    static void testPropertyChangeListenerIntegration() throws Exception {
        Tgt tg = new Tgt();
        PropertyChangeListener pcl = EventHandler.create(
                PropertyChangeListener.class, tg, "record", "propertyName", "propertyChange");
        pcl.propertyChange(new PropertyChangeEvent("src", "theProp", "old", "new"));
        check("theProp".equals(tg.got), "PropertyChangeEvent.propertyName extracted to action");
        check(tg.count == 1, "listener fired once");

        Tgt tg2 = new Tgt();
        PropertyChangeListener all = EventHandler.create(PropertyChangeListener.class, tg2, "fire", null);
        all.propertyChange(new PropertyChangeEvent("src", "p1", null, null));
        all.propertyChange(new PropertyChangeEvent("src", "p2", null, null));
        check(tg2.count == 2, "null property name: nullary action for every event");
        check("none".equals(tg2.got), "nullary action receives no argument");
    }

    static void testDispatchExceptions() throws Exception {
        final Tgt tg = new Tgt();
        checkThrows(RuntimeException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                EventHandler.create(Lsn.class, tg, "record", "nothere", "second")
                        .second(new Evt("n", "t", null));
            }
        }, "unknown event property name");
        checkThrows(RuntimeException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                EventHandler.create(Lsn.class, tg, "record", "source.nope.name", "second")
                        .second(new Evt("n", "t", new Src()));
            }
        }, "missing intermediate property in dot-path");
        checkThrows(RuntimeException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                EventHandler.create(Lsn.class, tg, "nosuchaction", null, "second")
                        .second(new Evt("n", "t", null));
            }
        }, "unknown action name");
        checkThrows(RuntimeException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                EventHandler.create(Lsn.class, tg, "", null, "second")
                        .second(new Evt("n", "t", null));
            }
        }, "empty action name has no method");
        checkThrows(RuntimeException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                EventHandler.create(Lsn.class, tg, "record", "name", "second").second(null);
            }
        }, "null event with property extraction");
        final Boom boom = new Boom();
        try {
            EventHandler.create(Lsn.class, boom, "record", "name", "second")
                    .second(new Evt("n", "t", null));
            check(false, "exception from action must propagate");
        } catch (RuntimeException re) {
            check(re == boom.token, "RuntimeException from target action propagates as-is");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCtorAndGetters();
        testCtorValidation();
        testProxyNature();
        testObjectMethods();
        testNullaryActionAllMethods();
        testMethodFilter();
        testPropertyExtraction();
        testActionForms();
        testQualifiedActionMutatesHandler();
        testPropertyChangeListenerIntegration();
        testDispatchExceptions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
