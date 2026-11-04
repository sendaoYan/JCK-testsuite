/*
 * @test
 * @summary java.beans.Introspector conformance: full contract coverage.
 *          decapitalize edge cases (acronyms, empty, single char, null),
 *          getBeanInfo for simple/boolean/array/indexed properties, bound
 *          (addPropertyChangeListener) and constrained (setter throwing
 *          PropertyVetoException) auto-detection, event set discovery,
 *          explicit BeanInfo search-path flags, stopClass validation
 *          (not-a-superclass -> IntrospectionException), and cache flush
 *          (flushCaches / flushFromCaches null -> NPE).
 * @run main IntrospectorTest
 */

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.EventObject;

public class IntrospectorTest {
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

    public interface FooListener extends java.util.EventListener { void fooHappened(EventObject e); }

    public static class Bean {
        private String name;
        private boolean flag;
        private int[] vals;
        private String constrained;
        public String getName() { return name; }
        public void setName(String v) { name = v; }
        public boolean isFlag() { return flag; }
        public void setFlag(boolean v) { flag = v; }
        public int[] getVals() { return vals; }
        public void setVals(int[] v) { vals = v; }
        public int getVal(int i) { return vals[i]; }
        public void setVal(int i, int v) { vals[i] = v; }
        public String getConstrained() { return constrained; }
        public void setConstrained(String v) throws PropertyVetoException { constrained = v; }
        public void addFooListener(FooListener l) {}
        public void removeFooListener(FooListener l) {}
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
        public void addVetoableChangeListener(VetoableChangeListener l) {}
        public void removeVetoableChangeListener(VetoableChangeListener l) {}
    }

    static PropertyDescriptor find(PropertyDescriptor[] pds, String name) {
        for (PropertyDescriptor pd : pds) if (name.equals(pd.getName())) return pd;
        return null;
    }
    static boolean hasEvent(EventSetDescriptor[] esds, String name) {
        for (EventSetDescriptor esd : esds) if (name.equals(esd.getName())) return true;
        return false;
    }

    static void testDecapitalize() {
        check("foo".equals(Introspector.decapitalize("Foo")), "decapitalize Foo -> foo");
        check("URL".equals(Introspector.decapitalize("URL")), "decapitalize acronym URL unchanged");
        check("URLFoo".equals(Introspector.decapitalize("URLFoo")), "decapitalize URLFoo unchanged");
        check("foo".equals(Introspector.decapitalize("foo")), "decapitalize already-lower unchanged");
        check("".equals(Introspector.decapitalize("")), "decapitalize empty unchanged");
        check("a".equals(Introspector.decapitalize("A")), "decapitalize single char A -> a");
        check("URL2".equals(Introspector.decapitalize("URL2")), "decapitalize URL2 unchanged");
        check(Introspector.decapitalize(null) == null, "decapitalize null -> null");
    }

    static void testGetBeanInfo() throws Exception {
        BeanInfo bi = Introspector.getBeanInfo(Bean.class, Object.class);
        check(bi.getBeanDescriptor() != null && Bean.class == bi.getBeanDescriptor().getBeanClass(),
                "beanDescriptor beanClass");

        PropertyDescriptor[] pds = bi.getPropertyDescriptors();
        PropertyDescriptor name = find(pds, "name");
        check(name != null && String.class == name.getPropertyType(), "name type String");
        check("getName".equals(name.getReadMethod().getName()), "name read method");
        check("setName".equals(name.getWriteMethod().getName()), "name write method");
        check(name.isBound(), "bound auto-detected via addPropertyChangeListener");
        check(!name.isConstrained(), "name not constrained");

        PropertyDescriptor flag = find(pds, "flag");
        check(flag != null && boolean.class == flag.getPropertyType(), "flag type boolean");
        check("isFlag".equals(flag.getReadMethod().getName()), "is* boolean read method");

        PropertyDescriptor vals = find(pds, "vals");
        check(vals != null && int[].class == vals.getPropertyType(), "array property type int[]");
        check("getVals".equals(vals.getReadMethod().getName()), "array read method");

        PropertyDescriptor val = find(pds, "val");
        check(val instanceof IndexedPropertyDescriptor, "val is indexed property");
        IndexedPropertyDescriptor ival = (IndexedPropertyDescriptor) val;
        check(ival.getIndexedPropertyType() == int.class, "indexed property type int");
        check("getVal".equals(ival.getIndexedReadMethod().getName()), "indexed read method");
        check("setVal".equals(ival.getIndexedWriteMethod().getName()), "indexed write method");
        check(ival.getPropertyType() == null, "array type null when no non-indexed accessor");

        PropertyDescriptor constrained = find(pds, "constrained");
        check(constrained != null && constrained.isConstrained(), "constrained via setter throwing PropertyVetoException");
        check(constrained.isBound(), "constrained property also bound");

        EventSetDescriptor[] esds = bi.getEventSetDescriptors();
        check(hasEvent(esds, "foo"), "foo event set discovered");
        check(hasEvent(esds, "propertyChange"), "propertyChange event set discovered");
        check(hasEvent(esds, "vetoableChange"), "vetoableChange event set discovered");

        check(Introspector.getBeanInfo(Bean.class) != null, "default stopClass getBeanInfo");
    }

    static void testFlagsAndStopClass() throws Exception {
        check(Introspector.getBeanInfo(Bean.class, Introspector.USE_ALL_BEANINFO) != null, "USE_ALL_BEANINFO");
        check(Introspector.getBeanInfo(Bean.class, Introspector.IGNORE_IMMEDIATE_BEANINFO) != null, "IGNORE_IMMEDIATE_BEANINFO");
        check(Introspector.getBeanInfo(Bean.class, Introspector.IGNORE_ALL_BEANINFO) != null, "IGNORE_ALL_BEANINFO");

        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Introspector.getBeanInfo(Bean.class, String.class);
                }}, "stopClass not a superclass -> IntrospectionException");
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Introspector.getBeanInfo(Bean.class, Bean.class);
                }}, "stopClass equal to beanClass -> IntrospectionException");
    }

    static void testCachesAndSearchPath() {
        String[] original = Introspector.getBeanInfoSearchPath();
        check(original != null && original.length >= 1, "default search path non-empty");
        try {
            Introspector.setBeanInfoSearchPath(new String[] { "test.pkg" });
            String[] sp = Introspector.getBeanInfoSearchPath();
            check(sp != null && sp.length == 1 && "test.pkg".equals(sp[0]), "setBeanInfoSearchPath reflected");
        } finally {
            Introspector.setBeanInfoSearchPath(original);
        }

        Introspector.flushCaches();
        Introspector.flushFromCaches(Bean.class);
        check(true, "flushCaches/flushFromCaches no throw");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() {
                    Introspector.flushFromCaches(null);
                }}, "flushFromCaches(null) -> NPE");
    }


    static void testStructure() {
        Class<?> c = Introspector.class;
        check(!c.isInterface(), "concrete class");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testDecapitalize();
        testGetBeanInfo();
        testFlagsAndStopClass();
        testCachesAndSearchPath();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
