/*
 * @test
 * @summary java.beans.JavaBean conformance (JDK 9+ only): annotation
 *          reflection (RUNTIME retention, Target({TYPE}) exact,
 *          Documented, not Inherited, three String elements description/
 *          defaultProperty/defaultEventSet all defaulting to "") and
 *          behavioral contract with Introspector: defaultProperty/default
 *          EventSet drive BeanInfo.getDefaultPropertyIndex() and
 *          getDefaultEventIndex() (find-by-name, also for inherited
 *          properties), description drives BeanDescriptor short
 *          description, missing names leave the index at -1, and a bare JavaBean annotation behaves like no annotation at all for indexes.
 * @run main JavaBeanTest
 */

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.Introspector;
import java.beans.JavaBean;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public class JavaBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static PropertyDescriptor findProp(BeanInfo bi, String name) {
        for (PropertyDescriptor pd : bi.getPropertyDescriptors()) if (name.equals(pd.getName())) return pd;
        return null;
    }
    static EventSetDescriptor findEvent(BeanInfo bi, String name) {
        for (EventSetDescriptor esd : bi.getEventSetDescriptors()) if (name.equals(esd.getName())) return esd;
        return null;
    }

    // ---- sample beans ----
    public interface FooListener extends java.util.EventListener { void fooHappened(java.util.EventObject e); }
    public interface BarListener extends java.util.EventListener { void barHappened(java.util.EventObject e); }

    @JavaBean(description = "my bean", defaultProperty = "name", defaultEventSet = "foo")
    public static class Annotated {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return 0; }
        public void setAge(int age) {}
        public void addFooListener(FooListener l) {}
        public void removeFooListener(FooListener l) {}
        public void addBarListener(BarListener l) {}
        public void removeBarListener(BarListener l) {}
    }

    public static class JBBase {
        private int a;
        public int getA() { return a; }
        public void setA(int a) { this.a = a; }
    }
    @JavaBean(defaultProperty = "a")
    public static class JBSub extends JBBase {
        private int b;
        public int getB() { return b; }
        public void setB(int b) { this.b = b; }
    }

    @JavaBean(defaultProperty = "missing")
    public static class JBMissingProp {
        public int getA() { return 1; }
        public void setA(int v) {}
    }
    @JavaBean(defaultEventSet = "nope")
    public static class JBMissingEvent {
        public int getA() { return 1; }
        public void setA(int v) {}
        public void addFooListener(FooListener l) {}
        public void removeFooListener(FooListener l) {}
    }
    @JavaBean
    public static class JBBare {
        public int getA() { return 1; }
        public void setA(int v) {}
    }
    public static class Plain {
        public int getA() { return 1; }
        public void setA(int v) {}
    }

    // existing checks (kept)
    static void testWhiteBoxBase() throws Exception {
        Class<?> c = Class.forName("java.beans.JavaBean");
        check(c.isAnnotation(), "is an annotation");
        check(c.getMethod("description").getReturnType() == String.class, "description returns String");
    }

    // existing checks (kept)
    static void testBlackBoxBase() throws Exception {
        JavaBean jb = Annotated.class.getAnnotation(JavaBean.class);
        check(jb != null, "annotation present");
        check("my bean".equals(jb.description()), "description");
        check("name".equals(jb.defaultProperty()), "defaultProperty");
        check("foo".equals(jb.defaultEventSet()), "defaultEventSet");

        // defaults
        JavaBean def = java.beans.JavaBean.class.getAnnotation(JavaBean.class);
        check(def == null, "JavaBean itself not annotated");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.JavaBean");
        check(c.getAnnotation(Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME,
                "RUNTIME retention");
        Target target = c.getAnnotation(Target.class);
        check(target != null && target.value().length == 1 && target.value()[0] == ElementType.TYPE,
                "@Target({TYPE}) exact");
        check(c.getAnnotation(Documented.class) != null, "@Documented (probe)");
        check(c.getAnnotation(Inherited.class) == null, "not @Inherited (probe)");
        check("".equals(c.getMethod("description").getDefaultValue()), "description default empty");
        check("".equals(c.getMethod("defaultProperty").getDefaultValue()), "defaultProperty default empty");
        check("".equals(c.getMethod("defaultEventSet").getDefaultValue()), "defaultEventSet default empty");
        check(c.getMethod("defaultProperty").getReturnType() == String.class, "defaultProperty String");
        check(c.getMethod("defaultEventSet").getReturnType() == String.class, "defaultEventSet String");
        check(c.getDeclaredMethods().length == 3, "exactly 3 elements");
        JavaBean a1 = Annotated.class.getAnnotation(JavaBean.class);
        JavaBean a2 = Annotated.class.getAnnotation(JavaBean.class);
        check(a1.equals(a2), "annotation proxies equal across lookups");
        check(a1.hashCode() == a2.hashCode(), "annotation hashCode stable");
        check(a1.annotationType() == JavaBean.class, "annotationType");
    }

    static void testDefaultIndexes() throws Exception {
        Introspector.flushCaches();
        BeanInfo bi = Introspector.getBeanInfo(Annotated.class);
        int idx = bi.getDefaultPropertyIndex();
        check(idx >= 0, "defaultPropertyIndex resolved");
        check(idx < bi.getPropertyDescriptors().length, "defaultPropertyIndex within array bounds");
        check("name".equals(bi.getPropertyDescriptors()[idx].getName()),
                "defaultPropertyIndex points at the 'name' descriptor (find-by-name)");
        int eidx = bi.getDefaultEventIndex();
        check(eidx >= 0, "defaultEventIndex resolved");
        check(eidx < bi.getEventSetDescriptors().length, "defaultEventIndex within array bounds");
        check("foo".equals(bi.getEventSetDescriptors()[eidx].getName()),
                "defaultEventIndex points at the 'foo' event set (find-by-name)");
        // supporting discovery facts
        check(findProp(bi, "name") != null && findProp(bi, "age") != null && findProp(bi, "class") != null,
                "properties name, age and class discovered");
        check(findEvent(bi, "foo") != null && findEvent(bi, "bar") != null, "event sets foo and bar discovered");
    }

    static void testBeanDescriptorDescription() throws Exception {
        Introspector.flushCaches();
        BeanInfo bi = Introspector.getBeanInfo(Annotated.class);
        BeanDescriptor bd = bi.getBeanDescriptor();
        check("my bean".equals(bd.getShortDescription()), "description -> short description");
        check(!"my bean".equals(bd.getDisplayName()), "displayName untouched by description");
        check(bd.getBeanClass() == Annotated.class, "bean class");
        check(bd.getCustomizerClass() == null, "no customizer class");
        check(bd.getName() != null && bd.getName().endsWith("Annotated"), "descriptor name is the binary class name");
    }

    static void testMissingNames() throws Exception {
        Introspector.flushCaches();
        check(Introspector.getBeanInfo(JBMissingProp.class).getDefaultPropertyIndex() == -1,
                "defaultProperty naming no property -> index stays -1 (probe)");
        check(Introspector.getBeanInfo(JBMissingEvent.class).getDefaultEventIndex() == -1,
                "defaultEventSet naming no event set -> index stays -1 (probe)");
        BeanInfo bare = Introspector.getBeanInfo(JBBare.class);
        check(bare.getDefaultPropertyIndex() == -1, "bare @JavaBean: property index -1");
        check(bare.getDefaultEventIndex() == -1, "bare @JavaBean: event index -1");
        check(bare.getBeanDescriptor().getShortDescription().equals(bare.getBeanDescriptor().getDisplayName()),
                "bare @JavaBean: description defaults to display name");
    }

    static void testInheritedPropertyAsDefault() throws Exception {
        Introspector.flushCaches();
        BeanInfo bi = Introspector.getBeanInfo(JBSub.class);
        check(findProp(bi, "a") != null && findProp(bi, "b") != null, "inherited a and own b discovered");
        int idx = bi.getDefaultPropertyIndex();
        check(idx >= 0 && "a".equals(bi.getPropertyDescriptors()[idx].getName()),
                "defaultProperty may name an inherited property");
    }

    static void testControl() throws Exception {
        Introspector.flushCaches();
        BeanInfo bi = Introspector.getBeanInfo(Plain.class);
        check(bi.getDefaultPropertyIndex() == -1, "unannotated class: property index -1");
        check(bi.getDefaultEventIndex() == -1, "unannotated class: event index -1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBoxBase();
        testBlackBoxBase();
        testWhiteBox();
        testDefaultIndexes();
        testBeanDescriptorDescription();
        testMissingNames();
        testInheritedPropertyAsDefault();
        testControl();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
