/*
 * @test
 * @summary java.beans.ConstructorProperties conformance: annotation
 *          reflection (RUNTIME retention, Target({CONSTRUCTOR}) exact,
 *          Documented, single String[] element value() with no default)
 *          and behavioral contract with the java.beans persistence stack:
 *          DefaultPersistenceDelegate prefers the annotated constructor
 *          (XMLEncoder round-trip preserves constructor-initialized fields,
 *          also when a no-arg constructor coexists), empty value {} and
 *          unknown property names fail encoding with the exception
 *          listener capturing InstantiationException / IllegalStateException
 *          (probe) and producing no object element, and the Introspector
 *          property set is unaffected by constructor annotations.
 * @run main ConstructorPropertiesTest
 */

import java.beans.BeanInfo;
import java.beans.ConstructorProperties;
import java.beans.ExceptionListener;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ConstructorPropertiesTest {
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
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- sample beans ----
    public static class CPBean {
        private int a;
        private String b;
        @ConstructorProperties({"a", "b"})
        public CPBean(int a, String b) { this.a = a; this.b = b; }
        public int getA() { return a; }
        public String getB() { return b; }
        public void setA(int a) { this.a = a; }
        public void setB(String b) { this.b = b; }
    }
    public static class CPBoth {
        private int a;
        public CPBoth() {}
        @ConstructorProperties({"a"})
        public CPBoth(int a) { this.a = a; }
        public int getA() { return a; }
        public void setA(int a) { this.a = a; }
    }
    public static class CPEmpty {
        private int a;
        @ConstructorProperties({})
        public CPEmpty(int a) { this.a = a; }
        public int getA() { return a; }
        public void setA(int a) { this.a = a; }
    }
    public static class CPWrong {
        private int a;
        @ConstructorProperties({"nope"})
        public CPWrong(int a) { this.a = a; }
        public int getA() { return a; }
        public void setA(int a) { this.a = a; }
    }

    // existing structural checks (kept)
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.beans.ConstructorProperties");
        check(c.isAnnotation(), "annotation");
        check(c.isInterface(), "annotation is an interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.lang.annotation.Annotation").isAssignableFrom(c), "implements/extends java.lang.annotation.Annotation");
        check(hasMethod(c, "value"), "declares value");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.ConstructorProperties");
        check(c.getAnnotation(Retention.class).value() == java.lang.annotation.RetentionPolicy.RUNTIME,
                "RUNTIME retention");
        Target target = c.getAnnotation(Target.class);
        check(target != null && target.value().length == 1 && target.value()[0] == ElementType.CONSTRUCTOR,
                "@Target({CONSTRUCTOR}) exact");
        check(c.getAnnotation(Documented.class) != null, "@Documented (probe)");
        check(c.getMethod("value").getReturnType() == String[].class, "value() returns String[]");
        check(c.getMethod("value").getDefaultValue() == null, "value() has no default");
        check(c.getDeclaredMethods().length == 1, "single element value()");
    }

    static void testAnnotationReflection() throws Exception {
        Constructor<CPBean> ctor = CPBean.class.getConstructor(int.class, String.class);
        ConstructorProperties cp = ctor.getAnnotation(ConstructorProperties.class);
        check(cp != null, "annotation present on annotated constructor");
        check(java.util.Arrays.equals(new String[]{"a", "b"}, cp.value()), "value array {a, b}");
        check(cp.value().length == 2, "value length 2");
        check("a".equals(cp.value()[0]) && "b".equals(cp.value()[1]), "value order preserved");
        check(java.util.Arrays.equals(cp.value(), cp.value()), "value consistent across calls");
        check(CPBoth.class.getConstructor().getAnnotation(ConstructorProperties.class) == null,
                "annotation absent on plain constructor");
    }

    static final List<Exception> captured = new ArrayList<Exception>();
    static final List<Exception> capture() {
        captured.clear();
        return captured;
    }

    static String encode(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(bos);
        enc.setExceptionListener(new ExceptionListener() {
            public void exceptionThrown(Exception e) { captured.add(e); }
        });
        enc.writeObject(o);
        enc.close();
        return bos.toString("UTF-8");
    }
    static Object decode(byte[] xml) {
        XMLDecoder dec = new XMLDecoder(new ByteArrayInputStream(xml));
        try { return dec.readObject(); } finally { dec.close(); }
    }

    static void testRoundTrip() throws Exception {
        capture();
        String xml = encode(new CPBean(7, "seven"));
        check(captured.isEmpty(), "no exceptions encoding CPBean");
        check(xml.contains("<object"), "object element written");
        check(xml.contains("ConstructorPropertiesTest$CPBean"), "class name recorded");
        check(xml.contains("<int>7</int>"), "ctor arg a=7 recorded");
        check(xml.contains("<string>seven</string>"), "ctor arg b=seven recorded");
        final byte[] bytes = xml.getBytes("UTF-8");
        Object back = decode(bytes);
        check(back instanceof CPBean, "decoded instance of CPBean");
        check(((CPBean) back).getA() == 7, "field a preserved by ctor round-trip");
        check("seven".equals(((CPBean) back).getB()), "field b preserved by ctor round-trip");
    }

    static void testAnnotatedCtorPreferred() throws Exception {
        capture();
        String xml = encode(new CPBoth(42));
        check(captured.isEmpty(), "no exceptions encoding CPBoth");
        check(xml.contains("<int>42</int>"), "annotated ctor used even with no-arg ctor present");
        check(!xml.contains("<void"), "no setter statements when ctor path taken");
        Object back = decode(xml.getBytes("UTF-8"));
        check(back instanceof CPBoth && ((CPBoth) back).getA() == 42, "CPBoth round-trip a=42");
    }

    static void testEmptyValue() throws Exception {
        capture();
        String xml = encode(new CPEmpty(5));
        check(!captured.isEmpty(), "encoding CPEmpty reports exceptions");
        boolean instantiationReported = false;
        for (Exception e : captured) if (e instanceof InstantiationException) instantiationReported = true;
        check(instantiationReported, "InstantiationException captured (probe)");
        check(!xml.contains("<object"), "no object element emitted");
        final byte[] bytes = xml.getBytes("UTF-8");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() { decode(bytes); } },
                "reading empty document -> ArrayIndexOutOfBoundsException");
    }

    static void testWrongPropertyName() throws Exception {
        capture();
        String xml = encode(new CPWrong(5));
        check(!captured.isEmpty(), "encoding CPWrong reports exceptions");
        boolean illegalState = false;
        boolean illegalArgument = false;
        for (Exception e : captured) {
            if (e instanceof IllegalStateException && e.getMessage() != null && e.getMessage().contains("nope"))
                illegalState = true;
            if (e instanceof IllegalArgumentException) illegalArgument = true;
        }
        check(illegalState, "IllegalStateException mentions unknown property (probe)");
        check(illegalArgument, "IllegalArgumentException follows (probe)");
        check(!xml.contains("<object"), "no object element emitted");
        final byte[] bytes = xml.getBytes("UTF-8");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() { decode(bytes); } },
                "reading discarded document -> ArrayIndexOutOfBoundsException");
    }

    static void testIntrospectorUnaffected() throws Exception {
        BeanInfo bi = Introspector.getBeanInfo(CPBean.class);
        PropertyDescriptor[] pds = bi.getPropertyDescriptors();
        check(pds.length == 3, "exactly a, b, class properties (ctor adds none)");
        PropertyDescriptor a = null, b = null;
        for (PropertyDescriptor pd : pds) {
            if ("a".equals(pd.getName())) a = pd;
            if ("b".equals(pd.getName())) b = pd;
        }
        check(a != null, "property a discovered");
        check(b != null, "property b discovered");
        check(a.getReadMethod() != null && a.getWriteMethod() != null, "a read/write methods");
        check(b.getReadMethod() != null && b.getWriteMethod() != null, "b read/write methods");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testWhiteBox();
        testAnnotationReflection();
        testRoundTrip();
        testAnnotatedCtorPreferred();
        testEmptyValue();
        testWrongPropertyName();
        testIntrospectorUnaffected();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
