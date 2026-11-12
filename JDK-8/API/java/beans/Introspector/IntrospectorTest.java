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
 *          Deepening: full decapitalize matrix (two-leading-cap rule on
 *          "ABc"/"AB"/"aB"/"URLs", non-ASCII upper "U-umlaut" pairs,
 *          titlecase chars, digits, leading space), bean hierarchy
 *          (stopClass hides inherited + Object properties, 4-arg overload),
 *          BeanInfo cache identity (same instance until flushCaches),
 *          non-defensive descriptor arrays, getBeanInfo(null) -> NPE,
 *          interface/primitive/array/package-private class introspection,
 *          setBeanInfoSearchPath(null) -> empty path (probe, restored in
 *          finally), flag constant exact values, and per-flag property-set
 *          equivalence for beans without explicit BeanInfo.
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
        // exact flag constant values (frozen API surface)
        check(Introspector.USE_ALL_BEANINFO == 1, "USE_ALL_BEANINFO == 1");
        check(Introspector.IGNORE_IMMEDIATE_BEANINFO == 2, "IGNORE_IMMEDIATE_BEANINFO == 2");
        check(Introspector.IGNORE_ALL_BEANINFO == 3, "IGNORE_ALL_BEANINFO == 3");
    }

    // ---------- deepening: full decapitalize matrix ----------

    static void testDecapitalizeMatrix() {
        // rule: length>1 AND first two chars both uppercase -> unchanged
        check("ABc".equals(Introspector.decapitalize("ABc")), "two leading caps ABc unchanged");
        check("AB".equals(Introspector.decapitalize("AB")), "two leading caps AB unchanged");
        check("URLs".equals(Introspector.decapitalize("URLs")), "acronym+s URLs unchanged");
        check("AB2".equals(Introspector.decapitalize("AB2")), "AB2 unchanged (second char uppercase)");
        check("aB".equals(Introspector.decapitalize("aB")), "aB unchanged (first already lowercase)");
        check("A".equals(Introspector.decapitalize("A")) == false, "A is lowered");
        check("a".equals(Introspector.decapitalize("a")), "single lowercase unchanged");
        // non-ASCII uppercase participates in the rule
        check("A\u00DC".equals(Introspector.decapitalize("A\u00DC")), "A+U-umlaut unchanged");
        check("\u00DCA".equals(Introspector.decapitalize("\u00DCA")), "U-umlaut+A unchanged");
        check("\u00DCU".equals(Introspector.decapitalize("\u00DCU")), "U-umlaut pair unchanged");
        check("\u00FCnicode".equals(Introspector.decapitalize("\u00DCnicode")), "U-umlaut head lowered");
        check("\u00FC\u00FC".equals(Introspector.decapitalize("\u00DC\u00FC")), "U-umlaut+u-umlaut lowered head");
        // titlecase chars (Lt) are NOT uppercase: rule not triggered, first char lowered
        check("\u01C6ungla".equals(Introspector.decapitalize("\u01C5ungla")), "titlecase D-z lowered");
        check("\u01C6\u01C5".equals(Introspector.decapitalize("\u01C5\u01C5")), "titlecase pair head lowered");
        // non-letters
        check(" x".equals(Introspector.decapitalize(" x")), "leading space unchanged");
        check("xy".equals(Introspector.decapitalize("Xy")), "Xy -> xy");
        // determinism
        String once = Introspector.decapitalize("\u00DCnicode");
        check(once.equals(Introspector.decapitalize("\u00DCnicode")), "decapitalize deterministic");
    }

    // ---------- deepening: bean hierarchy / stopClass semantics ----------

    public static class HBase {
        private int a;
        public int getA() { return a; }
        public void setA(int a) { this.a = a; }
    }
    public static class HSub extends HBase {
        private int b;
        public int getB() { return b; }
        public void setB(int b) { this.b = b; }
    }

    static void testHierarchyAndStopClasses() throws Exception {
        BeanInfo full = Introspector.getBeanInfo(HSub.class);
        check(find(full.getPropertyDescriptors(), "a") != null, "hierarchy: inherited a present");
        check(find(full.getPropertyDescriptors(), "b") != null, "hierarchy: declared b present");
        check(find(full.getPropertyDescriptors(), "class") != null, "hierarchy: Object getClass yields class");
        check(full.getPropertyDescriptors().length == 3, "hierarchy: exactly a,b,class");

        BeanInfo toBase = Introspector.getBeanInfo(HSub.class, HBase.class);
        check(find(toBase.getPropertyDescriptors(), "a") == null, "stopClass Base hides a");
        check(find(toBase.getPropertyDescriptors(), "b") != null, "stopClass Base keeps b");
        check(find(toBase.getPropertyDescriptors(), "class") == null, "stopClass Base hides class too");
        check(toBase.getPropertyDescriptors().length == 1, "stopClass Base leaves exactly b");
        check(toBase.getBeanDescriptor() != null, "beanDescriptor present with stopClass");
        check(toBase.getMethodDescriptors().length == 2, "only getB/setB method descriptors below stop");
        check(toBase.getEventSetDescriptors() != null && toBase.getEventSetDescriptors().length == 0,
                "empty (not null) event set array");

        BeanInfo toObject = Introspector.getBeanInfo(HSub.class, Object.class);
        check(find(toObject.getPropertyDescriptors(), "a") != null, "stopClass Object keeps a");
        check(find(toObject.getPropertyDescriptors(), "b") != null, "stopClass Object keeps b");
        check(find(toObject.getPropertyDescriptors(), "class") == null, "stopClass Object hides class");
        check(toObject.getPropertyDescriptors().length == 2, "stopClass Object leaves exactly a,b");

        BeanInfo four = Introspector.getBeanInfo(HSub.class, HBase.class, Introspector.USE_ALL_BEANINFO);
        check(four != null && find(four.getPropertyDescriptors(), "b") != null
                && find(four.getPropertyDescriptors(), "a") == null,
                "4-arg stopClass+flags mirrors 3-arg behavior");
        check(Introspector.getBeanInfo(HSub.class, Introspector.IGNORE_ALL_BEANINFO) != null,
                "flags-only overload returns BeanInfo");
        check(Introspector.getBeanInfo(HSub.class, 0) != null, "flags=0 accepted");
    }

    // ---------- deepening: cache identity ----------

    static void testCacheIdentity() throws Exception {
        Introspector.flushCaches();
        BeanInfo b1 = Introspector.getBeanInfo(HBase.class);
        BeanInfo b2 = Introspector.getBeanInfo(HBase.class);
        check(b1 == b2, "same-class getBeanInfo returns cached identical instance");

        PropertyDescriptor pd1 = find(b1.getPropertyDescriptors(), "a");
        PropertyDescriptor pd2 = find(Introspector.getBeanInfo(HBase.class).getPropertyDescriptors(), "a");
        check(pd1 == pd2, "cached PropertyDescriptor identical instance across calls");

        PropertyDescriptor[] arr1 = b1.getPropertyDescriptors();
        PropertyDescriptor[] arr2 = b1.getPropertyDescriptors();
        check(arr1 == arr2, "getPropertyDescriptors returns internal array (no defensive copy)");
        PropertyDescriptor saved = arr1[0];
        arr1[0] = null;
        check(b1.getPropertyDescriptors()[0] == null, "array mutation visible (shared storage, probe)");
        arr1[0] = saved;

        Introspector.flushCaches();
        BeanInfo b3 = Introspector.getBeanInfo(HBase.class);
        check(b3 != b1, "after flushCaches a fresh BeanInfo instance is built");
        PropertyDescriptor pd3 = find(b3.getPropertyDescriptors(), "a");
        check(pd3 != pd1, "after flushCaches a fresh PropertyDescriptor instance is built");
    }

    // ---------- deepening: negatives / odd bean types ----------

    public interface SimpleIface { int getA(); }
    static class PkgBean {
        private int x;
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
    }

    static void testNegativeAndOddTypes() throws Exception {
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Introspector.getBeanInfo((Class<?>) null);
                }}, "getBeanInfo(null) -> NPE");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Introspector.getBeanInfo(null, Object.class);
                }}, "getBeanInfo(null, stop) -> NPE");
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Introspector.getBeanInfo(HSub.class, String.class, Introspector.USE_ALL_BEANINFO);
                }}, "4-arg invalid stopClass -> IntrospectionException");
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    Introspector.getBeanInfo(HSub.class, HSub.class, 0);
                }}, "4-arg stopClass == beanClass -> IntrospectionException");

        // interface introspection works; getters become properties; Object methods excluded
        BeanInfo ifBi = Introspector.getBeanInfo(SimpleIface.class);
        check(ifBi.getPropertyDescriptors().length == 1, "interface yields exactly one property");
        check(find(ifBi.getPropertyDescriptors(), "a") != null, "interface getter becomes property a");
        check(find(ifBi.getPropertyDescriptors(), "class") == null, "interface has no Object class property");

        // package-private nested class in an accessible package is introspectable (probe: no exception)
        BeanInfo ppBi = Introspector.getBeanInfo(PkgBean.class);
        check(find(ppBi.getPropertyDescriptors(), "x") != null, "package-private class introspectable");

        // primitive / boxed / array classes accepted
        check(Introspector.getBeanInfo(int.class).getPropertyDescriptors().length == 0,
                "primitive class has no methods -> zero properties (probe)");
        check(find(Introspector.getBeanInfo(String[].class).getPropertyDescriptors(), "class") != null,
                "array class -> class property only");
    }

    // ---------- deepening: search path null / flag equivalence ----------

    static void testSearchPathNull() {
        String[] original = Introspector.getBeanInfoSearchPath();
        try {
            Introspector.setBeanInfoSearchPath(null);
            String[] sp = Introspector.getBeanInfoSearchPath();
            check(sp != null, "getBeanInfoSearchPath never null");
            check(sp.length == 0, "setBeanInfoSearchPath(null) stores empty path (probe)");
        } finally {
            Introspector.setBeanInfoSearchPath(original);
        }
        check(java.util.Arrays.equals(original, Introspector.getBeanInfoSearchPath()),
                "search path restored byte-for-byte");
    }

    static java.util.List<String> sortedPropNames(BeanInfo bi) {
        java.util.List<String> names = new java.util.ArrayList<String>();
        for (PropertyDescriptor pd : bi.getPropertyDescriptors()) names.add(pd.getName());
        java.util.Collections.sort(names);
        return names;
    }

    static void testFlagEquivalence() throws Exception {
        java.util.List<String> expected = sortedPropNames(Introspector.getBeanInfo(HBase.class));
        int[] flags = { Introspector.USE_ALL_BEANINFO, Introspector.IGNORE_IMMEDIATE_BEANINFO,
                        Introspector.IGNORE_ALL_BEANINFO, 0 };
        String[] labels = { "USE_ALL_BEANINFO", "IGNORE_IMMEDIATE_BEANINFO", "IGNORE_ALL_BEANINFO", "0" };
        for (int i = 0; i < flags.length; i++) {
            check(expected.equals(sortedPropNames(Introspector.getBeanInfo(HBase.class, flags[i]))),
                    "flag " + labels[i] + " yields identical property set (no explicit BeanInfo)");
        }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testDecapitalize();
        testGetBeanInfo();
        testFlagsAndStopClass();
        testCachesAndSearchPath();
        testDecapitalizeMatrix();
        testHierarchyAndStopClasses();
        testCacheIdentity();
        testNegativeAndOddTypes();
        testSearchPathNull();
        testFlagEquivalence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
