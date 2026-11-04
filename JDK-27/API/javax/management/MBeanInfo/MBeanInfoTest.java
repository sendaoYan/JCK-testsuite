/*
 * @test
 * @summary javax.management.MBeanInfo conformance: white-box structure + black-box behavior.
 *          Deepened: null-array defaulting, defensive array getters (empty shared),
 *          equals field matrix, shallow clone element sharing, descriptor defaults,
 *          serialization round-trip.
 * @run main MBeanInfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.management.*;

public class MBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static MBeanInfo build(String className, String desc) {
        MBeanAttributeInfo[] attrs = { new MBeanAttributeInfo("Attr", "int", "attr desc", true, true, false) };
        MBeanConstructorInfo[] ctors = { new MBeanConstructorInfo("Ctor", "ctor desc", new MBeanParameterInfo[0]) };
        MBeanOperationInfo[] ops = { new MBeanOperationInfo("Op", "op desc", new MBeanParameterInfo[0], "int", MBeanOperationInfo.INFO) };
        MBeanNotificationInfo[] notifs = { new MBeanNotificationInfo(new String[]{"type"}, "Notif", "notif desc") };
        return new MBeanInfo(className, desc, attrs, ctors, ops, notifs);
    }
    static MBeanInfo sample() { return build("com.example.Foo", "desc"); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(DescriptorRead.class.isAssignableFrom(c), "implements DescriptorRead");
        check(hasMethod(c, "getClassName"), "has getClassName");
        check(hasMethod(c, "getDescription"), "has getDescription");
        check(hasMethod(c, "getAttributes"), "has getAttributes");
        check(hasMethod(c, "getConstructors"), "has getConstructors");
        check(hasMethod(c, "getOperations"), "has getOperations");
        check(hasMethod(c, "getNotifications"), "has getNotifications");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanInfo mi = build("java.lang.String", "desc");
        check("java.lang.String".equals(mi.getClassName()), "getClassName");
        check("desc".equals(mi.getDescription()), "getDescription");
        check(mi.getAttributes().length == 1 && "Attr".equals(mi.getAttributes()[0].getName()), "getAttributes");
        check(mi.getConstructors().length == 1 && "Ctor".equals(mi.getConstructors()[0].getName()), "getConstructors");
        check(mi.getOperations().length == 1 && "Op".equals(mi.getOperations()[0].getName()), "getOperations");
        check(mi.getNotifications().length == 1 && "Notif".equals(mi.getNotifications()[0].getName()), "getNotifications");
        check(mi.getDescriptor() != null, "getDescriptor non-null");

        // empty arrays case
        MBeanInfo empty = new MBeanInfo("c", "d",
                new MBeanAttributeInfo[0], new MBeanConstructorInfo[0],
                new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
        check(empty.getAttributes().length == 0 && empty.getConstructors().length == 0
                && empty.getOperations().length == 0 && empty.getNotifications().length == 0,
                "empty MBeanInfo arrays");

        // clone produces equal, independent object
        MBeanInfo clone = (MBeanInfo) mi.clone();
        check(clone != mi, "clone not identical");
        check(clone.equals(mi), "clone equals original");

        // equals: identical construction equals; different className/description differ
        check(mi.equals(build("java.lang.String", "desc")), "equals identical construction");
        check(!mi.equals(build("other.Class", "desc")), "different className not equal");
        check(!mi.equals(build("java.lang.String", "other")), "different description not equal");
        check(!mi.equals(null) && !mi.equals("x"), "not equal null/non-MBeanInfo");
        check(mi.hashCode() == build("java.lang.String", "desc").hashCode(), "equal hashCode");

        // descriptor variant constructor preserves descriptor
        ImmutableDescriptor d = new ImmutableDescriptor("name=n", "description=d");
        MBeanInfo withDesc = new MBeanInfo("c", "d", new MBeanAttributeInfo[0],
                new MBeanConstructorInfo[0], new MBeanOperationInfo[0],
                new MBeanNotificationInfo[0], d);
        check(withDesc.getDescriptor() != null && "n".equals(withDesc.getDescriptor().getFieldValue("name")),
                "descriptor ctor preserves descriptor");
    }

    // ---- deepened: null arrays + empty-array sharing -----------------------

    static void testNullArraysAndSharing() {
        MBeanInfo empty = new MBeanInfo("c", null, null, null, null, null);
        check(empty.getDescription() == null, "null description retained");
        check(empty.getAttributes() != null && empty.getAttributes().length == 0, "null attributes -> empty");
        check(empty.getOperations().length == 0, "null operations -> empty");
        check(empty.getConstructors().length == 0, "null constructors -> empty");
        check(empty.getNotifications().length == 0, "null notifications -> empty");
        // both calls return the same shared instance (nothing to protect defensively)
        check(empty.getAttributes() == empty.getAttributes(), "empty getAttributes shared instance");
        check(empty.getOperations() == empty.getOperations(), "empty getOperations shared instance");
        check(empty.getConstructors() == empty.getConstructors(), "empty getConstructors shared instance");
        check(empty.getNotifications() == empty.getNotifications(), "empty getNotifications shared instance");
    }

    // ---- deepened: defensive copies of non-empty array getters -------------

    static void testArrayGetterDefensive() {
        MBeanInfo info = sample();
        MBeanAttributeInfo[] a = info.getAttributes(); a[0] = null;
        check(info.getAttributes()[0] != null, "getAttributes defensive copy");
        MBeanOperationInfo[] o = info.getOperations(); o[0] = null;
        check(info.getOperations()[0] != null, "getOperations defensive copy");
        MBeanConstructorInfo[] c = info.getConstructors(); c[0] = null;
        check(info.getConstructors()[0] != null, "getConstructors defensive copy");
        MBeanNotificationInfo[] n = info.getNotifications(); n[0] = null;
        check(info.getNotifications()[0] != null, "getNotifications defensive copy");
        check(info.getAttributes() != info.getAttributes(), "fresh array per call when non-empty");
        // clone is shallow: elements are shared between original and clone
        MBeanInfo clone = (MBeanInfo) info.clone();
        check(clone != info && clone.equals(info), "clone distinct but equal");
        check(clone.getAttributes()[0] == info.getAttributes()[0], "clone shares elements (shallow)");
    }

    // ---- deepened: equals participation of every field ---------------------

    static void testEqualsFieldParticipation() {
        MBeanInfo base = sample();
        check(base.equals(sample()) && base.hashCode() == sample().hashCode(),
                "identical reconstruction equal + hashCode");
        MBeanInfo otherDescr = new MBeanInfo("com.example.Foo", "desc",
                base.getAttributes(), base.getConstructors(),
                base.getOperations(), base.getNotifications(),
                new ImmutableDescriptor("extra=1"));
        check(!base.equals(otherDescr), "descriptor participates in equals");
        check(!base.equals(new MBeanInfo("com.example.Foo", "desc",
                new MBeanAttributeInfo[]{new MBeanAttributeInfo("Attr", "long", "attr desc", true, true, false)},
                base.getConstructors(), base.getOperations(), base.getNotifications())),
                "attributes participate in equals");
        check(!base.equals(new MBeanInfo("com.example.Foo", "desc",
                base.getAttributes(), new MBeanConstructorInfo[0],
                base.getOperations(), base.getNotifications())),
                "constructors participate in equals");
        check(!base.equals(new MBeanInfo("com.example.Foo", "desc",
                base.getAttributes(), base.getConstructors(),
                new MBeanOperationInfo[0], base.getNotifications())),
                "operations participate in equals");
        check(!base.equals(new MBeanInfo("com.example.Foo", "desc",
                base.getAttributes(), base.getConstructors(),
                base.getOperations(), new MBeanNotificationInfo[0])),
                "notifications participate in equals");
        MBeanInfo nd1 = new MBeanInfo("c", null, null, null, null, null);
        check(nd1.equals(new MBeanInfo("c", null, null, null, null, null)), "null description both sides equal");
        check(!nd1.equals(new MBeanInfo("c", "d", null, null, null, null)), "null vs non-null description differ");
    }

    // ---- deepened: descriptor defaults + serialization ----------------------

    static void testDescriptorDefaultsAndSerialization() throws Exception {
        MBeanInfo plain = sample();
        check(plain.getDescriptor().getFieldNames().length == 0, "6-arg ctor default empty descriptor");
        check(plain.getDescriptor() instanceof ImmutableDescriptor, "default descriptor is ImmutableDescriptor");
        check(new MBeanInfo("c", "d", null, null, null, null, null).getDescriptor().getFieldNames().length == 0,
                "null descriptor -> empty");

        MBeanInfo original = new MBeanInfo("com.example.Foo", "desc",
                new MBeanAttributeInfo[]{new MBeanAttributeInfo("Count", "int", "count", true, false, false)},
                new MBeanConstructorInfo[0], new MBeanOperationInfo[0], new MBeanNotificationInfo[0],
                new ImmutableDescriptor("k=v"));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MBeanInfo read = (MBeanInfo) ois.readObject();
        check(read.equals(original), "serialization round-trip equals");
        check("v".equals(read.getDescriptor().getFieldValue("k")), "descriptor survives serialization");
        check("count".equals(read.getAttributes()[0].getDescription()), "attribute detail survives serialization");
        check(read.getAttributes()[0] != null && read.getAttributes().length == 1, "arrays usable after round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNullArraysAndSharing();
        testArrayGetterDefensive();
        testEqualsFieldParticipation();
        testDescriptorDefaultsAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
