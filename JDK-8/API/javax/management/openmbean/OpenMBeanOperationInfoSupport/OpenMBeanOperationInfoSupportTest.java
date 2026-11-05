/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanOperationInfoSupport conformance: ctor forms, return open type, impact constants, signature defensive copies, equals/hashCode, serialization.
 * @run main OpenMBeanOperationInfoSupportTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;

public class OpenMBeanOperationInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static Object roundTrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanOperationInfoSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanOperationInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanOperationInfo");
        check(Class.forName("javax.management.openmbean.OpenMBeanOperationInfo").isAssignableFrom(c), "implements/extends javax.management.openmbean.OpenMBeanOperationInfo");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getReturnOpenType"), "declares getReturnOpenType");
        check(c.getMethod("getImpact") != null, "has getImpact (inherited)");
        check(c.getMethod("getSignature") != null, "has getSignature (inherited)");
        // ---- deepened: batch g ----
        testBasicCtor();
        testImpactConstants();
        testSignatureSemantics();
        testVoidReturn();
        testEqualsHashCode();
        testDescriptorCtor();
        testToStringShape();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static OpenMBeanParameterInfo[] params() {
        return new OpenMBeanParameterInfo[] {
            new OpenMBeanParameterInfoSupport("p", "d", SimpleType.STRING),
            new OpenMBeanParameterInfoSupport("q", "d", SimpleType.INTEGER)
        };
    }

    // ---- deepened: basic (name, desc, params, OpenType, impact) form ----
    static void testBasicCtor() throws Exception {
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("op", "desc", params(), SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        check("op".equals(o.getName()), "basic name");
        check("desc".equals(o.getDescription()), "basic desc");
        check(o.getReturnOpenType() == SimpleType.INTEGER, "return open type is same singleton");
        check(o.getImpact() == MBeanOperationInfo.ACTION, "impact ACTION");
        check("java.lang.Integer".equals(o.getReturnType()), "getReturnType is openType class name");
        check(o.getSignature().length == 2, "signature length");
        check("p".equals(o.getSignature()[0].getName()), "signature first param name");
        check("q".equals(o.getSignature()[1].getName()), "signature second param name");
        check(o.getSignature()[0] instanceof OpenMBeanParameterInfo, "signature element is open parameter");
        // via interface reference
        OpenMBeanOperationInfo viaIface = o;
        check(viaIface.getReturnOpenType().equals(SimpleType.INTEGER), "return open type via interface");
        check(viaIface.getImpact() == MBeanOperationInfo.ACTION, "impact via interface");
        check(viaIface.getSignature().length == 2, "signature via interface");
        check("op".equals(viaIface.getName()) && "desc".equals(viaIface.getDescription()), "name/desc via interface");
        check("java.lang.Integer".equals(viaIface.getReturnType()), "return type via interface");
    }

    // ---- deepened: impact constants from MBeanOperationInfo ----
    static void testImpactConstants() throws Exception {
        check(MBeanOperationInfo.INFO == 0, "INFO == 0");
        check(MBeanOperationInfo.ACTION == 1, "ACTION == 1");
        check(MBeanOperationInfo.ACTION_INFO == 2, "ACTION_INFO == 2");
        check(MBeanOperationInfo.UNKNOWN == 3, "UNKNOWN == 3");
        OpenMBeanOperationInfoSupport info = new OpenMBeanOperationInfoSupport("op", "d", null, SimpleType.VOID, MBeanOperationInfo.INFO);
        check(info.getImpact() == MBeanOperationInfo.INFO, "INFO impact stored");
        OpenMBeanOperationInfoSupport unknown = new OpenMBeanOperationInfoSupport("op", "d", null, SimpleType.STRING, MBeanOperationInfo.UNKNOWN);
        check(unknown.getImpact() == 3, "UNKNOWN impact stored");
    }

    // ---- deepened: signature defensive copy semantics ----
    static void testSignatureSemantics() throws Exception {
        OpenMBeanParameterInfo[] ps = params();
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("op", "d", ps, SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        MBeanParameterInfo[] s1 = o.getSignature();
        MBeanParameterInfo[] s2 = o.getSignature();
        check(s1 != s2, "getSignature returns fresh array each call");
        check(s1.length == s2.length && s1[0].equals(s2[0]) && s1[1].equals(s2[1]), "signature contents stable");
        // mutating the returned array does not affect the stored signature
        s1[0] = new OpenMBeanParameterInfoSupport("hacked", "d", SimpleType.STRING);
        check("p".equals(o.getSignature()[0].getName()), "mutating returned array has no effect");
        // constructor copies the array (caller mutation after construction is not visible)
        ps[0] = new OpenMBeanParameterInfoSupport("mutated", "d", SimpleType.STRING);
        check("p".equals(o.getSignature()[0].getName()), "constructor copies parameter array");
        // elements themselves are shared references (shallow copy)
        OpenMBeanParameterInfo[] ps2 = params();
        OpenMBeanOperationInfoSupport o2 = new OpenMBeanOperationInfoSupport("op", "d", ps2, SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        check(o2.getSignature()[0] == ps2[0], "signature elements shared (shallow copy)");
        // null params gives empty signature
        OpenMBeanOperationInfoSupport n = new OpenMBeanOperationInfoSupport("op", "d", null, SimpleType.STRING, MBeanOperationInfo.INFO);
        check(n.getSignature() != null && n.getSignature().length == 0, "null params -> empty signature");
    }

    // ---- deepened: SimpleType.VOID return type ----
    static void testVoidReturn() throws Exception {
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("reset", "d", null, SimpleType.VOID, MBeanOperationInfo.ACTION);
        check(o.getReturnOpenType() == SimpleType.VOID, "void return open type");
        check("java.lang.Void".equals(o.getReturnType()), "void return type name");
    }

    // ---- deepened: equals/hashCode (name, signature, returnOpenType, impact; desc not included) ----
    static void testEqualsHashCode() throws Exception {
        OpenMBeanOperationInfoSupport a = new OpenMBeanOperationInfoSupport("op", "d", params(), SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        OpenMBeanOperationInfoSupport b = new OpenMBeanOperationInfoSupport("op", "d", params(), SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        check(a.equals(b), "same shape equal");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode consistent");
        check(a.equals(a), "equals reflexive");
        check(!a.equals(null), "equals null false");
        check(!a.equals("x"), "equals unrelated false");
        check(a.equals(new OpenMBeanOperationInfoSupport("op", "OTHER", params(), SimpleType.INTEGER, MBeanOperationInfo.ACTION)),
            "description not in equals");
        check(!a.equals(new OpenMBeanOperationInfoSupport("op2", "d", params(), SimpleType.INTEGER, MBeanOperationInfo.ACTION)), "name participates");
        check(!a.equals(new OpenMBeanOperationInfoSupport("op", "d", params(), SimpleType.LONG, MBeanOperationInfo.ACTION)),
            "return open type participates");
        check(!a.equals(new OpenMBeanOperationInfoSupport("op", "d", params(), SimpleType.INTEGER, MBeanOperationInfo.INFO)),
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

    // ---- deepened: Descriptor constructor form ----
    static void testDescriptorCtor() throws Exception {
        Descriptor d = new ImmutableDescriptor(new String[] {"customKey"}, new Object[] {"customValue"});
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("op", "d", params(), SimpleType.INTEGER, MBeanOperationInfo.ACTION, d);
        check("customValue".equals(o.getDescriptor().getFieldValue("customKey")), "descriptor field preserved");
        check(o.getImpact() == MBeanOperationInfo.ACTION, "descriptor form impact");
        check(o.getReturnOpenType() == SimpleType.INTEGER, "descriptor form return type");
    }

    // ---- deepened: toString shape ----
    static void testToStringShape() throws Exception {
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("op", "d", params(), SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        String s = o.toString();
        check(s.startsWith("javax.management.openmbean.OpenMBeanOperationInfoSupport("), "toString prefix");
        check(s.contains("name=op"), "toString name");
        check(s.contains("impact=1"), "toString impact");
        check(s.contains("return=javax.management.openmbean.SimpleType(name=java.lang.Integer)"), "toString return open type");
        check(s.contains("OpenMBeanParameterInfoSupport(name=p"), "toString signature element");
    }

    // ---- deepened: serialization round-trip ----
    static void testSerialization() throws Exception {
        OpenMBeanOperationInfoSupport o = new OpenMBeanOperationInfoSupport("op", "d", params(), SimpleType.INTEGER, MBeanOperationInfo.ACTION);
        OpenMBeanOperationInfoSupport q = (OpenMBeanOperationInfoSupport) roundTrip(o);
        check(o.equals(q), "round-trip equals");
        check(q.hashCode() == o.hashCode(), "round-trip hashCode");
        check(q.getReturnOpenType() == SimpleType.INTEGER, "round-trip return open type singleton");
        check(q.getImpact() == MBeanOperationInfo.ACTION, "round-trip impact");
        check(q.getSignature().length == 2, "round-trip signature length");
        check("p".equals(q.getSignature()[0].getName()), "round-trip signature contents");
        check(q.getSignature()[0] instanceof OpenMBeanParameterInfo, "round-trip signature element is open parameter");
        check(ObjectStreamClass.lookup(OpenMBeanOperationInfoSupport.class).getSerialVersionUID()
            == 4996859732565369366L, "serialVersionUID");
    }
}
