/*
 * @test
 * @summary java.beans.Transient conformance: annotation reflection (RUNTIME
 *          retention, Target({METHOD}) exact, single boolean element
 *          value() with default true, not Documented) and behavioral
 *          interaction with Introspector: the annotated property stays
 *          discoverable but its PropertyDescriptor carries the
 *          "transient" attribute set to the annotation value (probe),
 *          getter-only / setter-only annotation, and getter/setter
 *          annotation conflicts resolved in favor of the SETTER on this
 *          JDK release. Version difference: java.beans.BeanProperty and
 *          java.beans.JavaBean (added in JDK 9) do not exist here
 *          (Class.forName -> ClassNotFoundException).
 * @run main TransientTest
 */

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.Transient;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

public class TransientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static PropertyDescriptor findProp(Class<?> bean, String name) throws Exception {
        BeanInfo bi = Introspector.getBeanInfo(bean);
        for (PropertyDescriptor pd : bi.getPropertyDescriptors()) if (name.equals(pd.getName())) return pd;
        return null;
    }

    // ---- sample beans (all public static: Introspector requirement) ----
    public static class TGetTrue {
        @Transient(true) public int getFoo() { return 1; }
        public void setFoo(int v) {}
    }
    public static class TGetFalse {
        @Transient(false) public int getFoo() { return 1; }
        public void setFoo(int v) {}
    }
    public static class TSetOnly {
        public int getFoo() { return 1; }
        @Transient(true) public void setFoo(int v) {}
    }
    public static class TNoAnn {
        public int getFoo() { return 1; }
        public void setFoo(int v) {}
    }
    public static class TConflictSetterWins {
        @Transient(true) public int getFoo() { return 1; }
        @Transient(false) public void setFoo(int v) {}
    }
    public static class TConflictSetterWins2 {
        @Transient(false) public int getFoo() { return 1; }
        @Transient(true) public void setFoo(int v) {}
    }

    // existing structural checks (kept)
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.beans.Transient");
        check(c.isAnnotation(), "annotation");
        check(c.isInterface(), "annotation is an interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.lang.annotation.Annotation").isAssignableFrom(c), "implements/extends java.lang.annotation.Annotation");
        check(hasMethod(c, "value"), "declares value");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Transient");
        check(c.getAnnotation(Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME,
                "RUNTIME retention");
        Target target = c.getAnnotation(Target.class);
        check(target != null && target.value().length == 1 && target.value()[0] == ElementType.METHOD,
                "@Target({METHOD}) exact");
        check(c.getAnnotation(Documented.class) == null, "not @Documented (probe)");
        Method value = c.getMethod("value");
        check(value.getReturnType() == boolean.class, "value() returns primitive boolean");
        check(Boolean.TRUE.equals(value.getDefaultValue()), "value() default is true (probe)");
        check(c.getDeclaredMethods().length == 1, "single element value()");
    }

    static void testAnnotationValues() throws Exception {
        Method a = TGetTrue.class.getMethod("getFoo");
        Method b = TGetFalse.class.getMethod("getFoo");
        Method none = TNoAnn.class.getMethod("getFoo");
        check(a.getAnnotation(Transient.class) != null, "annotation retrievable from getter");
        check(a.getAnnotation(Transient.class).value(), "explicit true value");
        check(!b.getAnnotation(Transient.class).value(), "explicit false value");
        check(none.getAnnotation(Transient.class) == null, "absent on non-annotated method");
        check(a.getAnnotation(Transient.class).equals(a.getAnnotation(Transient.class)),
                "annotation proxy equals across lookups");
        check(a.isAnnotationPresent(Transient.class), "isAnnotationPresent true");
        check(!none.isAnnotationPresent(Transient.class), "isAnnotationPresent false");
    }

    static void testIntrospectorBehavior() throws Exception {
        // annotated getter: property still present, transient attribute true
        PropertyDescriptor foo = findProp(TGetTrue.class, "foo");
        check(foo != null, "@Transient(true) getter: foo property still present (probe)");
        check(Boolean.TRUE.equals(foo.getValue("transient")), "transient attribute Boolean.TRUE");
        check(foo.getReadMethod() != null && "getFoo".equals(foo.getReadMethod().getName()), "read method intact");
        check(foo.getWriteMethod() != null && "setFoo".equals(foo.getWriteMethod().getName()), "write method intact");
        check(foo.getPropertyType() == int.class, "property type intact");

        // @Transient(false)
        PropertyDescriptor bar = findProp(TGetFalse.class, "foo");
        check(bar != null, "@Transient(false): property present");
        check(Boolean.FALSE.equals(bar.getValue("transient")), "transient attribute Boolean.FALSE");

        // annotated setter only
        PropertyDescriptor baz = findProp(TSetOnly.class, "foo");
        check(baz != null, "@Transient(true) setter: property still present");
        check(Boolean.TRUE.equals(baz.getValue("transient")), "setter annotation also sets attribute");

        // no annotation: attribute absent
        PropertyDescriptor plain = findProp(TNoAnn.class, "foo");
        check(plain != null, "plain property present");
        check(plain.getValue("transient") == null, "no transient attribute without annotation");
    }

    static void testGetterSetterConflict() throws Exception {
        // JDK-8: the SETTER's annotation wins (getter processed first, setter overrides)
        PropertyDescriptor c1 = findProp(TConflictSetterWins.class, "foo");
        check(c1 != null, "conflict bean property present");
        check(Boolean.FALSE.equals(c1.getValue("transient")),
                "JDK-8 getter=true/setter=false -> false (setter wins, probe)");
        PropertyDescriptor c2 = findProp(TConflictSetterWins2.class, "foo");
        check(Boolean.TRUE.equals(c2.getValue("transient")),
                "JDK-8 getter=false/setter=true -> true (setter wins, probe)");
    }

    static void testVersionDifferences() {
        // JDK 9 added java.beans.BeanProperty / java.beans.JavaBean; absent here
        try {
            Class.forName("java.beans.BeanProperty");
            check(false, "java.beans.BeanProperty must not exist on JDK-8");
        } catch (ClassNotFoundException expected) {
            check(true, "java.beans.BeanProperty absent -> ClassNotFoundException");
        }
        try {
            Class.forName("java.beans.JavaBean");
            check(false, "java.beans.JavaBean must not exist on JDK-8");
        } catch (ClassNotFoundException expected) {
            check(true, "java.beans.JavaBean absent -> ClassNotFoundException");
        }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testWhiteBox();
        testAnnotationValues();
        testIntrospectorBehavior();
        testGetterSetterConflict();
        testVersionDifferences();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
