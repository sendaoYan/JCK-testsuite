/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanConstructorInfoSupport conformance: ctor forms, signature defensive copies, equals/hashCode, descriptor, serialization.
 * @run main OpenMBeanConstructorInfoSupportTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanParameterInfo;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;

public class OpenMBeanConstructorInfoSupportTest {
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
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanConstructorInfoSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanConstructorInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanConstructorInfo");
        check(Class.forName("javax.management.openmbean.OpenMBeanConstructorInfo").isAssignableFrom(c), "implements/extends javax.management.openmbean.OpenMBeanConstructorInfo");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "toString"), "declares toString");
        check(c.getMethod("getSignature") != null, "has getSignature (inherited)");
        // ---- deepened: batch g ----
        testBasicCtor();
        testSignatureSemantics();
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

    // ---- deepened: basic (name, desc, params) form ----
    static void testBasicCtor() throws Exception {
        OpenMBeanConstructorInfoSupport k = new OpenMBeanConstructorInfoSupport("Ctor", "desc", params());
        check("Ctor".equals(k.getName()), "basic name");
        check("desc".equals(k.getDescription()), "basic desc");
        check(k.getSignature().length == 2, "signature length");
        check("p".equals(k.getSignature()[0].getName()), "signature first param");
        check("q".equals(k.getSignature()[1].getName()), "signature second param");
        check(k.getSignature()[0] instanceof OpenMBeanParameterInfo, "signature element is open parameter");
        // via interface reference
        OpenMBeanConstructorInfo viaIface = k;
        check("Ctor".equals(viaIface.getName()), "name via interface");
        check("desc".equals(viaIface.getDescription()), "desc via interface");
        check(viaIface.getSignature().length == 2, "signature via interface");
    }

    // ---- deepened: signature defensive copy semantics ----
    static void testSignatureSemantics() throws Exception {
        OpenMBeanParameterInfo[] ps = params();
        OpenMBeanConstructorInfoSupport k = new OpenMBeanConstructorInfoSupport("Ctor", "d", ps);
        MBeanParameterInfo[] s1 = k.getSignature();
        MBeanParameterInfo[] s2 = k.getSignature();
        check(s1 != s2, "getSignature returns fresh array each call");
        check(s1.length == s2.length && s1[0].equals(s2[0]) && s1[1].equals(s2[1]), "signature contents stable");
        // mutating the returned array has no effect
        s1[0] = new OpenMBeanParameterInfoSupport("hacked", "d", SimpleType.STRING);
        check("p".equals(k.getSignature()[0].getName()), "mutating returned array has no effect");
        // constructor copies the array
        ps[0] = new OpenMBeanParameterInfoSupport("mutated", "d", SimpleType.STRING);
        check("p".equals(k.getSignature()[0].getName()), "constructor copies parameter array");
        // elements shared (shallow copy)
        OpenMBeanParameterInfo[] ps2 = params();
        OpenMBeanConstructorInfoSupport k2 = new OpenMBeanConstructorInfoSupport("Ctor", "d", ps2);
        check(k2.getSignature()[0] == ps2[0], "signature elements shared (shallow copy)");
        // null params gives empty signature
        OpenMBeanConstructorInfoSupport n = new OpenMBeanConstructorInfoSupport("Ctor", "d", null);
        check(n.getSignature() != null && n.getSignature().length == 0, "null params -> empty signature");
    }

    // ---- deepened: equals/hashCode (name + signature only; desc not included) ----
    static void testEqualsHashCode() throws Exception {
        OpenMBeanConstructorInfoSupport a = new OpenMBeanConstructorInfoSupport("C", "d", params());
        OpenMBeanConstructorInfoSupport b = new OpenMBeanConstructorInfoSupport("C", "d", params());
        check(a.equals(b), "same shape equal");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode consistent");
        check(a.equals(a), "equals reflexive");
        check(!a.equals(null), "equals null false");
        check(!a.equals("x"), "equals unrelated false");
        check(a.equals(new OpenMBeanConstructorInfoSupport("C", "OTHER", params())), "description not in equals");
        check(!a.equals(new OpenMBeanConstructorInfoSupport("D", "d", params())), "name participates");
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
            new OpenMBeanParameterInfo[] { params()[0] });
        check(!one.equals(a), "one param vs two params not equal");
    }

    // ---- deepened: Descriptor constructor form ----
    static void testDescriptorCtor() throws Exception {
        Descriptor d = new ImmutableDescriptor(new String[] {"customKey"}, new Object[] {"customValue"});
        OpenMBeanConstructorInfoSupport k = new OpenMBeanConstructorInfoSupport("C", "d", params(), d);
        check("customValue".equals(k.getDescriptor().getFieldValue("customKey")), "descriptor field preserved");
        check(k.getSignature().length == 2, "descriptor form signature");
    }

    // ---- deepened: toString shape ----
    static void testToStringShape() throws Exception {
        OpenMBeanConstructorInfoSupport k = new OpenMBeanConstructorInfoSupport("C", "d", params());
        String s = k.toString();
        check(s.startsWith("javax.management.openmbean.OpenMBeanConstructorInfoSupport("), "toString prefix");
        check(s.contains("name=C"), "toString name");
        check(s.contains("OpenMBeanParameterInfoSupport(name=p"), "toString signature element");
    }

    // ---- deepened: serialization round-trip ----
    static void testSerialization() throws Exception {
        OpenMBeanConstructorInfoSupport k = new OpenMBeanConstructorInfoSupport("C", "d", params());
        OpenMBeanConstructorInfoSupport q = (OpenMBeanConstructorInfoSupport) roundTrip(k);
        check(k.equals(q), "round-trip equals");
        check(q.hashCode() == k.hashCode(), "round-trip hashCode");
        check(q.getSignature().length == 2, "round-trip signature length");
        check("q".equals(q.getSignature()[1].getName()), "round-trip signature contents");
        check(q.getSignature()[0] instanceof OpenMBeanParameterInfo, "round-trip signature element is open parameter");
        check(ObjectStreamClass.lookup(OpenMBeanConstructorInfoSupport.class).getSerialVersionUID()
            == -4400441579007477003L, "serialVersionUID");
    }
}
