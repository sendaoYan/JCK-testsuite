/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanConstructorInfoSupport conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanConstructorInfoSupportTest
 */

import java.lang.reflect.Modifier;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanParameterInfo;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;

public class OpenMBeanConstructorInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanConstructorInfoSupport");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(MBeanConstructorInfo.class.isAssignableFrom(c), "extends MBeanConstructorInfo");
        check(OpenMBeanConstructorInfo.class.isAssignableFrom(c), "implements OpenMBeanConstructorInfo");
        check(c.getMethod("getSignature") != null, "has getSignature");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        OpenMBeanParameterInfoSupport[] params = new OpenMBeanParameterInfoSupport[]{
            new OpenMBeanParameterInfoSupport("p", "param desc", SimpleType.STRING)
        };
        OpenMBeanConstructorInfoSupport ci = new OpenMBeanConstructorInfoSupport("Test", "ctor desc", params);
        check(ci.getName().equals("Test"), "name");
        check(ci.getDescription().equals("ctor desc"), "description");
        MBeanParameterInfo[] sig = ci.getSignature();
        check(sig.length == 1, "signature length");
        check(sig[0].getName().equals("p"), "param name");
        check(sig[0] instanceof OpenMBeanParameterInfoSupport, "param is OpenMBeanParameterInfoSupport");
        check(((OpenMBeanParameterInfoSupport) sig[0]).getOpenType() == SimpleType.STRING, "param openType");

        // empty signature
        OpenMBeanConstructorInfoSupport empty = new OpenMBeanConstructorInfoSupport("Empty", "d", new OpenMBeanParameterInfoSupport[0]);
        check(empty.getSignature().length == 0, "empty signature");

        // equals / hashCode
        OpenMBeanConstructorInfoSupport ci2 = new OpenMBeanConstructorInfoSupport("Test", "ctor desc", params);
        check(ci.equals(ci2), "equals");
        check(ci.hashCode() == ci2.hashCode(), "hashCode equal");
        check(!ci.equals(null), "equals null false");
        check(!ci.equals("x"), "equals non-info false");
        check(!ci.equals(new OpenMBeanConstructorInfoSupport("Other", "ctor desc", params)), "different name");
        check(!ci.equals(empty), "different signature");

        check(ci.toString().startsWith("javax.management.openmbean.OpenMBeanConstructorInfoSupport("), "toString prefix");
        check(ci.toString().contains("name=Test"), "toString name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- deepened: batch g ----
        testBasicCtorG();
        testSignatureSemanticsG();
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
        OpenMBeanConstructorInfoSupport k = new OpenMBeanConstructorInfoSupport("Ctor", "desc", paramsG());
        check("Ctor".equals(k.getName()), "basic name");
        check("desc".equals(k.getDescription()), "basic desc");
        check(k.getSignature().length == 2, "signature length");
        check("p".equals(k.getSignature()[0].getName()), "signature first param");
        check("q".equals(k.getSignature()[1].getName()), "signature second param");
        check(k.getSignature()[0] instanceof OpenMBeanParameterInfo, "signature element is open parameter");
        OpenMBeanConstructorInfo viaIface = k;
        check("Ctor".equals(viaIface.getName()), "name via interface");
        check("desc".equals(viaIface.getDescription()), "desc via interface");
        check(viaIface.getSignature().length == 2, "signature via interface");
    }

    static void testSignatureSemanticsG() throws Exception {
        OpenMBeanParameterInfo[] ps = paramsG();
        OpenMBeanConstructorInfoSupport k = new OpenMBeanConstructorInfoSupport("Ctor", "d", ps);
        MBeanParameterInfo[] s1 = k.getSignature();
        MBeanParameterInfo[] s2 = k.getSignature();
        check(s1 != s2, "getSignature returns fresh array each call");
        check(s1.length == s2.length && s1[0].equals(s2[0]) && s1[1].equals(s2[1]), "signature contents stable");
        s1[0] = new OpenMBeanParameterInfoSupport("hacked", "d", SimpleType.STRING);
        check("p".equals(k.getSignature()[0].getName()), "mutating returned array has no effect");
        ps[0] = new OpenMBeanParameterInfoSupport("mutated", "d", SimpleType.STRING);
        check("p".equals(k.getSignature()[0].getName()), "constructor copies parameter array");
        OpenMBeanParameterInfo[] ps2 = paramsG();
        OpenMBeanConstructorInfoSupport k2 = new OpenMBeanConstructorInfoSupport("Ctor", "d", ps2);
        check(k2.getSignature()[0] == ps2[0], "signature elements shared (shallow copy)");
        OpenMBeanConstructorInfoSupport n = new OpenMBeanConstructorInfoSupport("Ctor", "d", null);
        check(n.getSignature() != null && n.getSignature().length == 0, "null params -> empty signature");
    }

    static void testEqualsHashCodeG() throws Exception {
        OpenMBeanConstructorInfoSupport a = new OpenMBeanConstructorInfoSupport("C", "d", paramsG());
        OpenMBeanConstructorInfoSupport b = new OpenMBeanConstructorInfoSupport("C", "d", paramsG());
        check(a.equals(b), "same shape equal");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode consistent");
        check(a.equals(a), "equals reflexive");
        check(!a.equals(null), "equals null false");
        check(!a.equals("x"), "equals unrelated false");
        check(a.equals(new OpenMBeanConstructorInfoSupport("C", "OTHER", paramsG())), "description not in equals");
        check(!a.equals(new OpenMBeanConstructorInfoSupport("D", "d", paramsG())), "name participates");
        OpenMBeanParameterInfo[] otherSig = {
            new OpenMBeanParameterInfoSupport("p", "d", SimpleType.STRING),
            new OpenMBeanParameterInfoSupport("q", "d", SimpleType.LONG)
        };
        check(!a.equals(new OpenMBeanConstructorInfoSupport("C", "d", otherSig)), "signature participates");
        OpenMBeanConstructorInfoSupport z1 = new OpenMBeanConstructorInfoSupport("C", "d", null);
        OpenMBeanConstructorInfoSupport z2 = new OpenMBeanConstructorInfoSupport("C", "d", null);
        check(z1.equals(z2), "both empty signatures equal");
        check(!a.equals(z1), "two params vs zero params not equal");
        OpenMBeanConstructorInfoSupport one = new OpenMBeanConstructorInfoSupport("C", "d",
            new OpenMBeanParameterInfo[] { paramsG()[0] });
        check(!one.equals(a), "one param vs two params not equal");
    }

    static void testDescriptorCtorG() throws Exception {
        javax.management.Descriptor d = new javax.management.ImmutableDescriptor(new String[] {"customKey"}, new Object[] {"customValue"});
        OpenMBeanConstructorInfoSupport k = new OpenMBeanConstructorInfoSupport("C", "d", paramsG(), d);
        check("customValue".equals(k.getDescriptor().getFieldValue("customKey")), "descriptor field preserved");
        check(k.getSignature().length == 2, "descriptor form signature");
    }

    static void testToStringShapeG() throws Exception {
        OpenMBeanConstructorInfoSupport k = new OpenMBeanConstructorInfoSupport("C", "d", paramsG());
        String s = k.toString();
        check(s.startsWith("javax.management.openmbean.OpenMBeanConstructorInfoSupport("), "toString prefix");
        check(s.contains("name=C"), "toString name");
        check(s.contains("OpenMBeanParameterInfoSupport(name=p"), "toString signature element");
    }

    static void testSerializationG() throws Exception {
        OpenMBeanConstructorInfoSupport k = new OpenMBeanConstructorInfoSupport("C", "d", paramsG());
        OpenMBeanConstructorInfoSupport q = (OpenMBeanConstructorInfoSupport) roundTrip(k);
        check(k.equals(q), "round-trip equals");
        check(q.hashCode() == k.hashCode(), "round-trip hashCode");
        check(q.getSignature().length == 2, "round-trip signature length");
        check("q".equals(q.getSignature()[1].getName()), "round-trip signature contents");
        check(q.getSignature()[0] instanceof OpenMBeanParameterInfo, "round-trip signature element is open parameter");
        check(java.io.ObjectStreamClass.lookup(OpenMBeanConstructorInfoSupport.class).getSerialVersionUID()
            == -4400441579007477003L, "serialVersionUID");
    }
}
