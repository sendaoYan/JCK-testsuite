/*
 * @test
 * @summary javax.management.MBeanOperationInfo conformance: white-box structure + black-box behavior.
 *          Deepened: (String,Method) ctor with p1/p2 names + UNKNOWN impact,
 *          JDK-27 impact range validation (IAE), empty-signature sharing,
 *          descriptor ctor round-trip, equals matrix, hashCode = name+return,
 *          serialization.
 * @run main MBeanOperationInfoTest
 */

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanOperationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanOperationInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == MBeanFeatureInfo.class, "extends MBeanFeatureInfo");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getReturnType"), "has getReturnType");
        check(hasMethod(c, "getSignature"), "has getSignature");
        check(hasMethod(c, "getImpact"), "has getImpact");
        check(MBeanOperationInfo.INFO == 0, "INFO == 0");
        check(MBeanOperationInfo.ACTION == 1, "ACTION == 1");
        check(MBeanOperationInfo.ACTION_INFO == 2, "ACTION_INFO == 2");
        check(MBeanOperationInfo.UNKNOWN == 3, "UNKNOWN == 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanParameterInfo p = new MBeanParameterInfo("p", "int", "p desc");
        MBeanOperationInfo op = new MBeanOperationInfo("Op", "op desc",
                new MBeanParameterInfo[]{p}, "java.lang.String", MBeanOperationInfo.ACTION);
        check("Op".equals(op.getName()), "getName");
        check("op desc".equals(op.getDescription()), "getDescription");
        check("java.lang.String".equals(op.getReturnType()), "getReturnType");
        check(op.getImpact() == MBeanOperationInfo.ACTION, "getImpact");
        check(op.getSignature().length == 1 && "p".equals(op.getSignature()[0].getName()), "getSignature");

        // Method-based introspection (first arg is description; name is the method name)
        Method m = Sample.class.getMethod("echo", String.class);
        MBeanOperationInfo viaMethod = new MBeanOperationInfo("Echo", m);
        check("echo".equals(viaMethod.getName()), "method ctor name");
        check("java.lang.String".equals(viaMethod.getReturnType()), "method ctor return type");
        check(viaMethod.getSignature().length == 1, "method ctor signature length");
        check("java.lang.String".equals(viaMethod.getSignature()[0].getType()), "method ctor param type");

        // descriptor constructor
        ImmutableDescriptor d = new ImmutableDescriptor("name=Op", "description=op desc");
        MBeanOperationInfo od = new MBeanOperationInfo("Op", "op desc",
                new MBeanParameterInfo[0], "void", MBeanOperationInfo.INFO, d);
        check(od.getDescriptor() != null && "Op".equals(od.getDescriptor().getFieldValue("name")), "descriptor ctor");

        // equals
        check(op.equals(new MBeanOperationInfo("Op", "op desc",
                new MBeanParameterInfo[]{p}, "java.lang.String", MBeanOperationInfo.ACTION)), "equals identical");
        check(!op.equals(new MBeanOperationInfo("Op", "op desc",
                new MBeanParameterInfo[]{p}, "int", MBeanOperationInfo.ACTION)), "different return type not equal");
        check(!op.equals(new MBeanOperationInfo("Other", "op desc",
                new MBeanParameterInfo[]{p}, "java.lang.String", MBeanOperationInfo.ACTION)), "different name not equal");
        check(!op.equals(null), "not equal null");
        check(op.hashCode() == new MBeanOperationInfo("Op", "op desc",
                new MBeanParameterInfo[]{p}, "java.lang.String", MBeanOperationInfo.ACTION).hashCode(), "equal hashCode");

        // clone
        MBeanOperationInfo clone = (MBeanOperationInfo) op.clone();
        check(clone != op && clone.equals(op), "clone equals original");
        check(op.toString() != null, "toString non-null");
    }

    public static class Sample {
        public String echo(String s) { return s; }
        public String echo2(String s, int n) { return s + n; }
    }

    // ---- deepened: (String, Method) constructor -----------------------------

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface OpUnits {
        @DescriptorKey("units") String value();
    }
    public static class Annotated {
        @OpUnits("op-units")
        public String work(String s, int n) { return s + n; }
    }

    static void testMethodConstructorDeep() throws Exception {
        Method m = Annotated.class.getMethod("work", String.class, int.class);
        MBeanOperationInfo op = new MBeanOperationInfo("the description", m);
        check("work".equals(op.getName()), "Method ctor name from method");
        check("the description".equals(op.getDescription()), "Method ctor description from first arg");
        check(op.getImpact() == MBeanOperationInfo.UNKNOWN, "Method ctor defaults to UNKNOWN impact");
        check(op.getSignature().length == 2, "Method ctor signature length");
        check("p1".equals(op.getSignature()[0].getName()) && "p2".equals(op.getSignature()[1].getName()),
                "Method ctor params named p1/p2");
        check("java.lang.String".equals(op.getSignature()[0].getType()) && "int".equals(op.getSignature()[1].getType()),
                "Method ctor param types");
        check("".equals(op.getSignature()[0].getDescription()), "Method ctor param description empty");
        check("op-units".equals(op.getDescriptor().getFieldValue("units")),
                "Method ctor picks up DescriptorKey annotation");
    }

    // ---- deepened: JDK-27 impact range validation ---------------------------
    // (version difference: JDK-8 accepts any int impact silently)

    static void testImpactRangeValidation() {
        try {
            new MBeanOperationInfo("n", "d", null, "void", 99);
            check(false, "impact 99 should be rejected with IAE on JDK-27");
        } catch (IllegalArgumentException ex) {
            check(ex.getMessage() != null && ex.getMessage().contains("impact"),
                    "IAE message mentions impact: " + ex.getMessage());
        }
        try {
            new MBeanOperationInfo("n", "d", null, "void", -1);
            check(false, "impact -1 should be rejected with IAE on JDK-27");
        } catch (IllegalArgumentException ex) { passed++; }
        try {
            new MBeanOperationInfo("n", "d", null, "void", -1, null);
            check(false, "6-arg ctor impact -1 should be rejected with IAE");
        } catch (IllegalArgumentException ex) { passed++; }
        // boundary values remain legal
        check(new MBeanOperationInfo("n", "d", null, "void", 0).getImpact() == 0, "impact 0 legal");
        check(new MBeanOperationInfo("n", "d", null, "void", 3).getImpact() == 3, "impact 3 legal");
    }

    // ---- deepened: sharing, descriptor round-trip, equals, serialization ----

    static void testDeepenedMisc() throws Exception {
        MBeanOperationInfo empty = new MBeanOperationInfo("op", "d", new MBeanParameterInfo[0], "void", 1);
        check(empty.getSignature() == empty.getSignature(), "empty signature returns shared instance");

        MBeanOperationInfo od = new MBeanOperationInfo("n", "d", null, "void", MBeanOperationInfo.INFO,
                new ImmutableDescriptor("x=9"));
        check("9".equals(od.getDescriptor().getFieldValue("x")), "descriptor ctor round-trip");
        check(new MBeanOperationInfo("n", "d", null, "void", MBeanOperationInfo.INFO, null)
                .getDescriptor().getFieldNames().length == 0, "null descriptor -> empty");

        MBeanParameterInfo[] sig = { new MBeanParameterInfo("p", "int", "d") };
        MBeanOperationInfo base = new MBeanOperationInfo("op", "d", sig, "int", MBeanOperationInfo.ACTION);
        check(!base.equals(new MBeanOperationInfo("op", "d", sig, "long", MBeanOperationInfo.ACTION)),
                "return type participates in equals");
        check(!base.equals(new MBeanOperationInfo("op", "d", sig, "int", MBeanOperationInfo.INFO)),
                "impact participates in equals");
        check(!base.equals(new MBeanOperationInfo("op", "d", new MBeanParameterInfo[0], "int", MBeanOperationInfo.ACTION)),
                "signature participates in equals");
        check(!base.equals(new MBeanOperationInfo("op", "OTHER", sig, "int", MBeanOperationInfo.ACTION)),
                "description participates in equals");
        check(!base.equals(new MBeanOperationInfo("op", "d", sig, "int", MBeanOperationInfo.ACTION,
                new ImmutableDescriptor("k=v"))), "descriptor participates in equals");
        check(base.hashCode() == new MBeanOperationInfo("op", "d", null, "int", 0).hashCode(),
                "hashCode uses only name and return type");

        MBeanOperationInfo original = new MBeanOperationInfo("op", "d", sig, "int", MBeanOperationInfo.ACTION_INFO,
                new ImmutableDescriptor("units=ms"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MBeanOperationInfo read = (MBeanOperationInfo) ois.readObject();
        check(read.equals(original), "serialization round-trip equals");
        check(read.getImpact() == MBeanOperationInfo.ACTION_INFO, "impact survives serialization");
        check("ms".equals(read.getDescriptor().getFieldValue("units")), "descriptor survives serialization");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMethodConstructorDeep();
        testImpactRangeValidation();
        testDeepenedMisc();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
