/*
 * @test
 * @summary java.beans.IndexedPropertyDescriptor conformance: full contract.
 *          all three constructors, array + indexed type, indexed read/write
 *          methods, explicit method-name constructor, setIndexedReadMethod /
 *          setIndexedWriteMethod (incl. null clearing and bad-method
 *          IntrospectionException), name validation, equals/hashCode.
 *          Top-up: indexed-only/array-only/all-null constructions, array-vs-
 *          indexed type mismatch rules (2-arg and Method ctor), non-int index
 *          and void-return negatives, non-void indexed write acceptance,
 *          equals family incl. recorded PD/IPD asymmetry, inherited property
 *          features, not serializable.
 * @run main IndexedPropertyDescriptorTest
 */

import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class IndexedPropertyDescriptorTest {
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

    public static class Bean {
        private int[] items = new int[0];
        public int[] getItems() { return items; }
        public void setItems(int[] v) { items = v; }
        public int getItems(int i) { return items[i]; }
        public void setItems(int i, int v) { items[i] = v; }
    }

    // helper beans for the appended deepening tests
    public static class IndexedOnlyBean {
        public String getNames(int i) { return null; }
        public void setNames(int i, String v) {}
    }
    public static class MismatchBean {
        public Integer[] getVals() { return null; }
        public void setVals(Integer[] v) {}
        public String getVals(int i) { return null; }
        public void setVals(int i, String v) {}
    }
    public static class BadIndexBean {
        public String getNames(boolean b) { return null; }
        public void setNames(String idx, String v) {}
    }
    public static class VoidIndexedReadBean {
        public void getNames(int i) {}
        public void setNames(int i, String v) {}
    }
    public static class ReturningIndexedWriteBean {
        public String getNames(int i) { return null; }
        public int setNames(int i, String v) { return 0; }
    }

    static void testBasicConstructor() throws Exception {
        IndexedPropertyDescriptor pd = new IndexedPropertyDescriptor("items", Bean.class);
        check("items".equals(pd.getName()), "name");
        check(int[].class == pd.getPropertyType(), "array propertyType");
        check(int.class == pd.getIndexedPropertyType(), "indexed propertyType");
        check("getItems".equals(pd.getReadMethod().getName()), "array read method");
        check("setItems".equals(pd.getWriteMethod().getName()), "array write method");
        check("getItems".equals(pd.getIndexedReadMethod().getName()), "indexed read method");
        check("setItems".equals(pd.getIndexedWriteMethod().getName()), "indexed write method");
    }

    static void testMethodNameConstructor() throws Exception {
        IndexedPropertyDescriptor pd = new IndexedPropertyDescriptor(
                "items", Bean.class, "getItems", "setItems", "getItems", "setItems");
        check(int[].class == pd.getPropertyType(), "6-arg array type");
        check(int.class == pd.getIndexedPropertyType(), "6-arg indexed type");
    }

    static void testMethodPairConstructor() throws Exception {
        Method gr = Bean.class.getMethod("getItems");
        Method sw = Bean.class.getMethod("setItems", int[].class);
        Method ir = Bean.class.getMethod("getItems", int.class);
        Method iw = Bean.class.getMethod("setItems", int.class, int.class);
        IndexedPropertyDescriptor pd = new IndexedPropertyDescriptor("items", gr, sw, ir, iw);
        check(gr == pd.getReadMethod() && sw == pd.getWriteMethod(), "array methods identity");
        check(ir == pd.getIndexedReadMethod() && iw == pd.getIndexedWriteMethod(), "indexed methods identity");
        check(int.class == pd.getIndexedPropertyType(), "indexed type");
    }

    static void testSetters() throws Exception {
        IndexedPropertyDescriptor pd = new IndexedPropertyDescriptor("items", Bean.class);
        Method ir = Bean.class.getMethod("getItems", int.class);
        Method iw = Bean.class.getMethod("setItems", int.class, int.class);
        pd.setIndexedReadMethod(ir);
        pd.setIndexedWriteMethod(iw);
        check(ir == pd.getIndexedReadMethod() && iw == pd.getIndexedWriteMethod(), "set indexed methods");

        // clear write while read still set (clears without exception)
        pd.setIndexedWriteMethod(null);
        check(pd.getIndexedWriteMethod() == null, "setIndexedWriteMethod(null) clears");

        // clear read on a fresh descriptor while write still set
        IndexedPropertyDescriptor pd2 = new IndexedPropertyDescriptor("items", Bean.class);
        pd2.setIndexedReadMethod(null);
        check(pd2.getIndexedReadMethod() == null, "setIndexedReadMethod(null) clears");

        Method bad = Bean.class.getMethod("getItems"); // wrong arg count for indexed
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    pd.setIndexedReadMethod(bad);
                }}, "setIndexedReadMethod bad arg count -> IntrospectionException");
    }

    static void testExceptionsAndEquals() throws Exception {
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new IndexedPropertyDescriptor(null, Bean.class);
                }}, "null name -> IntrospectionException");

        IndexedPropertyDescriptor a = new IndexedPropertyDescriptor("items", Bean.class);
        IndexedPropertyDescriptor b = new IndexedPropertyDescriptor("items", Bean.class);
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        check(!a.equals(null), "not equals null");
    }


    static void testStructure() {
        check(PropertyDescriptor.class.isAssignableFrom(IndexedPropertyDescriptor.class), "extends PropertyDescriptor");
    }

    // ---- appended deepening tests (top-up) ----

    static void testIndexedOnlyMethodCtor() throws Exception {
        Method ir = IndexedOnlyBean.class.getMethod("getNames", int.class);
        Method iw = IndexedOnlyBean.class.getMethod("setNames", int.class, String.class);
        IndexedPropertyDescriptor pd = new IndexedPropertyDescriptor("names", null, null, ir, iw);
        check(pd.getReadMethod() == null && pd.getWriteMethod() == null,
                "indexed-only: array accessors null");
        check(ir == pd.getIndexedReadMethod() && iw == pd.getIndexedWriteMethod(),
                "indexed-only: indexed accessor identity");
        check(pd.getPropertyType() == null, "indexed-only: array propertyType null");
        check(String.class == pd.getIndexedPropertyType(), "indexed-only: indexed type String");
    }

    static void testAllNullMethodCtor() throws Exception {
        IndexedPropertyDescriptor pd = new IndexedPropertyDescriptor("names", null, null, null, null);
        check(pd.getPropertyType() == null, "all-null: propertyType null");
        check(pd.getIndexedPropertyType() == null, "all-null: indexedPropertyType null");
        check(pd.getReadMethod() == null && pd.getWriteMethod() == null, "all-null: array accessors null");
        check(pd.getIndexedReadMethod() == null && pd.getIndexedWriteMethod() == null,
                "all-null: indexed accessors null");
        check("names".equals(pd.getName()), "all-null: name kept");
    }

    static void testArrayOnlyMethodCtorThrows() throws Exception {
        // array accessors present but no indexed accessors: indexed type null mismatches array type
        Method gr = Bean.class.getMethod("getItems");
        Method sw = Bean.class.getMethod("setItems", int[].class);
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new IndexedPropertyDescriptor("items", gr, sw, null, null);
                }}, "array-only Method ctor -> IntrospectionException");
    }

    static void testMismatchTypeRules() throws Exception {
        // explicit Method-pair with Integer[] array vs String indexed -> mismatch
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new IndexedPropertyDescriptor("vals",
                            MismatchBean.class.getMethod("getVals"),
                            MismatchBean.class.getMethod("setVals", Integer[].class),
                            MismatchBean.class.getMethod("getVals", int.class),
                            MismatchBean.class.getMethod("setVals", int.class, String.class));
                }}, "array/indexed type mismatch via Method ctor -> IntrospectionException");
        // (name, Class) on a mismatched bean: lookup failure is reported as Method not found
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new IndexedPropertyDescriptor("vals", MismatchBean.class);
                }}, "2-arg ctor on mismatched bean -> IntrospectionException");
        // (name, Class) on an indexed-only bean: array accessors are required
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new IndexedPropertyDescriptor("names", IndexedOnlyBean.class);
                }}, "2-arg ctor on indexed-only bean -> IntrospectionException");
        // 6-arg with null array read/write: indexed-only via names works
        IndexedPropertyDescriptor pd = new IndexedPropertyDescriptor(
                "names", IndexedOnlyBean.class, null, null, "getNames", "setNames");
        check(pd.getReadMethod() == null && pd.getWriteMethod() == null,
                "6-arg with null array names: array accessors null");
        check(String.class == pd.getIndexedPropertyType(),
                "6-arg with null array names: indexed type resolved");
    }

    static void testIndexedSetterNegatives() throws Exception {
        IndexedPropertyDescriptor base = new IndexedPropertyDescriptor("names",
                null, null,
                IndexedOnlyBean.class.getMethod("getNames", int.class),
                IndexedOnlyBean.class.getMethod("setNames", int.class, String.class));
        // indexed write whose first parameter is not int
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    base.setIndexedWriteMethod(BadIndexBean.class.getMethod("setNames", String.class, String.class));
                }}, "setIndexedWriteMethod(non-int index) -> IntrospectionException");
        // indexed write with wrong arg count
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    base.setIndexedWriteMethod(IndexedOnlyBean.class.getMethod("getNames", int.class));
                }}, "setIndexedWriteMethod(1-arg) -> IntrospectionException");
        // indexed read whose parameter is not int
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    base.setIndexedReadMethod(BadIndexBean.class.getMethod("getNames", boolean.class));
                }}, "setIndexedReadMethod(non-int index) -> IntrospectionException");
        // indexed read returning void
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new IndexedPropertyDescriptor("names", null, null,
                            VoidIndexedReadBean.class.getMethod("getNames", int.class),
                            VoidIndexedReadBean.class.getMethod("setNames", int.class, String.class));
                }}, "void indexed read -> IntrospectionException");
        // indexed write with non-void return is legal
        IndexedPropertyDescriptor ret = new IndexedPropertyDescriptor("names", null, null,
                ReturningIndexedWriteBean.class.getMethod("getNames", int.class),
                ReturningIndexedWriteBean.class.getMethod("setNames", int.class, String.class));
        check(String.class == ret.getIndexedPropertyType(),
                "non-void indexed write accepted, type from indexed read");
    }

    static void testEqualsFamily() throws Exception {
        IndexedPropertyDescriptor a = new IndexedPropertyDescriptor("items", Bean.class);
        IndexedPropertyDescriptor b = new IndexedPropertyDescriptor("items", Bean.class);
        check(a.equals(b) && b.equals(a), "equal for same construction");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        // differing indexed accessors break equality
        IndexedPropertyDescriptor c = new IndexedPropertyDescriptor("items", Bean.class);
        c.setIndexedReadMethod(null);
        check(!a.equals(c), "cleared indexed read breaks equals");
        // asymmetry recorded as actual behavior: IPD.equals(plain PD) is false
        // while the plain PD considers the IPD equal when base accessors match
        PropertyDescriptor plain = new PropertyDescriptor("items", Bean.class);
        check(!a.equals(plain), "IPD.equals(PD) false (recorded actual)");
        check(plain.equals(a), "PD.equals(IPD) true when base property matches (recorded actual)");
    }

    static void testInheritedPropertyFeatures() throws Exception {
        IndexedPropertyDescriptor pd = new IndexedPropertyDescriptor("items", Bean.class);
        check(!pd.isBound() && !pd.isConstrained(), "inherited flags default false");
        pd.setBound(true);
        check(pd.isBound(), "setBound on IPD");
        pd.setConstrained(true);
        check(pd.isConstrained(), "setConstrained on IPD");
        pd.setPropertyEditorClass(java.beans.PropertyEditorSupport.class);
        check(java.beans.PropertyEditorSupport.class == pd.getPropertyEditorClass(),
                "setPropertyEditorClass on IPD");
        pd.setValue("k", "v");
        check("v".equals(pd.getValue("k")), "setValue inherited on IPD");
    }

    static void testNotSerializable() throws Exception {
        check(!java.io.Serializable.class.isAssignableFrom(IndexedPropertyDescriptor.class),
                "IndexedPropertyDescriptor is not Serializable");
        final IndexedPropertyDescriptor pd = new IndexedPropertyDescriptor("items", Bean.class);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        final java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        try {
            oos.writeObject(pd);
            check(false, "expected NotSerializableException");
        } catch (java.io.NotSerializableException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBasicConstructor();
        testMethodNameConstructor();
        testMethodPairConstructor();
        testSetters();
        testExceptionsAndEquals();
        testIndexedOnlyMethodCtor();
        testAllNullMethodCtor();
        testArrayOnlyMethodCtorThrows();
        testMismatchTypeRules();
        testIndexedSetterNegatives();
        testEqualsFamily();
        testInheritedPropertyFeatures();
        testNotSerializable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
