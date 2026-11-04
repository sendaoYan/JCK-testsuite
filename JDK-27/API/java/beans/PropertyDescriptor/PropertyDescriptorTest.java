/*
 * @test
 * @summary java.beans.PropertyDescriptor conformance: full contract coverage.
 *          all three constructors + read-only/write-only forms, exact
 *          IntrospectionException triggers (null/empty name, null bean,
 *          missing method, bad read arg count, void read method, type
 *          mismatch, 2-arg setter), bound/constrained toggling and
 *          Introspector auto-detection, property editor class and
 *          createPropertyEditor (new instance per call, (Object bean) ctor,
 *          null when absent/non-editor/abstract), setReadMethod/setWriteMethod
 *          null clearing, equals/hashCode matrix, not serializable.
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

    // helper beans for the appended deepening tests
    public static class TwoArgSetterBean {
        public int getFoo() { return 0; }
        public void setFoo(int a, int b) {}
    }
    public static class WrongTypesBean {
        public String getFoo() { return null; }
        public void setFoo(int v) {}
    }
    public static class WriteOnlyBean {
        public void setFoo(int v) {}
    }
    public static class PlainBean {
        public int getFoo() { return 0; }
        public void setFoo(int v) {}
    }
    public static class SameShapeBean {
        public int getFoo() { return 0; }
        public void setFoo(int v) {}
    }
    public static class VetoBean {
        private int x;
        public int getX() { return x; }
        public void setX(int v) throws java.beans.PropertyVetoException { x = v; }
        public void addVetoableChangeListener(java.beans.VetoableChangeListener l) {}
        public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {}
    }
    public static class EditorWithBeanCtor extends PropertyEditorSupport {
        public Object received;
        public EditorWithBeanCtor(Object bean) { super(); received = bean; }
    }
    public abstract static class AbstractEditor extends PropertyEditorSupport {}

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

    // ---- appended deepening tests (top-up) ----

    static void testBadAccessorSignatures() throws Exception {
        // 2-arg setter via the method-name constructor -> IntrospectionException
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("foo", TwoArgSetterBean.class, "getFoo", "setFoo");
                }}, "2-arg setter via name ctor -> IntrospectionException");
        // 2-arg setter via the Method-pair constructor -> IntrospectionException
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("foo", TwoArgSetterBean.class.getMethod("getFoo"),
                            TwoArgSetterBean.class.getMethod("setFoo", int.class, int.class));
                }}, "2-arg setter via Method ctor -> IntrospectionException");
        // getter/setter type mismatch: String read + int write
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("foo", WrongTypesBean.class);
                }}, "read/write type mismatch -> IntrospectionException");
        // Method-pair with mismatched types
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("foo", WrongTypesBean.class.getMethod("getFoo"),
                            WrongTypesBean.class.getMethod("setFoo", int.class));
                }}, "Method-pair type mismatch -> IntrospectionException");
        // explicit method names not found, and no conventional fallback either
        checkThrows(IntrospectionException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new PropertyDescriptor("nope", Bean.class, "noGetter", "noSetter");
                }}, "noGetter/noSetter names -> IntrospectionException");
    }

    static void testPropertyTypeEdgeCases() throws Exception {
        // write-only property: type comes from the write method parameter
        PropertyDescriptor wo = new PropertyDescriptor("foo", WriteOnlyBean.class, null, "setFoo");
        check(int.class == wo.getPropertyType(), "write-only propertyType from setter param");
        check(wo.getReadMethod() == null, "write-only has no read method");
        // no methods at all: type null
        PropertyDescriptor empty = new PropertyDescriptor("x", null, null);
        check(empty.getPropertyType() == null, "no methods -> propertyType null");
        check(empty.getReadMethod() == null && empty.getWriteMethod() == null,
                "no methods -> both accessors null");
    }

    static void testCreatePropertyEditorDetails() throws Exception {
        PropertyDescriptor pd = new PropertyDescriptor("foo", PlainBean.class);
        pd.setPropertyEditorClass(PropertyEditorSupport.class);
        PropertyEditor e1 = pd.createPropertyEditor(null);
        PropertyEditor e2 = pd.createPropertyEditor(null);
        check(e1 != null && e2 != null, "editor created");
        check(e1 != e2, "createPropertyEditor returns a new instance each call");
        // editor with an (Object bean) constructor receives the exact bean instance
        PropertyDescriptor pd2 = new PropertyDescriptor("foo", PlainBean.class);
        pd2.setPropertyEditorClass(EditorWithBeanCtor.class);
        PlainBean target = new PlainBean();
        EditorWithBeanCtor withBean = (EditorWithBeanCtor) pd2.createPropertyEditor(target);
        check(withBean != null, "bean-ctor editor created for non-null bean");
        check(withBean.received == target, "bean-ctor editor receives the bean");
        // null bean: the (Object) ctor path is not taken; no no-arg ctor exists -> null
        check(pd2.createPropertyEditor(null) == null,
                "null bean with only-(Object)-ctor editor -> null");
        // abstract editor class cannot be instantiated -> null
        PropertyDescriptor pd3 = new PropertyDescriptor("foo", PlainBean.class);
        pd3.setPropertyEditorClass(AbstractEditor.class);
        check(pd3.createPropertyEditor(null) == null, "abstract editor class -> null");
        check(AbstractEditor.class == pd3.getPropertyEditorClass(),
                "editor class kept even when instantiation fails");
    }

    static void testPropertyEditorClearing() throws Exception {
        PropertyDescriptor pd = new PropertyDescriptor("foo", PlainBean.class);
        pd.setPropertyEditorClass(PropertyEditorSupport.class);
        check(PropertyEditorSupport.class == pd.getPropertyEditorClass(), "editor class set");
        pd.setPropertyEditorClass(null);
        check(pd.getPropertyEditorClass() == null, "setPropertyEditorClass(null) clears");
        check(pd.createPropertyEditor(new PlainBean()) == null,
                "createPropertyEditor null after clearing");
    }

    static void testEqualsMatrix() throws Exception {
        // equal shape via different constructor paths
        PropertyDescriptor byClass = new PropertyDescriptor("foo", PlainBean.class);
        PropertyDescriptor byMethods = new PropertyDescriptor("foo",
                PlainBean.class.getMethod("getFoo"), PlainBean.class.getMethod("setFoo", int.class));
        check(byClass.equals(byMethods), "equal across constructor paths");
        check(byMethods.equals(byClass), "equal across constructor paths (symmetric)");
        check(byClass.hashCode() == byMethods.hashCode(), "hashCode consistent across paths");
        // same shape but a different bean class -> not equal
        PropertyDescriptor otherBean = new PropertyDescriptor("foo", SameShapeBean.class);
        check(!byClass.equals(otherBean), "same shape different bean class not equal");
        // displayName/shortDescription do not participate in equals
        PropertyDescriptor renamed = new PropertyDescriptor("foo", PlainBean.class);
        renamed.setDisplayName("Other");
        renamed.setShortDescription("Other desc");
        check(byClass.equals(renamed), "displayName/shortDescription ignored by equals");
        // bound/constrained flags do participate
        PropertyDescriptor flagged = new PropertyDescriptor("foo", PlainBean.class);
        flagged.setBound(true);
        check(!byClass.equals(flagged), "bound flag difference not equal");
        flagged.setBound(false);
        flagged.setConstrained(true);
        check(!byClass.equals(flagged), "constrained flag difference not equal");
        // property editor class participates
        PropertyDescriptor withEditor = new PropertyDescriptor("foo", PlainBean.class);
        withEditor.setPropertyEditorClass(PropertyEditorSupport.class);
        check(!byClass.equals(withEditor), "editor class difference not equal");
        // cleared write method participates
        PropertyDescriptor noWrite = new PropertyDescriptor("foo", PlainBean.class);
        noWrite.setWriteMethod(null);
        check(!byClass.equals(noWrite), "cleared write method not equal");
    }

    static void testBoundConstrainedAutoDetection() throws Exception {
        // direct construction: no bound detection without Introspector
        PropertyDescriptor plain = new PropertyDescriptor("foo", PlainBean.class);
        check(!plain.isBound(), "no listener methods -> not bound");
        check(!plain.isConstrained(), "never constrained by direct construction");
        // Introspector detects constrained from setter throwing PropertyVetoException
        java.beans.BeanInfo info = java.beans.Introspector.getBeanInfo(VetoBean.class);
        PropertyDescriptor found = null;
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if ("x".equals(pd.getName())) found = pd;
        }
        check(found != null, "veto property found by Introspector");
        check(found.isConstrained(), "setter throws PropertyVetoException -> constrained");
        check(!found.isBound(), "no propertyChange listeners -> not bound");
    }

    static void testNotSerializable() throws Exception {
        check(!java.io.Serializable.class.isAssignableFrom(PropertyDescriptor.class),
                "PropertyDescriptor is not Serializable");
        final PropertyDescriptor pd = new PropertyDescriptor("foo", PlainBean.class);
        pd.setBound(true);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        final java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        try {
            oos.writeObject(pd);
            check(false, "expected NotSerializableException");
        } catch (java.io.NotSerializableException expected) {
            passed++;
        }
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
        testBadAccessorSignatures();
        testPropertyTypeEdgeCases();
        testCreatePropertyEditorDetails();
        testPropertyEditorClearing();
        testEqualsMatrix();
        testBoundConstrainedAutoDetection();
        testNotSerializable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
