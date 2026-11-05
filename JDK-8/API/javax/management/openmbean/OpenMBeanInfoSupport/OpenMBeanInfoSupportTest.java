/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanInfoSupport conformance: ctor forms, null arrays, defensive copies, set-semantics equals, serialization, MXBean open-info closed loop.
 * @run main OpenMBeanInfoSupportTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfo;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

public class OpenMBeanInfoSupportTest {
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

    // MXBean closed-loop fixtures (self-contained per file)
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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanInfoSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.MBeanInfo".equals(c.getSuperclass().getName()), "superclass javax.management.MBeanInfo");
        check(Class.forName("javax.management.openmbean.OpenMBeanInfo").isAssignableFrom(c), "implements/extends javax.management.openmbean.OpenMBeanInfo");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "toString"), "declares toString");
        // ---- deepened: batch g ----
        testBasicCtor();
        testNullArrays();
        testDefensiveCopies();
        testEqualsHashCode();
        testDescriptorCtor();
        testToStringShape();
        testSerialization();
        testMxBeanOpenInfoClosedLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static OpenMBeanAttributeInfo[] attrs() {
        return new OpenMBeanAttributeInfo[] {
            new OpenMBeanAttributeInfoSupport("a", "d", SimpleType.INTEGER, true, true, false)
        };
    }
    static OpenMBeanAttributeInfo[] attrsZ() {
        return new OpenMBeanAttributeInfo[] {
            new OpenMBeanAttributeInfoSupport("z", "d", SimpleType.STRING, true, true, false)
        };
    }
    static OpenMBeanConstructorInfo[] ctors() {
        return new OpenMBeanConstructorInfo[] { new OpenMBeanConstructorInfoSupport("C", "d", null) };
    }
    static OpenMBeanOperationInfo[] ops() {
        return new OpenMBeanOperationInfo[] {
            new OpenMBeanOperationInfoSupport("op", "d", new OpenMBeanParameterInfoSupport[] {
                new OpenMBeanParameterInfoSupport("p", "d", SimpleType.STRING) }, SimpleType.INTEGER, 1)
        };
    }
    static MBeanNotificationInfo[] notifs() {
        return new MBeanNotificationInfo[] { new MBeanNotificationInfo(new String[] {"t"}, "n", "d") };
    }

    // ---- deepened: full 6-arg constructor ----
    static void testBasicCtor() throws Exception {
        OpenMBeanInfoSupport i = new OpenMBeanInfoSupport("com.Foo", "desc", attrs(), ctors(), ops(), notifs());
        check("com.Foo".equals(i.getClassName()), "className");
        check("desc".equals(i.getDescription()), "description");
        check(i.getAttributes().length == 1, "attributes length");
        check("a".equals(i.getAttributes()[0].getName()), "attribute name");
        check(i.getAttributes()[0] instanceof OpenMBeanAttributeInfo, "attribute is OpenMBeanAttributeInfo");
        check(((OpenMBeanAttributeInfo) i.getAttributes()[0]).getOpenType() == SimpleType.INTEGER,
            "attribute open type reachable");
        check(i.getConstructors().length == 1, "constructors length");
        check(i.getConstructors()[0] instanceof OpenMBeanConstructorInfo, "constructor is OpenMBeanConstructorInfo");
        check(i.getOperations().length == 1, "operations length");
        check(i.getOperations()[0] instanceof OpenMBeanOperationInfo, "operation is OpenMBeanOperationInfo");
        check(((OpenMBeanOperationInfo) i.getOperations()[0]).getReturnOpenType() == SimpleType.INTEGER,
            "operation return open type reachable");
        check(i.getNotifications().length == 1, "notifications length");
        // via interface reference
        OpenMBeanInfo viaIface = i;
        check("com.Foo".equals(viaIface.getClassName()), "className via interface");
        check(viaIface.getAttributes().length == 1 && viaIface.getOperations().length == 1
            && viaIface.getConstructors().length == 1 && viaIface.getNotifications().length == 1,
            "all sections via interface");
        // clone() inherited from MBeanInfo
        MBeanInfo cloned = (MBeanInfo) i.clone();
        check(cloned.equals(i) || cloned.getClassName().equals("com.Foo"), "clone works");
        check(i.getDescriptor() != null, "descriptor non-null");
    }

    // ---- deepened: null arrays normalize to empty arrays ----
    static void testNullArrays() throws Exception {
        OpenMBeanInfoSupport i = new OpenMBeanInfoSupport("com.Foo", "d", null, null, null, null);
        check(i.getAttributes() != null && i.getAttributes().length == 0, "null attrs -> empty");
        check(i.getOperations() != null && i.getOperations().length == 0, "null ops -> empty");
        check(i.getConstructors() != null && i.getConstructors().length == 0, "null ctors -> empty");
        check(i.getNotifications() != null && i.getNotifications().length == 0, "null notifs -> empty");
    }

    // ---- deepened: getter defensive copies ----
    static void testDefensiveCopies() throws Exception {
        OpenMBeanInfoSupport i = new OpenMBeanInfoSupport("com.Foo", "d", attrs(), ctors(), ops(), notifs());
        check(i.getAttributes() != i.getAttributes(), "getAttributes defensive copy");
        check(i.getOperations() != i.getOperations(), "getOperations defensive copy");
        check(i.getConstructors() != i.getConstructors(), "getConstructors defensive copy");
        check(i.getNotifications() != i.getNotifications(), "getNotifications defensive copy");
        // mutating a returned array has no effect on subsequent reads
        MBeanAttributeInfo[] got = i.getAttributes();
        got[0] = new OpenMBeanAttributeInfoSupport("hacked", "d", SimpleType.STRING, true, true, false);
        check("a".equals(i.getAttributes()[0].getName()), "mutating returned attrs array has no effect");
        // constructor copies the caller's arrays (OpenMBeanInfoSupport converts arrays)
        OpenMBeanAttributeInfo[] caller = attrs();
        OpenMBeanInfoSupport j = new OpenMBeanInfoSupport("com.Foo", "d", caller, ctors(), ops(), notifs());
        caller[0] = new OpenMBeanAttributeInfoSupport("mutated", "d", SimpleType.STRING, true, true, false);
        check("a".equals(j.getAttributes()[0].getName()), "constructor copies attribute array");
    }

    // ---- deepened: equals/hashCode (set semantics, description not included) ----
    static void testEqualsHashCode() throws Exception {
        OpenMBeanInfoSupport a = new OpenMBeanInfoSupport("com.Foo", "d", attrs(), ctors(), ops(), notifs());
        OpenMBeanInfoSupport b = new OpenMBeanInfoSupport("com.Foo", "d", attrs(), ctors(), ops(), notifs());
        check(a.equals(b), "same shape equal");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode consistent");
        check(a.equals(a), "equals reflexive");
        check(!a.equals(null), "equals null false");
        check(!a.equals("x"), "equals unrelated false");
        check(!a.equals(new OpenMBeanInfoSupport("com.Bar", "d", attrs(), ctors(), ops(), notifs())), "className participates");
        check(a.equals(new OpenMBeanInfoSupport("com.Foo", "OTHER", attrs(), ctors(), ops(), notifs())),
            "description not in equals");
        check(!a.equals(new OpenMBeanInfoSupport("com.Foo", "d", attrsZ(), ctors(), ops(), notifs())),
            "attribute content participates");
        // set semantics: attribute order in the arrays is irrelevant
        OpenMBeanAttributeInfo[] az = attrsZ();
        OpenMBeanAttributeInfo[] azr = { az[0], attrs()[0] };
        OpenMBeanInfoSupport two1 = new OpenMBeanInfoSupport("com.Foo", "d", azr, ctors(), ops(), notifs());
        OpenMBeanInfoSupport two2 = new OpenMBeanInfoSupport("com.Foo", "d",
            new OpenMBeanAttributeInfo[] { attrs()[0], az[0] }, ctors(), ops(), notifs());
        check(two1.equals(two2), "attribute order irrelevant (set semantics)");
        check(two1.hashCode() == two2.hashCode(), "set-semantics hashCode consistent");
        // plain MBeanInfo with same content is not equal (different class is fine - OpenMBeanInfo cast fails)
        MBeanAttributeInfo[] plainAttrs = { new javax.management.MBeanAttributeInfo("a", "java.lang.Integer", "d", true, true, false) };
        javax.management.MBeanConstructorInfo[] plainCtors = { new javax.management.MBeanConstructorInfo("C", "d", null) };
        javax.management.MBeanOperationInfo[] plainOps = { new javax.management.MBeanOperationInfo("op", "d", null, "java.lang.Integer", 1) };
        check(!a.equals(new MBeanInfo("com.Foo", "d", plainAttrs, plainCtors, plainOps, notifs())),
            "plain MBeanInfo not equal (not an OpenMBeanInfo)");
    }

    // ---- deepened: Descriptor constructor form ----
    static void testDescriptorCtor() throws Exception {
        Descriptor d = new ImmutableDescriptor(new String[] {"customKey"}, new Object[] {"customValue"});
        OpenMBeanInfoSupport i = new OpenMBeanInfoSupport("com.Foo", "d", attrs(), ctors(), ops(), notifs(), d);
        check("customValue".equals(i.getDescriptor().getFieldValue("customKey")), "descriptor field preserved");
        check(i.getAttributes().length == 1, "descriptor form attrs");
    }

    // ---- deepened: toString shape ----
    static void testToStringShape() throws Exception {
        OpenMBeanInfoSupport i = new OpenMBeanInfoSupport("com.Foo", "d", attrs(), ctors(), ops(), notifs());
        String s = i.toString();
        check(s.startsWith("javax.management.openmbean.OpenMBeanInfoSupport("), "toString prefix");
        check(s.contains("mbean_class_name=com.Foo"), "toString class name");
        check(s.contains("attributes=["), "toString attributes section");
        check(s.contains("operations=["), "toString operations section");
        check(s.contains("constructors=["), "toString constructors section");
        check(s.contains("notifications=["), "toString notifications section");
    }

    // ---- deepened: serialization round-trip ----
    static void testSerialization() throws Exception {
        OpenMBeanInfoSupport i = new OpenMBeanInfoSupport("com.Foo", "d", attrs(), ctors(), ops(), notifs());
        OpenMBeanInfoSupport q = (OpenMBeanInfoSupport) roundTrip(i);
        check(i.equals(q), "round-trip equals");
        check(q.hashCode() == i.hashCode(), "round-trip hashCode");
        check("com.Foo".equals(q.getClassName()), "round-trip className");
        check(q.getAttributes().length == 1, "round-trip attrs length");
        check(q.getAttributes()[0] instanceof OpenMBeanAttributeInfo, "round-trip attr still open");
        check(((OpenMBeanAttributeInfo) q.getAttributes()[0]).getOpenType() == SimpleType.INTEGER,
            "round-trip attr open type singleton");
        check(ObjectStreamClass.lookup(OpenMBeanInfoSupport.class).getSerialVersionUID()
            == 4349395935420511492L, "serialVersionUID");
    }

    // ---- deepened: MXBean registration produces OpenMBean*Info with CompositeType (closed loop) ----
    static void testMxBeanOpenInfoClosedLoop() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.g.openinfo:type=Point");
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
        // attribute value round-trip through the server is CompositeData with x=1,y=2
        Object v = server.getAttribute(on, "Point");
        check(v instanceof javax.management.openmbean.CompositeData, "getAttribute returns CompositeData");
        javax.management.openmbean.CompositeData cd = (javax.management.openmbean.CompositeData) v;
        check(Integer.valueOf(1).equals(cd.get("x")) && Integer.valueOf(2).equals(cd.get("y")), "composite data contents");
        // MXBean operations (if any) carry OpenMBeanOperationInfo
        for (MBeanOperationInfo moi : info.getOperations()) {
            check(moi instanceof OpenMBeanOperationInfo, "MXBean operation info is OpenMBeanOperationInfo");
        }
    }
}
