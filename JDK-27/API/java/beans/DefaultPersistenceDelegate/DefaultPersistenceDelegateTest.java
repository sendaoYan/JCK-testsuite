/*
 * @test
 * @summary java.beans.DefaultPersistenceDelegate conformance: white-box structure + black-box behavior.
 *          Both constructors (including null and empty name arrays), fresh-
 *          instance property diffing (only changed properties emitted, setters
 *          not invoked for unchanged values), constructor-property-name path
 *          (ordered getter expressions, works without a nullary constructor
 *          or setters), error matrix (unknown property name, null property
 *          name, missing nullary constructor) routed to the ExceptionListener,
 *          transient PropertyDescriptor attribute honored, public field
 *          persistence, mutatesTo semantics with and without declared equals.
 * @run main DefaultPersistenceDelegateTest
 */

import java.beans.DefaultPersistenceDelegate;
import java.beans.ExceptionListener;
import java.beans.IntrospectionException;
import java.beans.PersistenceDelegate;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.Statement;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DefaultPersistenceDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class CountBean {
        public static int setXCount;
        private int x;
        private String s;
        public CountBean() {}
        public int getX() { return x; }
        public void setX(int v) { setXCount++; x = v; }
        public String getS() { return s; }
        public void setS(String v) { s = v; }
    }
    public static class CtorBean {
        private int a;
        private String b;
        public CtorBean() {}
        public CtorBean(int a, String b) { this.a = a; this.b = b; }
        public int getA() { return a; }
        public String getB() { return b; }
    }
    public static class CtorOnly {
        private final int a;
        private final String b;
        public CtorOnly(int a, String b) { this.a = a; this.b = b; }
        public int getA() { return a; }
        public String getB() { return b; }
    }
    public static class NoDefault {
        private int v;
        public NoDefault(int v) { this.v = v; }
        public int getV() { return v; }
        public void setV(int v) { this.v = v; }
    }
    public static class FieldBean {
        public int count = 0;
        public FieldBean() {}
    }
    public static class TransientBean {
        private String secret = "p";
        private String kept = "k";
        public TransientBean() {}
        public String getSecret() { return secret; }
        public void setSecret(String v) { secret = v; }
        public String getKept() { return kept; }
        public void setKept(String v) { kept = v; }
    }
    public static class TransientBeanBeanInfo extends SimpleBeanInfo {
        public PropertyDescriptor[] getPropertyDescriptors() {
            try {
                PropertyDescriptor sec = new PropertyDescriptor("secret", TransientBean.class);
                sec.setValue("transient", Boolean.TRUE);
                PropertyDescriptor kep = new PropertyDescriptor("kept", TransientBean.class);
                return new PropertyDescriptor[] { sec, kep };
            } catch (IntrospectionException e) {
                return null;
            }
        }
    }

    static class CapturingListener implements ExceptionListener {
        final List<Exception> exs = new ArrayList<Exception>();
        public void exceptionThrown(Exception e) { exs.add(e); }
    }

    static String encode(Object o) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.writeObject(o);
        enc.close();
        return baos.toString("UTF-8");
    }
    static String encodeWith(CapturingListener l, Object o) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(l);
        enc.writeObject(o);
        enc.close();
        return baos.toString("UTF-8");
    }
    static Object decode(String xml) {
        java.beans.XMLDecoder dec = new java.beans.XMLDecoder(
                new java.io.ByteArrayInputStream(xml.getBytes()));
        try { return dec.readObject(); } finally { dec.close(); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.DefaultPersistenceDelegate");
        check(PersistenceDelegate.class.isAssignableFrom(c), "extends PersistenceDelegate");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(java.lang.reflect.Modifier.isPublic(
                c.getDeclaredConstructor().getModifiers()), "nullary constructor is public");
        check(java.lang.reflect.Modifier.isPublic(
                c.getDeclaredConstructor(String[].class).getModifiers()), "String[] constructor is public");
    }

    static void testBlackBox() throws Exception {
        DefaultPersistenceDelegate dpd = new DefaultPersistenceDelegate();
        check(dpd instanceof PersistenceDelegate, "instanceof PersistenceDelegate");
        DefaultPersistenceDelegate dpd2 = new DefaultPersistenceDelegate(new String[]{"x", "y"});
        check(dpd2 instanceof PersistenceDelegate, "array ctor instanceof");
    }

    static void testConstructorNameArrays() throws Exception {
        check(new DefaultPersistenceDelegate((String[]) null) != null,
                "null name array accepted (treated as no-arg delegate)");
        String xml = encodeWith(new CapturingListener(), newDefaultWithNames(null));
        check(!xml.contains("void property"), "null name array behaves like the nullary delegate");
        check(new DefaultPersistenceDelegate(new String[0]) != null,
                "empty name array accepted");
    }
    static DefaultPersistenceDelegate newDefaultWithNames(String[] names) {
        return new DefaultPersistenceDelegate(names);
    }

    static void testDiffingEmission() throws Exception {
        CapturingListener l = new CapturingListener();
        String allDefault = encodeWith(l, new CountBean());
        check(l.exs.isEmpty(), "default-valued bean encodes without exceptions");
        check(!allDefault.contains("void property"), "unchanged properties are not emitted");

        CountBean changed = new CountBean();
        changed.setX(7);
        changed.setS("z");
        String changedXml = encodeWith(l, changed);
        check(changedXml.contains("void property=\"x\""), "changed int property emitted");
        check(changedXml.contains("void property=\"s\""), "changed String property emitted");

        CountBean onlyX = new CountBean();
        onlyX.setX(5);
        String onlyXXml = encodeWith(l, onlyX);
        check(onlyXXml.contains("void property=\"x\""), "changed property emitted (partial change)");
        check(!onlyXXml.contains("void property=\"s\""), "unchanged property suppressed (partial change)");
    }

    static void testSetterNotInvokedWhenEqual() throws Exception {
        // The encoder also executes setter statements in its cloned environment,
        // so the counter is reset between encode and decode to observe decode only.
        String defaults = encode(new CountBean());
        CountBean.setXCount = 0;
        decode(defaults);
        check(CountBean.setXCount == 0, "decoder setter never invoked for default-valued property");

        CountBean changed = new CountBean();
        changed.setX(7);
        changed.setS("z");
        String changedXml = encode(changed);
        check(CountBean.setXCount >= 1, "encoder clone environment executes the changed setter");
        CountBean.setXCount = 0;
        CountBean back = (CountBean) decode(changedXml);
        check(CountBean.setXCount == 1, "decoder setter invoked exactly once for changed property");
        check(back.getX() == 7 && "z".equals(back.getS()), "decoded changed bean fields");
    }

    static void testConstructorPropertyNames() throws Exception {
        CapturingListener l = new CapturingListener();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(l);
        enc.setPersistenceDelegate(CtorBean.class,
                new DefaultPersistenceDelegate(new String[] { "a", "b" }));
        enc.writeObject(new CtorBean(5, "five"));
        enc.close();
        check(l.exs.isEmpty(), "constructor-property path encodes without exceptions");
        String xml = baos.toString("UTF-8");
        check(xml.contains("<int>5</int>"), "first constructor argument from getter a");
        check(xml.contains("<string>five</string>"), "second constructor argument from getter b");
        check(!xml.contains("void property"), "constructor path emits no property statements");
        CtorBean back = (CtorBean) decode(xml);
        check(back.getA() == 5, "constructor-property bean a round-trip");
        check("five".equals(back.getB()), "constructor-property bean b round-trip");
    }

    static void testConstructorOnlyBean() throws Exception {
        CapturingListener l = new CapturingListener();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(l);
        enc.setPersistenceDelegate(CtorOnly.class,
                new DefaultPersistenceDelegate(new String[] { "a", "b" }));
        enc.writeObject(new CtorOnly(6, "six"));
        enc.close();
        check(l.exs.isEmpty(), "bean without nullary constructor and setters encodes cleanly");
        CtorOnly back = (CtorOnly) decode(baos.toString("UTF-8"));
        check(back.getA() == 6, "constructor-only bean a round-trip");
        check("six".equals(back.getB()), "constructor-only bean b round-trip");
    }

    static void testUnknownPropertyName() throws Exception {
        CapturingListener l = new CapturingListener();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(l);
        enc.setPersistenceDelegate(CtorBean.class,
                new DefaultPersistenceDelegate(new String[] { "nope" }));
        enc.writeObject(new CtorBean(1, "one"));
        enc.close();
        check(l.exs.size() >= 1, "unknown property name reported to listener");
        Exception first = l.exs.get(0);
        check(first instanceof IllegalStateException, "unknown property name yields IllegalStateException");
        check(first.getMessage() != null && first.getMessage().contains("nope"),
                "exception message names the missing property");
        boolean discarding = false;
        for (Exception e : l.exs) {
            if (e.getMessage() != null && e.getMessage().contains("discarding")) discarding = true;
        }
        check(discarding, "object discarded with a discarding wrapper exception");
        check(!baos.toString("UTF-8").contains("<object"), "failed object omitted from output");
    }

    static void testNullPropertyName() throws Exception {
        CapturingListener l = new CapturingListener();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(l);
        enc.setPersistenceDelegate(CountBean.class,
                new DefaultPersistenceDelegate(new String[] { null }));
        enc.writeObject(new CountBean());
        enc.close();
        check(l.exs.size() >= 1, "null property name reported to listener");
        check(l.exs.get(0) instanceof IllegalArgumentException,
                "null property name yields IllegalArgumentException");
    }

    static void testMissingNullaryConstructor() throws Exception {
        CapturingListener l = new CapturingListener();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(l);
        enc.writeObject(new NoDefault(3));
        enc.close();
        check(l.exs.size() >= 1, "missing nullary constructor reported to listener");
        Exception first = l.exs.get(0);
        check(first instanceof java.lang.InstantiationException,
                "missing nullary constructor yields InstantiationException");
        check(first.getCause() instanceof NoSuchMethodException,
                "InstantiationException caused by NoSuchMethodException");
        check(!baos.toString("UTF-8").contains("<object"), "failed object omitted from output");
    }

    static void testTransientPropertySkipped() throws Exception {
        CapturingListener l = new CapturingListener();
        TransientBean tb = new TransientBean();
        tb.setSecret("sekrit");
        tb.setKept("kept2");
        String xml = encodeWith(l, tb);
        check(l.exs.isEmpty(), "transient-marked bean encodes without exceptions");
        check(!xml.contains("sekrit"), "transient property value not emitted");
        check(xml.contains("kept2"), "non-transient property value emitted");
        TransientBean back = (TransientBean) decode(xml);
        check("p".equals(back.getSecret()), "transient property keeps its default after round-trip");
        check("kept2".equals(back.getKept()), "non-transient property round-trips");
    }

    static void testPublicFieldPersistence() throws Exception {
        CapturingListener l = new CapturingListener();
        FieldBean fb = new FieldBean();
        fb.count = 9;
        String xml = encodeWith(l, fb);
        check(l.exs.isEmpty(), "public field bean encodes without exceptions");
        check(xml.contains("method=\"getField\""), "public field emitted via getField");
        FieldBean back = (FieldBean) decode(xml);
        check(back.count == 9, "public field round-trips");
    }

    static void testMutatesToSemantics() {
        class PubNoNames extends DefaultPersistenceDelegate {
            boolean m(Object a, Object b) { return mutatesTo(a, b); }
        }
        PubNoNames d0 = new PubNoNames();
        check(d0.m("a", "b"), "no-name delegate mutatesTo true for any same-class pair");
        check(!d0.m("a", Integer.valueOf(1)), "no-name delegate mutatesTo false across classes");

        class PubNames extends DefaultPersistenceDelegate {
            PubNames(String[] names) { super(names); }
            boolean m(Object a, Object b) { return mutatesTo(a, b); }
        }
        PubNames d1 = new PubNames(new String[] { "x" });
        check(d1.m("a", "a"), "named delegate mutatesTo true for equal String instances");
        check(!d1.m("a", "b"), "named delegate mutatesTo false for unequal String instances (equals declared)");
        PubNames d2 = new PubNames(new String[] { "x", "y" });
        check(d2.m(new CtorBean(1, "a"), new CtorBean(2, "b")),
                "named delegate mutatesTo true when equals not declared (class equality)");
        boolean npe = false;
        try { d1.m(null, "a"); } catch (NullPointerException e) { npe = true; }
        check(npe, "named delegate mutatesTo with null old instance throws NullPointerException");
    }

    static void testDefaultDelegateRegistration() {
        java.beans.Encoder enc = new java.beans.Encoder();
        check(enc.getPersistenceDelegate(CtorBean.class) instanceof DefaultPersistenceDelegate,
                "unknown types map to a DefaultPersistenceDelegate");
        DefaultPersistenceDelegate custom = new DefaultPersistenceDelegate(new String[] { "a" });
        enc.setPersistenceDelegate(CtorBean.class, custom);
        check(enc.getPersistenceDelegate(CtorBean.class) == custom,
                "registered delegate returned by getPersistenceDelegate");
        enc.setPersistenceDelegate(CtorBean.class, null);
        check(enc.getPersistenceDelegate(CtorBean.class) instanceof DefaultPersistenceDelegate,
                "setPersistenceDelegate(null) falls back to the default delegate");
    }

    static void testNamesArrayCopied() throws Exception {
        String[] names = new String[] { "a", "b" };
        DefaultPersistenceDelegate dpd = new DefaultPersistenceDelegate(names);
        names[0] = "bogus";
        CapturingListener l = new CapturingListener();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(baos);
        enc.setExceptionListener(l);
        enc.setPersistenceDelegate(CtorBean.class, dpd);
        enc.writeObject(new CtorBean(5, "five"));
        enc.close();
        check(l.exs.isEmpty(), "delegate unaffected by caller mutating the name array");
        CtorBean back = (CtorBean) decode(baos.toString("UTF-8"));
        check(back.getA() == 5, "copied names still resolve getter a");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorNameArrays();
        testDiffingEmission();
        testSetterNotInvokedWhenEqual();
        testConstructorPropertyNames();
        testConstructorOnlyBean();
        testUnknownPropertyName();
        testNullPropertyName();
        testMissingNullaryConstructor();
        testTransientPropertySkipped();
        testPublicFieldPersistence();
        testMutatesToSemantics();
        testDefaultDelegateRegistration();
        testNamesArrayCopied();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
