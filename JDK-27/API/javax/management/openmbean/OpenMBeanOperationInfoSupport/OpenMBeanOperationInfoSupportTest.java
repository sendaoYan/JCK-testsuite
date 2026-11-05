/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanOperationInfoSupport conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanOperationInfoSupportTest
 */

import java.lang.reflect.Modifier;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class OpenMBeanOperationInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanOperationInfoSupport");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(MBeanOperationInfo.class.isAssignableFrom(c), "extends MBeanOperationInfo");
        check(OpenMBeanOperationInfo.class.isAssignableFrom(c), "implements OpenMBeanOperationInfo");
        check(c.getMethod("getReturnOpenType") != null, "has getReturnOpenType");
        check(c.getMethod("getImpact") != null, "has getImpact");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        OpenMBeanParameterInfoSupport[] params = new OpenMBeanParameterInfoSupport[]{
            new OpenMBeanParameterInfoSupport("x", "x desc", SimpleType.INTEGER)
        };
        OpenMBeanOperationInfoSupport oi = new OpenMBeanOperationInfoSupport(
            "add", "add desc", params, SimpleType.INTEGER, MBeanOperationInfo.INFO);
        check(oi.getName().equals("add"), "name");
        check(oi.getDescription().equals("add desc"), "description");
        check(oi.getReturnOpenType() == SimpleType.INTEGER, "return open type");
        check(oi.getReturnType().equals("java.lang.Integer"), "return type class name");
        check(oi.getImpact() == MBeanOperationInfo.INFO, "impact INFO");
        MBeanParameterInfo[] sig = oi.getSignature();
        check(sig.length == 1 && sig[0].getName().equals("x"), "signature");
        check(((OpenMBeanParameterInfoSupport) sig[0]).getOpenType() == SimpleType.INTEGER, "param open type");

        // different impact constants
        check(new OpenMBeanOperationInfoSupport("a", "d", new OpenMBeanParameterInfoSupport[0],
            SimpleType.VOID, MBeanOperationInfo.ACTION).getImpact() == MBeanOperationInfo.ACTION, "impact ACTION");
        check(new OpenMBeanOperationInfoSupport("a", "d", new OpenMBeanParameterInfoSupport[0],
            SimpleType.VOID, MBeanOperationInfo.ACTION_INFO).getImpact() == MBeanOperationInfo.ACTION_INFO, "impact ACTION_INFO");
        check(new OpenMBeanOperationInfoSupport("a", "d", new OpenMBeanParameterInfoSupport[0],
            SimpleType.VOID, MBeanOperationInfo.UNKNOWN).getImpact() == MBeanOperationInfo.UNKNOWN, "impact UNKNOWN");

        // equals / hashCode
        OpenMBeanOperationInfoSupport oi2 = new OpenMBeanOperationInfoSupport(
            "add", "add desc", params, SimpleType.INTEGER, MBeanOperationInfo.INFO);
        check(oi.equals(oi2), "equals");
        check(oi.hashCode() == oi2.hashCode(), "hashCode equal");
        check(!oi.equals(null), "equals null false");
        check(!oi.equals("x"), "equals non-info false");
        check(!oi.equals(new OpenMBeanOperationInfoSupport("sub", "add desc", params, SimpleType.INTEGER, MBeanOperationInfo.INFO)), "different name");
        check(!oi.equals(new OpenMBeanOperationInfoSupport("add", "add desc", params, SimpleType.LONG, MBeanOperationInfo.INFO)), "different return type");

        check(oi.toString().startsWith("javax.management.openmbean.OpenMBeanOperationInfoSupport("), "toString prefix");
        check(oi.toString().contains("name=add"), "toString name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- deepened: batch g ----
        testBasicCtorG();
        testImpactConstantsG();
        testSignatureSemanticsG();
        testVoidReturnG();
        testEqualsHashCodeG();
        testDescriptorCtorG();
        testToStringShapeG();
        testSerializationG();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepened (batch g): full contract coverage =================

    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static OpenMBeanParameterInfo[] paramsG() {
        return new OpenMBeanParameterInfo[] {
            new OpenMBeanParameterInfoSupport("p", "d", SimpleType.STRING),
            new OpenMBeanParameterInfoSupport("q", "d", SimpleType.INTEGER)
        };
    }

    static void testBasicCtorG() throws Exception {
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("op", "desc", paramsG(), SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        check("op".equals(o.getName()), "basic name");
        check("desc".equals(o.getDescription()), "basic desc");
        check(o.getReturnOpenType() == SimpleType.INTEGER, "return open type is same singleton");
        check(o.getImpact() == MBeanOperationInfo.ACTION, "impact ACTION");
        check("java.lang.Integer".equals(o.getReturnType()), "getReturnType is openType class name");
        check(o.getSignature().length == 2, "signature length");
        check("p".equals(o.getSignature()[0].getName()), "signature first param name");
        check("q".equals(o.getSignature()[1].getName()), "signature second param name");
        check(o.getSignature()[0] instanceof OpenMBeanParameterInfo, "signature element is open parameter");
        OpenMBeanOperationInfo viaIface = o;
        check(viaIface.getReturnOpenType().equals(SimpleType.INTEGER), "return open type via interface");
        check(viaIface.getImpact() == MBeanOperationInfo.ACTION, "impact via interface");
        check(viaIface.getSignature().length == 2, "signature via interface");
        check("op".equals(viaIface.getName()) && "desc".equals(viaIface.getDescription()), "name/desc via interface");
        check("java.lang.Integer".equals(viaIface.getReturnType()), "return type via interface");
    }

    static void testImpactConstantsG() throws Exception {
        check(MBeanOperationInfo.INFO == 0, "INFO == 0");
        check(MBeanOperationInfo.ACTION == 1, "ACTION == 1");
        check(MBeanOperationInfo.ACTION_INFO == 2, "ACTION_INFO == 2");
        check(MBeanOperationInfo.UNKNOWN == 3, "UNKNOWN == 3");
        OpenMBeanOperationInfoSupport info = new OpenMBeanOperationInfoSupport("op", "d", null, SimpleType.VOID, MBeanOperationInfo.INFO);
        check(info.getImpact() == MBeanOperationInfo.INFO, "INFO impact stored");
        OpenMBeanOperationInfoSupport unknown = new OpenMBeanOperationInfoSupport("op", "d", null, SimpleType.STRING, MBeanOperationInfo.UNKNOWN);
        check(unknown.getImpact() == 3, "UNKNOWN impact stored");
    }

    static void testSignatureSemanticsG() throws Exception {
        OpenMBeanParameterInfo[] ps = paramsG();
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("op", "d", ps, SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        MBeanParameterInfo[] s1 = o.getSignature();
        MBeanParameterInfo[] s2 = o.getSignature();
        check(s1 != s2, "getSignature returns fresh array each call");
        check(s1.length == s2.length && s1[0].equals(s2[0]) && s1[1].equals(s2[1]), "signature contents stable");
        s1[0] = new OpenMBeanParameterInfoSupport("hacked", "d", SimpleType.STRING);
        check("p".equals(o.getSignature()[0].getName()), "mutating returned array has no effect");
        ps[0] = new OpenMBeanParameterInfoSupport("mutated", "d", SimpleType.STRING);
        check("p".equals(o.getSignature()[0].getName()), "constructor copies parameter array");
        OpenMBeanParameterInfo[] ps2 = paramsG();
        OpenMBeanOperationInfoSupport o2 = new OpenMBeanOperationInfoSupport("op", "d", ps2, SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        check(o2.getSignature()[0] == ps2[0], "signature elements shared (shallow copy)");
        OpenMBeanOperationInfoSupport n = new OpenMBeanOperationInfoSupport("op", "d", null, SimpleType.STRING, MBeanOperationInfo.INFO);
        check(n.getSignature() != null && n.getSignature().length == 0, "null params -> empty signature");
    }

    static void testVoidReturnG() throws Exception {
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("reset", "d", null, SimpleType.VOID, MBeanOperationInfo.ACTION);
        check(o.getReturnOpenType() == SimpleType.VOID, "void return open type");
        check("java.lang.Void".equals(o.getReturnType()), "void return type name");
    }

    static void testEqualsHashCodeG() throws Exception {
        OpenMBeanOperationInfoSupport a = new OpenMBeanOperationInfoSupport("op", "d", paramsG(), SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        OpenMBeanOperationInfoSupport b = new OpenMBeanOperationInfoSupport("op", "d", paramsG(), SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        check(a.equals(b), "same shape equal");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode consistent");
        check(a.equals(a), "equals reflexive");
        check(!a.equals(null), "equals null false");
        check(!a.equals("x"), "equals unrelated false");
        check(a.equals(new OpenMBeanOperationInfoSupport("op", "OTHER", paramsG(), SimpleType.INTEGER, MBeanOperationInfo.ACTION)),
            "description not in equals");
        check(!a.equals(new OpenMBeanOperationInfoSupport("op2", "d", paramsG(), SimpleType.INTEGER, MBeanOperationInfo.ACTION)), "name participates");
        check(!a.equals(new OpenMBeanOperationInfoSupport("op", "d", paramsG(), SimpleType.LONG, MBeanOperationInfo.ACTION)),
            "return open type participates");
        check(!a.equals(new OpenMBeanOperationInfoSupport("op", "d", paramsG(), SimpleType.INTEGER, MBeanOperationInfo.INFO)),
            "impact participates");
        OpenMBeanParameterInfo[] otherSig = {
            new OpenMBeanParameterInfoSupport("p", "d", SimpleType.STRING),
            new OpenMBeanParameterInfoSupport("q", "d", SimpleType.LONG)
        };
        check(!a.equals(new OpenMBeanOperationInfoSupport("op", "d", otherSig, SimpleType.INTEGER, MBeanOperationInfo.ACTION)),
            "signature participates");
        OpenMBeanParameterInfo[] reorded = {
            new OpenMBeanParameterInfoSupport("q", "d", SimpleType.INTEGER),
            new OpenMBeanParameterInfoSupport("p", "d", SimpleType.STRING)
        };
        check(!a.equals(new OpenMBeanOperationInfoSupport("op", "d", reorded, SimpleType.INTEGER, MBeanOperationInfo.ACTION)),
            "signature order matters (Arrays.equals)");
    }

    static void testDescriptorCtorG() throws Exception {
        javax.management.Descriptor d = new javax.management.ImmutableDescriptor(new String[] {"customKey"}, new Object[] {"customValue"});
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("op", "d", paramsG(), SimpleType.INTEGER, MBeanOperationInfo.ACTION, d);
        check("customValue".equals(o.getDescriptor().getFieldValue("customKey")), "descriptor field preserved");
        check(o.getImpact() == MBeanOperationInfo.ACTION, "descriptor form impact");
        check(o.getReturnOpenType() == SimpleType.INTEGER, "descriptor form return type");
    }

    static void testToStringShapeG() throws Exception {
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("op", "d", paramsG(), SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        String s = o.toString();
        check(s.startsWith("javax.management.openmbean.OpenMBeanOperationInfoSupport("), "toString prefix");
        check(s.contains("name=op"), "toString name");
        check(s.contains("impact=1"), "toString impact");
        check(s.contains("return=javax.management.openmbean.SimpleType(name=java.lang.Integer)"), "toString return open type");
        check(s.contains("OpenMBeanParameterInfoSupport(name=p"), "toString signature element");
    }

    static void testSerializationG() throws Exception {
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("op", "d", paramsG(), SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        OpenMBeanOperationInfoSupport q = (OpenMBeanOperationInfoSupport) roundTrip(o);
        check(o.equals(q), "round-trip equals");
        check(q.hashCode() == o.hashCode(), "round-trip hashCode");
        check(q.getReturnOpenType() == SimpleType.INTEGER, "round-trip return open type singleton");
        check(q.getImpact() == MBeanOperationInfo.ACTION, "round-trip impact");
        check(q.getSignature().length == 2, "round-trip signature length");
        check("p".equals(q.getSignature()[0].getName()), "round-trip signature contents");
        check(q.getSignature()[0] instanceof OpenMBeanParameterInfo, "round-trip signature element is open parameter");
        check(java.io.ObjectStreamClass.lookup(OpenMBeanOperationInfoSupport.class).getSerialVersionUID()
            == 4996859732565369366L, "serialVersionUID");
    }
}
