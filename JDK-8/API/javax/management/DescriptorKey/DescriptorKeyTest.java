/*
 * @test
 * @summary javax.management.DescriptorKey conformance: annotation reflection.
 *          Deepened: meta-facts (Target exactly METHOD, Documented, RUNTIME
 *          retention, value() String without default) and the REAL loop - a
 *          custom CacheSpec annotation whose elements carry DescriptorKey is
 *          placed on a Standard MBean interface; after registerMBean the
 *          server-side MBeanInfo exposes those elements in the attribute /
 *          operation / parameter descriptors (String, int to Integer,
 *          Class to getName, enum to name, String[] preserved), the consistency
 *          rule (conflicting getter/setter values -> IllegalArgumentException)
 *          and invoke still work through the same server.
 * @run main DescriptorKeyTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.management.Descriptor;
import javax.management.DescriptorKey;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

public class DescriptorKeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- deepened annotation carriers + the MBean under introspection ----

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

    // getter and setter annotate the same descriptor field with DIFFERENT values
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

    // ---- deepened: meta-facts of @DescriptorKey itself ----
    static void testAnnotationFacts() throws Exception {
        Class<?> c = Class.forName("javax.management.DescriptorKey");
        Target t = c.getAnnotation(Target.class);
        check(t != null && t.value().length == 1 && t.value()[0] == ElementType.METHOD,
                "@Target is exactly {ElementType.METHOD}");
        check(c.isAnnotationPresent(Documented.class), "@Documented present");
        check(RetentionPolicy.RUNTIME.equals(c.getAnnotation(Retention.class).value()),
                "RUNTIME retention");
        Method v = c.getMethod("value");
        check(v.getReturnType() == String.class, "value() returns String");
        check(v.getDefaultValue() == null, "value() has no default");
        check(Class.forName("java.lang.annotation.Annotation").isAssignableFrom(c),
                "annotation type extends Annotation");
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
                "attribute descriptor field units == bytes (explicit value)");
        check(Integer.valueOf(5).equals(d.getFieldValue("defVal")),
                "attribute descriptor defVal: int element boxed to Integer 5");
        check("java.lang.Thread".equals(d.getFieldValue("cls")),
                "attribute descriptor cls: Class element stored as getName()");
        check("FAST".equals(d.getFieldValue("mode")),
                "attribute descriptor mode: enum element stored as name()");
        Object tags = d.getFieldValue("tags");
        check(tags instanceof String[] && Arrays.equals(new String[]{"a", "b"}, (String[]) tags),
                "attribute descriptor tags: String[] preserved");
        check(!attr.getDescriptor().getFieldNames().toString().isEmpty(), "descriptor non-empty");

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
        // the introspected metadata coexists with normal invocation on the same server
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
        } catch (javax.management.NotCompliantMBeanException ex) {
            Throwable cause = ex.getCause();
            check(cause instanceof IllegalArgumentException
                    && cause.getMessage() != null
                    && cause.getMessage().contains("Inconsistent values for descriptor field units"),
                    "NotCompliantMBeanException caused by IAE: " + cause);
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.DescriptorKey");
        check(c.isAnnotation(), "annotation");
        check(c.isInterface(), "annotation is an interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Annotation.class.isAssignableFrom(c), "implements/extends java.lang.annotation.Annotation");
        check(hasMethod(c, "value"), "declares value");

        testAnnotationFacts();
        testStandardMBeanDescriptorClosure();
        testConflictingGetterSetterRejected();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
