/*
 * @test
 * @summary java.beans.BeanProperty conformance (JDK 9+ only): annotation
 *          reflection (RUNTIME retention, Target({METHOD}) exact,
 *          Documented, all 8 elements with exact defaults: bound=true,
 *          expert/hidden/preferred/required/visualUpdate=false,
 *          description="", enumerationValues={}) and behavioral contract
 *          with Introspector: expert/hidden/preferred/description applied
 *          to the PropertyDescriptor, required/visualUpdate exposed via
 *          getValue attributes, bound=true only takes effect together
 *          with add/removePropertyChangeListener methods (probe matrix),
 *          and enumerationValues resolved against public static fields
 *          into Object[] {name, value, rawString} triplets (non-static
 *          field reference yields the empty default).
 * @run main BeanPropertyTest
 */

import java.beans.BeanInfo;
import java.beans.BeanProperty;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public class BeanPropertyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static PropertyDescriptor findProp(Class<?> bean, String name) throws Exception {
        BeanInfo bi = Introspector.getBeanInfo(bean);
        for (PropertyDescriptor pd : bi.getPropertyDescriptors()) if (name.equals(pd.getName())) return pd;
        return null;
    }

    // ---- sample beans (public static: Introspector requirement) ----
    public static class Bean {
        @BeanProperty
        public int getFoo() { return 1; }
        @BeanProperty(bound = false, expert = true, description = "d")
        public int getBar() { return 2; }
    }
    public static class BPFlagged {
        private int foo;
        @BeanProperty(bound = true, expert = true, preferred = true, hidden = true, description = "the flagged one")
        public int getFoo() { return foo; }
        public void setFoo(int v) { foo = v; }
    }
    public static class BPBound {
        @BeanProperty(bound = true) public int getT() { return 1; }
        public void setT(int v) {}
        @BeanProperty(bound = false) public int getF() { return 1; }
        public void setF(int v) {}
        public int getP() { return 1; }
        public void setP(int v) {}
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
    }
    public static class BPNoListener {
        @BeanProperty(bound = true) public int getT() { return 1; }
        public void setT(int v) {}
    }
    public static class BPEnum {
        public static final int ONE = 1;
        public static final int TWO = 2;
        public int INSTANCE = 9; // public but non-static
        @BeanProperty(enumerationValues = {"ONE", "TWO"})
        public int getLevel() { return 1; }
        public void setLevel(int v) {}
    }
    public static class BPNonStatic {
        public int INSTANCE = 5;
        @BeanProperty(enumerationValues = {"INSTANCE"})
        public int getLevel() { return 1; }
        public void setLevel(int v) {}
    }
    public static class BPRequired {
        @BeanProperty(required = true, visualUpdate = true)
        public int getBaz() { return 0; }
        public void setBaz(int v) {}
    }
    public static class BPBool {
        @BeanProperty(expert = true, description = "flag desc")
        public boolean isFlag() { return true; }
        public void setFlag(boolean v) {}
    }

    // existing checks (kept)
    static void testWhiteBoxBase() throws Exception {
        Class<?> c = Class.forName("java.beans.BeanProperty");
        check(c.isAnnotation(), "is an annotation");
        check(c.getMethod("bound").getReturnType() == boolean.class, "bound returns boolean");
    }

    // existing checks (kept)
    static void testBlackBoxBase() throws Exception {
        BeanProperty foo = Bean.class.getMethod("getFoo").getAnnotation(BeanProperty.class);
        check(foo != null, "foo annotation present");
        check(foo.bound(), "bound default true");
        check(!foo.expert() && !foo.hidden() && !foo.preferred() && !foo.required() && !foo.visualUpdate(), "other bool defaults false");
        check("".equals(foo.description()), "description default empty");
        check(foo.enumerationValues().length == 0, "enumerationValues default empty");

        BeanProperty bar = Bean.class.getMethod("getBar").getAnnotation(BeanProperty.class);
        check(!bar.bound(), "bound false");
        check(bar.expert(), "expert true");
        check("d".equals(bar.description()), "description d");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.BeanProperty");
        check(c.getAnnotation(Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME,
                "RUNTIME retention");
        Target target = c.getAnnotation(Target.class);
        check(target != null && target.value().length == 1 && target.value()[0] == ElementType.METHOD,
                "@Target({METHOD}) exact");
        check(c.getAnnotation(Documented.class) != null, "@Documented (probe)");
        // exact defaults of all 8 elements via reflection
        check(Boolean.TRUE.equals(c.getMethod("bound").getDefaultValue()), "bound default true");
        check(Boolean.FALSE.equals(c.getMethod("expert").getDefaultValue()), "expert default false");
        check(Boolean.FALSE.equals(c.getMethod("hidden").getDefaultValue()), "hidden default false");
        check(Boolean.FALSE.equals(c.getMethod("preferred").getDefaultValue()), "preferred default false");
        check(Boolean.FALSE.equals(c.getMethod("required").getDefaultValue()), "required default false");
        check(Boolean.FALSE.equals(c.getMethod("visualUpdate").getDefaultValue()), "visualUpdate default false");
        check("".equals(c.getMethod("description").getDefaultValue()), "description default empty string");
        Object ev = c.getMethod("enumerationValues").getDefaultValue();
        check(ev instanceof String[] && ((String[]) ev).length == 0, "enumerationValues default empty String[]");
        // element types
        check(c.getMethod("bound").getReturnType() == boolean.class, "bound boolean");
        check(c.getMethod("expert").getReturnType() == boolean.class, "expert boolean");
        check(c.getMethod("hidden").getReturnType() == boolean.class, "hidden boolean");
        check(c.getMethod("preferred").getReturnType() == boolean.class, "preferred boolean");
        check(c.getMethod("required").getReturnType() == boolean.class, "required boolean");
        check(c.getMethod("visualUpdate").getReturnType() == boolean.class, "visualUpdate boolean");
        check(c.getMethod("description").getReturnType() == String.class, "description String");
        check(c.getMethod("enumerationValues").getReturnType() == String[].class, "enumerationValues String[]");
        check(c.getDeclaredMethods().length == 8, "exactly 8 elements");
        // annotation instances
        BeanProperty a1 = Bean.class.getMethod("getFoo").getAnnotation(BeanProperty.class);
        BeanProperty a2 = Bean.class.getMethod("getFoo").getAnnotation(BeanProperty.class);
        check(a1.equals(a2), "annotation proxies equal across lookups");
        check(a1.hashCode() == a2.hashCode(), "annotation hashCode stable");
        check(a1.annotationType() == BeanProperty.class, "annotationType");
    }

    static void testFlaggedDescriptor() throws Exception {
        Introspector.flushCaches();
        PropertyDescriptor foo = findProp(BPFlagged.class, "foo");
        check(foo != null, "flagged property present");
        check(foo.isExpert(), "expert applied");
        check(foo.isHidden(), "hidden applied");
        check(foo.isPreferred(), "preferred applied");
        check("the flagged one".equals(foo.getShortDescription()), "description -> short description");
        check(!"the flagged one".equals(foo.getDisplayName()), "displayName stays property name");
        check(Boolean.TRUE.equals(foo.getValue("expert")), "expert value attribute TRUE");
        check(Boolean.TRUE.equals(foo.getValue("hidden")), "hidden value attribute TRUE");
        check(foo.isBound() == false, "bound=true without listener methods not honored (probe)");
        check(foo.getReadMethod() != null && foo.getWriteMethod() != null, "read/write methods intact");
        // defaults for untouched flags on an annotated property
        check(foo.isConstrained() == false, "not constrained");
    }

    static void testBoundMatrix() throws Exception {
        Introspector.flushCaches();
        check(findProp(BPBound.class, "t").isBound(), "bound=true + listener methods -> bound");
        check(!findProp(BPBound.class, "f").isBound(), "bound=false + listener methods -> not bound");
        check(findProp(BPBound.class, "p").isBound(), "unannotated + listener methods -> bound");
        check(!findProp(BPNoListener.class, "t").isBound(), "bound=true without listener methods -> not bound");
    }

    static void testEnumerationValues() throws Exception {
        Introspector.flushCaches();
        PropertyDescriptor level = findProp(BPEnum.class, "level");
        check(level != null, "enum property present");
        Object ev = level.getValue("enumerationValues");
        check(ev instanceof Object[], "enumerationValues attribute is Object[]");
        Object[] triplets = (Object[]) ev;
        check(triplets.length == 6, "two entries x 3 elements");
        check("ONE".equals(triplets[0]), "entry1 name");
        check(Integer.valueOf(1).equals(triplets[1]), "entry1 resolved field value 1");
        check("ONE".equals(triplets[2]), "entry1 raw string");
        check("TWO".equals(triplets[3]), "entry2 name");
        check(Integer.valueOf(2).equals(triplets[4]), "entry2 resolved field value 2");
        check("TWO".equals(triplets[5]), "entry2 raw string");

        PropertyDescriptor nonStatic = findProp(BPNonStatic.class, "level");
        Object ev2 = nonStatic.getValue("enumerationValues");
        check(ev2 instanceof Object[] && ((Object[]) ev2).length == 0,
                "non-static field reference not resolved -> empty (probe)");
    }

    static void testRequiredVisualUpdate() throws Exception {
        Introspector.flushCaches();
        PropertyDescriptor baz = findProp(BPRequired.class, "baz");
        check(Boolean.TRUE.equals(baz.getValue("required")), "required attribute TRUE");
        check(Boolean.TRUE.equals(baz.getValue("visualUpdate")), "visualUpdate attribute TRUE");
    }

    static void testBooleanIsGetter() throws Exception {
        Introspector.flushCaches();
        PropertyDescriptor flag = findProp(BPBool.class, "flag");
        check(flag != null, "boolean is* property present");
        check(flag.isExpert(), "annotation applied to is-getter");
        check("flag desc".equals(flag.getShortDescription()), "is-getter description applied");
        check(flag.getPropertyType() == boolean.class, "boolean property type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBoxBase();
        testBlackBoxBase();
        testWhiteBox();
        testFlaggedDescriptor();
        testBoundMatrix();
        testEnumerationValues();
        testRequiredVisualUpdate();
        testBooleanIsGetter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
