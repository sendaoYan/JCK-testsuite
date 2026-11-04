/*
 * @test
 * @summary java.beans.IndexedPropertyDescriptor conformance: full contract.
 *          all three constructors, array + indexed type, indexed read/write
 *          methods, explicit method-name constructor, setIndexedReadMethod /
 *          setIndexedWriteMethod (incl. null clearing and bad-method
 *          IntrospectionException), name validation, equals/hashCode.
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

    public static void main(String[] args) throws Exception {
        testStructure();
        testBasicConstructor();
        testMethodNameConstructor();
        testMethodPairConstructor();
        testSetters();
        testExceptionsAndEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
