/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanInfoSupport conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanInfoSupportTest
 */

import java.lang.reflect.Modifier;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.OpenMBeanInfo;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;

public class OpenMBeanInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static OpenMBeanInfoSupport makeInfo() {
        OpenMBeanAttributeInfoSupport ai = new OpenMBeanAttributeInfoSupport("Name", "desc", SimpleType.STRING, true, true, false);
        OpenMBeanConstructorInfoSupport ci = new OpenMBeanConstructorInfoSupport("Test", "desc", new OpenMBeanParameterInfoSupport[0]);
        OpenMBeanOperationInfoSupport oi = new OpenMBeanOperationInfoSupport("op", "desc", new OpenMBeanParameterInfoSupport[0], SimpleType.INTEGER, MBeanOperationInfo.INFO);
        return new OpenMBeanInfoSupport("Test", "desc",
            new OpenMBeanAttributeInfoSupport[]{ai},
            new OpenMBeanConstructorInfoSupport[]{ci},
            new OpenMBeanOperationInfoSupport[]{oi},
            new MBeanNotificationInfo[0]);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanInfoSupport");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(MBeanInfo.class.isAssignableFrom(c), "extends MBeanInfo");
        check(OpenMBeanInfo.class.isAssignableFrom(c), "implements OpenMBeanInfo");
        check(c.getMethod("getAttributes") != null, "has getAttributes");
        check(c.getMethod("getConstructors") != null, "has getConstructors");
        check(c.getMethod("getOperations") != null, "has getOperations");
        check(c.getMethod("getNotifications") != null, "has getNotifications");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        OpenMBeanInfoSupport info = makeInfo();
        check(info.getClassName().equals("Test"), "className");
        check(info.getDescription().equals("desc"), "description");

        MBeanAttributeInfo[] attrs = info.getAttributes();
        check(attrs.length == 1 && attrs[0].getName().equals("Name"), "attributes");
        check(attrs[0] instanceof OpenMBeanAttributeInfoSupport, "attribute concrete type");

        MBeanConstructorInfo[] ctors = info.getConstructors();
        check(ctors.length == 1 && ctors[0].getName().equals("Test"), "constructors");

        MBeanOperationInfo[] ops = info.getOperations();
        check(ops.length == 1 && ops[0].getName().equals("op"), "operations");
        check(((OpenMBeanOperationInfoSupport) ops[0]).getReturnOpenType() == SimpleType.INTEGER, "operation return type");

        check(info.getNotifications().length == 0, "notifications empty");

        // equals / hashCode
        OpenMBeanInfoSupport info2 = makeInfo();
        check(info.equals(info2), "equals");
        check(info.hashCode() == info2.hashCode(), "hashCode equal");
        check(!info.equals(null), "equals null false");
        check(!info.equals("x"), "equals non-info false");

        check(info.toString().startsWith("javax.management.openmbean.OpenMBeanInfoSupport("), "toString prefix");
        check(info.toString().contains("Test"), "toString contains className");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- deepened: batch g ----
        testNullArraysG();
        testDefensiveCopiesG();
        testEqualsHashCodeG();
        testDescriptorCtorG();
        testToStringShapeG();
        testSerializationG();
        testMxBeanOpenInfoClosedLoopG();
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
    public interface PointMXBean { Point getPoint(); }
    public static class Point {
        private final int x; private final int y;
        public Point() { this(0, 0); }
        public Point(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }
    }
    public static class PointImpl implements PointMXBean {
        public Point getPoint() { return new Point(1, 2); }
    }
    static OpenMBeanAttributeInfo[] attrsG() {
        return new OpenMBeanAttributeInfo[] {
            new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false)
        };
    }
    static OpenMBeanAttributeInfo[] attrsZG() {
        return new OpenMBeanAttributeInfo[] {
            new OpenMBeanAttributeInfoSupport("z", "d", SimpleType.STRING, true, true, false)
        };
    }
    static OpenMBeanConstructorInfo[] ctorsG() {
        return new OpenMBeanConstructorInfo[] { new OpenMBeanConstructorInfoSupport("C", "d", null) };
    }
    static OpenMBeanOperationInfo[] opsG() {
        return new OpenMBeanOperationInfo[] {
            new OpenMBeanOperationInfoSupport("op", "d", new OpenMBeanParameterInfoSupport[] {
                new OpenMBeanParameterInfoSupport("p", "d", SimpleType.STRING) }, SimpleType.INTEGER, 1)
        };
    }
    static MBeanNotificationInfo[] notifsG() {
        return new MBeanNotificationInfo[] { new MBeanNotificationInfo(new String[] {"t"}, "n", "d") };
    }

    static void testNullArraysG() throws Exception {
        OpenMBeanInfoSupport i = new OpenMBeanInfoSupport("com.Foo", "d", null, null, null, null);
        check(i.getAttributes() != null && i.getAttributes().length == 0, "null attrs -> empty");
        check(i.getOperations() != null && i.getOperations().length == 0, "null ops -> empty");
        check(i.getConstructors() != null && i.getConstructors().length == 0, "null ctors -> empty");
        check(i.getNotifications() != null && i.getNotifications().length == 0, "null notifs -> empty");
    }

    static void testDefensiveCopiesG() throws Exception {
        OpenMBeanInfoSupport i = new OpenMBeanInfoSupport("com.Foo", "d", attrsG(), ctorsG(), opsG(), notifsG());
        check(i.getAttributes() != i.getAttributes(), "getAttributes defensive copy");
        check(i.getOperations() != i.getOperations(), "getOperations defensive copy");
        check(i.getConstructors() != i.getConstructors(), "getConstructors defensive copy");
        check(i.getNotifications() != i.getNotifications(), "getNotifications defensive copy");
        MBeanAttributeInfo[] got = i.getAttributes();
        got[0] = new OpenMBeanAttributeInfoSupport("hacked", "d", SimpleType.STRING, true, true, false);
        check("a".equals(i.getAttributes()[0].getName()), "mutating returned attrs array has no effect");
        OpenMBeanAttributeInfo[] caller = attrsG();
        OpenMBeanInfoSupport j = new OpenMBeanInfoSupport("com.Foo", "d", caller, ctorsG(), opsG(), notifsG());
        caller[0] = new OpenMBeanAttributeInfoSupport("mutated", "d", SimpleType.STRING, true, true, false);
        check("a".equals(j.getAttributes()[0].getName()), "constructor copies attribute array");
    }

    static void testEqualsHashCodeG() throws Exception {
        OpenMBeanInfoSupport a = new OpenMBeanInfoSupport("com.Foo", "d", attrsG(), ctorsG(), opsG(), notifsG());
        OpenMBeanInfoSupport b = new OpenMBeanInfoSupport("com.Foo", "d", attrsG(), ctorsG(), opsG(), notifsG());
        check(a.equals(b), "same shape equal");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode consistent");
        check(a.equals(a), "equals reflexive");
        check(!a.equals(null), "equals null false");
        check(!a.equals("x"), "equals unrelated false");
        check(!a.equals(new OpenMBeanInfoSupport("com.Bar", "d", attrsG(), ctorsG(), opsG(), notifsG())), "className participates");
        check(a.equals(new OpenMBeanInfoSupport("com.Foo", "OTHER", attrsG(), ctorsG(), opsG(), notifsG())),
            "description not in equals");
        check(!a.equals(new OpenMBeanInfoSupport("com.Foo", "d", attrsZG(), ctorsG(), opsG(), notifsG())),
            "attribute content participates");
        OpenMBeanAttributeInfo[] az = attrsZG();
        OpenMBeanAttributeInfo[] azr = { az[0], attrsG()[0] };
        OpenMBeanInfoSupport two1 = new OpenMBeanInfoSupport("com.Foo", "d", azr, ctorsG(), opsG(), notifsG());
        OpenMBeanInfoSupport two2 = new OpenMBeanInfoSupport("com.Foo", "d",
            new OpenMBeanAttributeInfo[] { attrsG()[0], az[0] }, ctorsG(), opsG(), notifsG());
        check(two1.equals(two2), "attribute order irrelevant (set semantics)");
        check(two1.hashCode() == two2.hashCode(), "set-semantics hashCode consistent");
        MBeanAttributeInfo[] plainAttrs = { new javax.management.MBeanAttributeInfo("a", "java.lang.Integer", "d", true, true, false) };
        javax.management.MBeanConstructorInfo[] plainCtors = { new javax.management.MBeanConstructorInfo("C", "d", null) };
        javax.management.MBeanOperationInfo[] plainOps = { new javax.management.MBeanOperationInfo("op", "d", null, "java.lang.Integer", 1) };
        check(!a.equals(new MBeanInfo("com.Foo", "d", plainAttrs, plainCtors, plainOps, notifsG())),
            "plain MBeanInfo not equal (not an OpenMBeanInfo)");
    }

    static void testDescriptorCtorG() throws Exception {
        javax.management.Descriptor d = new javax.management.ImmutableDescriptor(new String[] {"customKey"}, new Object[] {"customValue"});
        OpenMBeanInfoSupport i = new OpenMBeanInfoSupport("com.Foo", "d", attrsG(), ctorsG(), opsG(), notifsG(), d);
        check("customValue".equals(i.getDescriptor().getFieldValue("customKey")), "descriptor field preserved");
        check(i.getAttributes().length == 1, "descriptor form attrs");
    }

    static void testToStringShapeG() throws Exception {
        OpenMBeanInfoSupport i = new OpenMBeanInfoSupport("com.Foo", "d", attrsG(), ctorsG(), opsG(), notifsG());
        String s = i.toString();
        check(s.startsWith("javax.management.openmbean.OpenMBeanInfoSupport("), "toString prefix");
        check(s.contains("mbean_class_name=com.Foo"), "toString class name");
        check(s.contains("attributes=["), "toString attributes section");
        check(s.contains("operations=["), "toString operations section");
        check(s.contains("constructors=["), "toString constructors section");
        check(s.contains("notifications=["), "toString notifications section");
    }

    static void testSerializationG() throws Exception {
        OpenMBeanInfoSupport i = new OpenMBeanInfoSupport("com.Foo", "d", attrsG(), ctorsG(), opsG(), notifsG());
        OpenMBeanInfoSupport q = (OpenMBeanInfoSupport) roundTrip(i);
        check(i.equals(q), "round-trip equals");
        check(q.hashCode() == i.hashCode(), "round-trip hashCode");
        check("com.Foo".equals(q.getClassName()), "round-trip className");
        check(q.getAttributes().length == 1, "round-trip attrs length");
        check(q.getAttributes()[0] instanceof OpenMBeanAttributeInfo, "round-trip attr still open");
        check(((OpenMBeanAttributeInfo) q.getAttributes()[0]).getOpenType() == SimpleType.INTEGER,
            "round-trip attr open type singleton");
        check(java.io.ObjectStreamClass.lookup(OpenMBeanInfoSupport.class).getSerialVersionUID()
            == 4349395935420511492L, "serialVersionUID");
    }

    static void testMxBeanOpenInfoClosedLoopG() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName on = new javax.management.ObjectName("jck.g.openinfo:type=Point");
        server.registerMBean(new PointImpl(), on);
        MBeanInfo info = server.getMBeanInfo(on);
        check("true".equals(String.valueOf(info.getDescriptor().getFieldValue("mxbean"))), "mxbean descriptor flag true");
        check(info.getAttributes().length >= 1, "mxbean has attributes");
        boolean foundPoint = false;
        for (MBeanAttributeInfo mai : info.getAttributes()) {
            if (!"Point".equals(mai.getName())) continue;
            foundPoint = true;
            check(mai instanceof OpenMBeanAttributeInfo, "MXBean attribute info is OpenMBeanAttributeInfo");
            OpenMBeanAttributeInfo oai = (OpenMBeanAttributeInfo) mai;
            OpenType<?> ot = oai.getOpenType();
            check(ot instanceof CompositeType, "MXBean composite attribute mapped to CompositeType");
            CompositeType ct = (CompositeType) ot;
            check(ct.containsKey("x") && ct.containsKey("y"), "composite items x and y");
            check(ct.getType("x").equals(SimpleType.INTEGER), "item x is INTEGER");
            check(ct.getType("y").equals(SimpleType.INTEGER), "item y is INTEGER");
            check(ot.equals(mai.getDescriptor().getFieldValue("openType")), "descriptor openType equals getOpenType");
            check(oai.isReadable() && !oai.isWritable(), "derived getter readable-only");
        }
        check(foundPoint, "Point attribute found");
        Object v = server.getAttribute(on, "Point");
        check(v instanceof javax.management.openmbean.CompositeData, "getAttribute returns CompositeData");
        javax.management.openmbean.CompositeData cd = (javax.management.openmbean.CompositeData) v;
        check(Integer.valueOf(1).equals(cd.get("x")) && Integer.valueOf(2).equals(cd.get("y")), "composite data contents");
        for (MBeanOperationInfo moi : info.getOperations()) {
            check(moi instanceof OpenMBeanOperationInfo, "MXBean operation info is OpenMBeanOperationInfo");
        }
    }
}
