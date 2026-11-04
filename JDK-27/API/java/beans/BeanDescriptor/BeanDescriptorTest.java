/*
 * @test
 * @summary java.beans.BeanDescriptor conformance: white-box structure + black-box behavior.
 *          Both constructors, null beanClass NPE, name derivation (unqualified class name,
 *          nested and array classes recorded), displayName default, inherited
 *          FeatureDescriptor flags/values, identity equals, not serializable,
 *          SimpleBeanInfo integration.
 * @run main BeanDescriptorTest
 */

import java.beans.BeanDescriptor;
import java.beans.Customizer;
import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.SimpleBeanInfo;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;

public class BeanDescriptorTest {
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

    public static class MyBean {}
    public static class MyCustomizer implements Customizer {
        public void setObject(Object bean) {}
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
    }
    public static class MyBeanInfo extends SimpleBeanInfo {
        private final BeanDescriptor bd = new BeanDescriptor(MyBean.class, MyCustomizer.class);
        @Override
        public BeanDescriptor getBeanDescriptor() { return bd; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.BeanDescriptor");
        check(java.beans.FeatureDescriptor.class.isAssignableFrom(c), "extends FeatureDescriptor");
        check(c.getConstructors().length == 2, "exactly two public constructors");
        boolean oneArg = false, twoArg = false;
        for (java.lang.reflect.Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 1 && p[0] == Class.class) oneArg = true;
            if (p.length == 2 && p[0] == Class.class && p[1] == Class.class) twoArg = true;
        }
        check(oneArg, "constructor (Class) present");
        check(twoArg, "constructor (Class, Class) present");
        try {
            c.getMethod("getBeanClass");
            c.getMethod("getCustomizerClass");
            passed++;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("missing getBeanClass/getCustomizerClass");
        }
        // no equals override (identity semantics)
        boolean hasEquals = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("equals")) hasEquals = true;
        }
        check(!hasEquals, "BeanDescriptor does not override equals");
    }

    static void testBlackBox() throws Exception {
        BeanDescriptor bd = new BeanDescriptor(String.class);
        check(String.class == bd.getBeanClass(), "beanClass");
        check(bd.getCustomizerClass() == null, "customizer null default");
        BeanDescriptor bd2 = new BeanDescriptor(String.class, java.beans.PropertyEditorSupport.class);
        check(java.beans.PropertyEditorSupport.class == bd2.getCustomizerClass(), "customizerClass");
    }

    static void testNameDerivation() {
        check("String".equals(new BeanDescriptor(String.class).getName()),
                "name defaults to unqualified class name");
        check("Date".equals(new BeanDescriptor(java.util.Date.class).getName()),
                "package stripped from name");
        // nested classes keep the binary $ form
        check("BeanDescriptorTest$MyBean".equals(new BeanDescriptor(MyBean.class).getName()),
                "nested class name keeps $ form");
        check("Map$Entry".equals(new BeanDescriptor(java.util.Map.Entry.class).getName()),
                "deeply nested class name");
        // array classes: the JDK strips everything up to the last '.', leaving "[...;" tail
        check("String;".equals(new BeanDescriptor(String[].class).getName()),
                "array class name recorded actual form");
    }

    static void testDisplayNameDefault() {
        BeanDescriptor bd = new BeanDescriptor(String.class);
        check("String".equals(bd.getDisplayName()), "displayName defaults to name");
        bd.setDisplayName("Friendly");
        check("Friendly".equals(bd.getDisplayName()), "setDisplayName overrides");
        check("Friendly".equals(bd.getShortDescription()),
                "shortDescription falls back to the explicit displayName");
        bd.setName("renamed");
        check("renamed".equals(bd.getName()), "setName overrides derived name");
        check("Friendly".equals(bd.getShortDescription()),
                "shortDescription still falls back to explicit displayName after rename");
    }

    static void testCustomizerForms() {
        BeanDescriptor one = new BeanDescriptor(MyBean.class);
        check(one.getCustomizerClass() == null, "1-arg ctor customizer null");
        BeanDescriptor two = new BeanDescriptor(MyBean.class, MyCustomizer.class);
        check(MyBean.class == two.getBeanClass(), "2-arg beanClass identity");
        check(MyCustomizer.class == two.getCustomizerClass(), "2-arg customizerClass identity");
        BeanDescriptor explicitNull = new BeanDescriptor(MyBean.class, null);
        check(explicitNull.getCustomizerClass() == null, "explicit null customizer allowed");
    }

    static void testNullBeanClass() {
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { new BeanDescriptor(null); }
        }, "BeanDescriptor(null) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Exception { new BeanDescriptor(null, MyCustomizer.class); }
        }, "BeanDescriptor(null, Class) -> NPE");
    }

    static void testInheritedFeatureBehavior() {
        BeanDescriptor bd = new BeanDescriptor(MyBean.class);
        check(!bd.isExpert() && !bd.isHidden() && !bd.isPreferred(), "flags default false");
        bd.setExpert(true); check(bd.isExpert(), "expert toggle");
        bd.setHidden(true); check(bd.isHidden(), "hidden toggle");
        bd.setPreferred(true); check(bd.isPreferred(), "preferred toggle");
        bd.setValue("k", "v");
        check("v".equals(bd.getValue("k")), "setValue/getValue inherited");
        check(bd.getValue("nope") == null, "unknown value null");
    }

    static void testToString() {
        BeanDescriptor bd = new BeanDescriptor(MyBean.class);
        String s = bd.toString();
        check(s != null, "toString non-null");
        check(s.contains("MyBean"), "toString mentions bean class");
    }

    static void testNotSerializable() throws Exception {
        check(!java.io.Serializable.class.isAssignableFrom(BeanDescriptor.class),
                "BeanDescriptor is not Serializable");
        final ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream());
        checkThrows(NotSerializableException.class, new ThrowingRunnable() {
            public void run() throws Exception { oos.writeObject(new BeanDescriptor(MyBean.class)); }
        }, "writeObject(BeanDescriptor) -> NotSerializableException");
    }

    static void testEqualsIdentity() {
        BeanDescriptor a = new BeanDescriptor(MyBean.class);
        BeanDescriptor b = new BeanDescriptor(MyBean.class);
        check(a.equals(a), "equals self");
        check(!a.equals(b), "identity equals semantics");
    }

    static void testSimpleBeanInfoIntegration() {
        MyBeanInfo info = new MyBeanInfo();
        BeanDescriptor bd = info.getBeanDescriptor();
        check(bd != null, "SimpleBeanInfo subclass provides BeanDescriptor");
        check(MyBean.class == bd.getBeanClass(), "BeanInfo-provided beanClass");
        check(MyCustomizer.class == bd.getCustomizerClass(), "BeanInfo-provided customizerClass");
        check(bd == info.getBeanDescriptor(), "same instance returned each call");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNameDerivation();
        testDisplayNameDefault();
        testCustomizerForms();
        testNullBeanClass();
        testInheritedFeatureBehavior();
        testToString();
        testNotSerializable();
        testEqualsIdentity();
        testSimpleBeanInfoIntegration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
