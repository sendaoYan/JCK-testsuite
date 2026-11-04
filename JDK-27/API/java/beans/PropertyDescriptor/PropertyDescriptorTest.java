/*
 * @test
 * @summary java.beans.PropertyDescriptor conformance: full contract coverage.
 *          all three constructors + read-only/write-only forms, exact
 *          IntrospectionException triggers (null/empty name, null bean,
 *          missing method, bad read arg count, void read method, type
 *          mismatch), bound/constrained toggling, property editor class and
 *          createPropertyEditor (null when absent / non-editor class),
 *          setReadMethod/setWriteMethod null clearing, equals/hashCode.
 * @run main PropertyDescriptorTest
 */

import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Method;

public class PropertyDescriptorTest {
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
        private int foo;
        private boolean bar;
        private String name;
        public int getFoo() { return foo; }
        public void setFoo(int v) { foo = v; }
        public boolean isBar() { return bar; }
        public void setBar(boolean b) { bar = b; }
        public String getName() { return name; }
        public void setName(String v) { name = v; }
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
        public void voidReturn() {}
        public int takesArg(int x) { return x; }
    }

    static void testBasicConstructor() throws Exception {
        PropertyDescriptor pd = new PropertyDescriptor("foo", Bean.class);
        check("foo".equals(pd.getName()), "name");
        check(int.class == pd.getPropertyType(), "propertyType int");
        check("getFoo".equals(pd.getReadMethod().getName()), "readMethod");
        check("setFoo".equals(pd.getWriteMethod().getName()), "writeMethod");
        check(pd.isBound(), "bound auto-detected");
        check(!pd.isConstrained(), "constrained default false");

        PropertyDescriptor bar = new PropertyDescriptor("bar", Bean.class);
        check(boolean.class == bar.getPropertyType(), "boolean propertyType");
        check("isBar".equals(bar.getReadMethod().getName()), "is* readMethod");
    }

    static void testReadWriteConstructors() throws Exception {
        // write-only: read method name null
        PropertyDescriptor wo = new PropertyDescriptor("foo", Bean.class, null, "setFoo");
        check(wo.getReadMethod() == null, "write-only read method null");
        check("setFoo".equals(wo.getWriteMethod().getName()), "write-only write method");

        // read-only: write method name null
        PropertyDescriptor ro = new PropertyDescriptor("foo", Bean.class, "getFoo", null);
        check("getFoo".equals(ro.getReadMethod().getName()), "read-only read method");
        check(ro.getWriteMethod() == null, "read-only write method null");

        Method g = Bean.class.getMethod("getFoo");
        Method s = Bean.class.getMethod("setFoo", int.class);
        PropertyDescriptor mp = new PropertyDescriptor("foo", g, s);
        check(int.class == mp.getPropertyType(), "method-pair ctor type");
        check(g == mp.getReadMethod() && s == mp.getWriteMethod(), "method-pair identity");

        PropertyDescriptor wo2 = new PropertyDescriptor("foo", null, s);
        check(wo2.getReadMethod() == null && s == wo2.getWriteMethod(), "method-pair write-only");
    }

    static void testConstructorExceptions() throws Exception {
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor(null, Bean.class);
                }}, "null name -> IntrospectionException");
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("", Bean.class);
                }}, "empty name -> IntrospectionException");
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("foo", null);
                }}, "null beanClass -> IntrospectionException");
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("nope", Bean.class);
                }}, "missing property -> IntrospectionException");
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("absent", Bean.class, "getAbsent", null);
                }}, "missing read method -> IntrospectionException");
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("absent", Bean.class, null, "setAbsent");
                }}, "missing write method -> IntrospectionException");

        Method badRead = Bean.class.getMethod("takesArg", int.class);
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("x", badRead, null);
                }}, "read method with args -> IntrospectionException");
        Method voidRead = Bean.class.getMethod("voidReturn");
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("x", voidRead, null);
                }}, "void read method -> IntrospectionException");
    }

    static void testSettersAndType() throws Exception {
        PropertyDescriptor pd = new PropertyDescriptor("foo", Bean.class);
        pd.setReadMethod(null);
        check(pd.getReadMethod() == null, "setReadMethod(null) clears");
        pd.setWriteMethod(null);
        check(pd.getWriteMethod() == null, "setWriteMethod(null) clears");

        Method g2 = Bean.class.getMethod("getName");
        pd.setReadMethod(g2);
        check(g2 == pd.getReadMethod() && String.class == pd.getPropertyType(),
                "setReadMethod updates type");

        Method s2 = Bean.class.getMethod("setName", String.class);
        pd.setWriteMethod(s2);
        check(s2 == pd.getWriteMethod(), "setWriteMethod");

        Method badRead = Bean.class.getMethod("getFoo"); // int vs String write -> mismatch
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    pd.setReadMethod(badRead);
                }}, "setReadMethod type mismatch -> IntrospectionException");

        Method argRead = Bean.class.getMethod("takesArg", int.class);
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    pd.setReadMethod(argRead);
                }}, "setReadMethod bad arg count -> IntrospectionException");
    }

    static void testBoundConstrainedEditor() throws Exception {
        PropertyDescriptor pd = new PropertyDescriptor("foo", Bean.class);
        pd.setBound(true);
        check(pd.isBound(), "setBound true");
        pd.setConstrained(true);
        check(pd.isConstrained(), "setConstrained true");
        pd.setBound(false);
        check(!pd.isBound(), "setBound false");

        check(pd.getPropertyEditorClass() == null, "default editor class null");
        pd.setPropertyEditorClass(PropertyEditorSupport.class);
        check(PropertyEditorSupport.class == pd.getPropertyEditorClass(), "setPropertyEditorClass");
        PropertyEditor pe = pd.createPropertyEditor(null);
        check(pe instanceof PropertyEditorSupport, "createPropertyEditor instantiates");
        pd.setPropertyEditorClass(null);
        check(pd.createPropertyEditor(null) == null, "createPropertyEditor null when no editor class");
        pd.setPropertyEditorClass(String.class); // not a PropertyEditor
        check(pd.createPropertyEditor(null) == null, "createPropertyEditor null for non-editor class");
    }

    static void testEqualsHashCode() throws Exception {
        PropertyDescriptor a = new PropertyDescriptor("foo", Bean.class);
        PropertyDescriptor b = new PropertyDescriptor("foo", Bean.class);
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        PropertyDescriptor c = new PropertyDescriptor("bar", Bean.class);
        check(!a.equals(c), "different name not equal");
        check(!a.equals(null), "not equals null");
        check(!a.equals("foo"), "not equals non-PropertyDescriptor");
    }


    static void testStructure() {
        check(java.beans.FeatureDescriptor.class.isAssignableFrom(PropertyDescriptor.class), "extends FeatureDescriptor");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBasicConstructor();
        testReadWriteConstructors();
        testConstructorExceptions();
        testSettersAndType();
        testBoundConstrainedEditor();
        testEqualsHashCode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
