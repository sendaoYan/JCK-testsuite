/*
 * @test
 * @summary javax.management.DescriptorKey conformance: white-box structure + annotation behavior.
 *          Deepened: Target exactly METHOD, Documented, and the REAL loop - a
 *          custom CacheSpec annotation whose elements carry DescriptorKey on a
 *          Standard MBean interface lands in the server-side MBeanInfo
 *          attribute/operation/parameter descriptors after registerMBean
 *          (String, int to Integer, Class to getName, enum to name, String[]
 *          preserved), plus the getter/setter consistency rule.
 * @run main DescriptorKeyTest
 */

import java.lang.reflect.*;
import java.lang.annotation.*;
import java.util.*;
import javax.management.*;

public class DescriptorKeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.DescriptorKey");
        check(c.isAnnotation(), "is annotation");
        check(java.lang.annotation.Annotation.class.isAssignableFrom(c), "extends Annotation");
        check(hasMethod(c, "value"), "has value()");
        Method value = c.getMethod("value");
        check(value.getReturnType() == String.class, "value() returns String");
        check(value.getDefaultValue() == null, "value() has no default");
        check(RetentionPolicy.RUNTIME.equals(c.getAnnotation(Retention.class).value()), "runtime retention");
        check(c.isAnnotationPresent(Target.class), "has @Target");
        Target t = c.getAnnotation(Target.class);
        check(t.value().length == 1 && t.value()[0] == ElementType.METHOD,
                "@Target is exactly {ElementType.METHOD}");
        check(c.isAnnotationPresent(Documented.class), "@Documented present");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // The annotation is usable on methods and readable via reflection.
        Method m = Sample.class.getMethod("describe");
        DescriptorKey key = m.getAnnotation(DescriptorKey.class);
        check(key != null, "annotation present on method");
        check("MyKey".equals(key.value()), "annotation value");
    }

    public static class Sample {
        @DescriptorKey("MyKey")
        public String describe() { return ""; }
    }

    // ---- deepened: annotation carriers + the MBean under introspection ----

    public enum Mode { FAST, SLOW }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface CacheSpec {
        @DescriptorKey("units") String units() default "";
        @DescriptorKey("defVal") int defVal() default 5;
        @DescriptorKey("cls") Class<?> cls() default Thread.class;
        @DescriptorKey("mode") Mode mode() default Mode.FAST;
        @DescriptorKey("tags") String[] tags() default {"a", "b"};
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface OpUnits {
        @DescriptorKey("units") String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface ParamUnits {
        @DescriptorKey("units") String value();
    }

    public interface CounterMBean {
        @CacheSpec(units = "bytes")
        int getCount();
        @OpUnits("op-units")
        int add(@ParamUnits("param-units") int p1, int p2);
    }

    public static class Counter implements CounterMBean {
        public int getCount() { return 42; }
        public int add(int p1, int p2) { return p1 + p2; }
    }

    public interface BrokenMBean {
        @CacheSpec(units = "g")
        String getL();
        @CacheSpec(units = "s")
        void setL(String v);
    }

    public static class Broken implements BrokenMBean {
        public String getL() { return null; }
        public void setL(String v) { }
    }

    // ---- deepened: THE loop annotation -> @DescriptorKey -> info descriptor ----
    static void testStandardMBeanDescriptorClosure() throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.b:type=Counter");
        mbs.registerMBean(new Counter(), on);
        MBeanInfo info = mbs.getMBeanInfo(on);

        MBeanAttributeInfo attr = null;
        for (MBeanAttributeInfo a : info.getAttributes())
            if (a.getName().equals("Count")) attr = a;
        check(attr != null, "attribute Count present in server-side MBeanInfo");
        Descriptor d = attr.getDescriptor();
        check("bytes".equals(d.getFieldValue("units")),
                "attribute descriptor units == bytes (explicit value)");
        check(Integer.valueOf(5).equals(d.getFieldValue("defVal")),
                "attribute descriptor defVal: int element boxed to Integer 5");
        check("java.lang.Thread".equals(d.getFieldValue("cls")),
                "attribute descriptor cls: Class element stored as getName()");
        check("FAST".equals(d.getFieldValue("mode")),
                "attribute descriptor mode: enum element stored as name()");
        Object tags = d.getFieldValue("tags");
        check(tags instanceof String[] && Arrays.equals(new String[]{"a", "b"}, (String[]) tags),
                "attribute descriptor tags: String[] preserved");

        MBeanOperationInfo op = null;
        for (MBeanOperationInfo o : info.getOperations())
            if (o.getName().equals("add")) op = o;
        check(op != null, "operation add present in server-side MBeanInfo");
        check("op-units".equals(op.getDescriptor().getFieldValue("units")),
                "operation descriptor units == op-units");
        check("param-units".equals(op.getSignature()[0].getDescriptor().getFieldValue("units")),
                "parameter descriptor units == param-units");
        check(op.getSignature()[1].getDescriptor().getFieldValue("units") == null,
                "unannotated parameter has no units field");
        Object r = mbs.invoke(on, "add", new Object[]{3, 4}, new String[]{"int", "int"});
        check(Integer.valueOf(7).equals(r), "invoke add(3,4) == 7 through the same server");
    }

    // ---- deepened: consistency rule between getter and setter ----
    static void testConflictingGetterSetterRejected() throws Exception {
        try {
            MBeanServer mbs = MBeanServerFactory.newMBeanServer();
            ObjectName on = new ObjectName("jck.b:type=Broken");
            mbs.registerMBean(new Broken(), on);
            mbs.getMBeanInfo(on);
            check(false, "conflicting @DescriptorKey getter/setter values must be rejected");
        } catch (NotCompliantMBeanException ex) {
            Throwable cause = ex.getCause();
            check(cause instanceof IllegalArgumentException
                    && cause.getMessage() != null
                    && cause.getMessage().contains("Inconsistent values for descriptor field units"),
                    "NotCompliantMBeanException caused by IAE: " + cause);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testStandardMBeanDescriptorClosure();
        testConflictingGetterSetterRejected();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
