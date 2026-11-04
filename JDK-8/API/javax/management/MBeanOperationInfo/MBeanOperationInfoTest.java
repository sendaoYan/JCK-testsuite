/*
 * @test
 * @summary javax.management.MBeanOperationInfo conformance: full contract
 *          coverage. return type/signature/impact, impact constants, null
 *          signature -> empty, defensive signature getter (empty shared),
 *          (String,Method) constructor with p1/p2 synthetic parameter names
 *          and UNKNOWN impact, JDK-8 impact is NOT range-validated (any int
 *          retained), descriptor ctor round-trip + null -> empty,
 *          equals participation matrix, hashCode = name+returnType only,
 *          clone/equals, serialization round-trip.
 * @run main MBeanOperationInfoTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import javax.management.ImmutableDescriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

public class MBeanOperationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanParameterInfo p = new MBeanParameterInfo("x", "int", "param");
        MBeanOperationInfo o = new MBeanOperationInfo("op", "desc", new MBeanParameterInfo[]{p}, "int", MBeanOperationInfo.ACTION);
        check("op".equals(o.getName()), "name");
        check("int".equals(o.getReturnType()), "return type");
        check(o.getSignature().length == 1 && "x".equals(o.getSignature()[0].getName()), "signature");
        check(o.getImpact() == MBeanOperationInfo.ACTION, "impact");
        check(MBeanOperationInfo.INFO != MBeanOperationInfo.UNKNOWN, "impact constants");
    }

    static void testImpactConstants() {
        check(MBeanOperationInfo.INFO == 0, "INFO == 0");
        check(MBeanOperationInfo.ACTION == 1, "ACTION == 1");
        check(MBeanOperationInfo.ACTION_INFO == 2, "ACTION_INFO == 2");
        check(MBeanOperationInfo.UNKNOWN == 3, "UNKNOWN == 3");
    }

    static void testNullSignatureAndDefensiveGetter() {
        MBeanOperationInfo o = new MBeanOperationInfo("op", "d", null, "void", MBeanOperationInfo.INFO);
        check(o.getSignature().length == 0, "null signature -> empty");
        MBeanParameterInfo[] sig = o.getSignature();
        sig = new MBeanParameterInfo[]{new MBeanParameterInfo("a", "int", "d")};
        check(o.getSignature().length == 0, "getSignature defensive");

        MBeanOperationInfo withParam = new MBeanOperationInfo("op2", "d",
            new MBeanParameterInfo[]{new MBeanParameterInfo("p", "int", "d")}, "int", MBeanOperationInfo.UNKNOWN);
        withParam.getSignature()[0] = null;
        check(withParam.getSignature()[0] != null, "getSignature defensive copy");
    }

    static void testCloneEquals() {
        MBeanOperationInfo o = new MBeanOperationInfo("op", "d",
            new MBeanParameterInfo[]{new MBeanParameterInfo("p", "int", "d")}, "int", MBeanOperationInfo.ACTION);
        MBeanOperationInfo c = (MBeanOperationInfo) o.clone();
        check(o.equals(c) && o.hashCode() == c.hashCode(), "clone equals/hashCode");
    }

    // ---- deepened: (String, Method) constructor -----------------------------

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface OpUnits {
        @javax.management.DescriptorKey("units")
        String value();
    }

    public static class SampleOps {
        @OpUnits("op-units")
        public String echo(String s, int n) { return s + n; }
    }

    static void testMethodConstructor() throws Exception {
        Method m = SampleOps.class.getMethod("echo", String.class, int.class);
        MBeanOperationInfo op = new MBeanOperationInfo("the description", m);
        check("echo".equals(op.getName()), "Method ctor name from method");
        check("the description".equals(op.getDescription()), "Method ctor description from first arg");
        check("java.lang.String".equals(op.getReturnType()), "Method ctor return type");
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

    // ---- deepened: JDK-8 impact is NOT range-validated ----------------------
    // (version difference: JDK-27+ rejects out-of-range impact with IAE)

    static void testImpactNotRangeCheckedOnJdk8() {
        MBeanOperationInfo big = new MBeanOperationInfo("op", "d", null, "void", 99);
        check(big.getImpact() == 99, "JDK-8 retains out-of-range impact 99");
        MBeanOperationInfo neg = new MBeanOperationInfo("op", "d", null, "void", -1);
        check(neg.getImpact() == -1, "JDK-8 retains negative impact -1");
        MBeanOperationInfo big6 = new MBeanOperationInfo("op", "d", null, "void", 99, null);
        check(big6.getImpact() == 99, "JDK-8 6-arg ctor retains out-of-range impact");
    }

    // ---- deepened: empty signature array is shared --------------------------

    static void testEmptySignatureShared() {
        MBeanOperationInfo o = new MBeanOperationInfo("op", "d", new MBeanParameterInfo[0], "void", 1);
        check(o.getSignature() == o.getSignature(), "empty signature returns shared instance");
    }

    // ---- deepened: descriptor constructor round-trip ------------------------

    static void testDescriptorCtorRoundTrip() {
        ImmutableDescriptor d = new ImmutableDescriptor("x=9");
        MBeanOperationInfo o = new MBeanOperationInfo("n", "d", null, "void", MBeanOperationInfo.INFO, d);
        check("9".equals(o.getDescriptor().getFieldValue("x")), "descriptor ctor round-trip");
        check(new MBeanOperationInfo("n", "d", null, "void", MBeanOperationInfo.INFO, null)
                .getDescriptor().getFieldNames().length == 0, "null descriptor -> empty");
        check(new MBeanOperationInfo("n", "d", null, "void", MBeanOperationInfo.INFO)
                .getDescriptor().getFieldNames().length == 0, "5-arg ctor descriptor empty");
    }

    // ---- deepened: equals participation + hashCode name/return only ---------

    static void testEqualsAndHashCode() {
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
        check(!base.equals(null) && !base.equals("op"), "equals null/foreign false");
    }

    // ---- deepened: serialization round-trip ---------------------------------

    static void testSerialization() throws Exception {
        MBeanOperationInfo original = new MBeanOperationInfo("op", "d",
                new MBeanParameterInfo[]{new MBeanParameterInfo("p", "int", "pd")},
                "int", MBeanOperationInfo.ACTION_INFO, new ImmutableDescriptor("units=ms"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MBeanOperationInfo read = (MBeanOperationInfo) ois.readObject();
        check(read.equals(original), "serialization round-trip equals");
        check(read.getImpact() == MBeanOperationInfo.ACTION_INFO, "impact survives serialization");
        check("ms".equals(read.getDescriptor().getFieldValue("units")), "descriptor survives serialization");
        check(read.getSignature()[0].getName().equals("p"), "signature survives serialization");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testImpactConstants();
        testNullSignatureAndDefensiveGetter();
        testCloneEquals();
        testMethodConstructor();
        testImpactNotRangeCheckedOnJdk8();
        testEmptySignatureShared();
        testDescriptorCtorRoundTrip();
        testEqualsAndHashCode();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
