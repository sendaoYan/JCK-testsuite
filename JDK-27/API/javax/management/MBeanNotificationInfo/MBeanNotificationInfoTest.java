/*
 * @test
 * @summary javax.management.MBeanNotificationInfo conformance: white-box structure + black-box behavior.
 *          Deepened: null/empty types -> shared empty instance, defensive getter,
 *          order-sensitive equals, descriptor ctor round-trip, serialization.
 * @run main MBeanNotificationInfoTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanNotificationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanNotificationInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == MBeanFeatureInfo.class, "extends MBeanFeatureInfo");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getNotifTypes"), "has getNotifTypes");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        String[] types = {"jmx.attribute.change", "jmx.monitor.counter.threshold"};
        MBeanNotificationInfo n = new MBeanNotificationInfo(types, "Notif", "notif desc");
        check("Notif".equals(n.getName()), "getName");
        check("notif desc".equals(n.getDescription()), "getDescription");
        String[] got = n.getNotifTypes();
        check(got.length == 2, "getNotifTypes length");
        check("jmx.attribute.change".equals(got[0]), "notifTypes[0]");
        check("jmx.monitor.counter.threshold".equals(got[1]), "notifTypes[1]");

        // descriptor constructor
        ImmutableDescriptor d = new ImmutableDescriptor("name=Notif", "description=notif desc");
        MBeanNotificationInfo nd = new MBeanNotificationInfo(types, "Notif", "notif desc", d);
        check(nd.getDescriptor() != null && "Notif".equals(nd.getDescriptor().getFieldValue("name")), "descriptor ctor");

        // equals compares name, description, and types
        check(n.equals(new MBeanNotificationInfo(types, "Notif", "notif desc")), "equals identical");
        check(!n.equals(new MBeanNotificationInfo(new String[]{"other"}, "Notif", "notif desc")), "different types not equal");
        check(!n.equals(new MBeanNotificationInfo(types, "Other", "notif desc")), "different name not equal");
        check(!n.equals(null), "not equal null");
        check(n.hashCode() == new MBeanNotificationInfo(types, "Notif", "notif desc").hashCode(), "equal hashCode");

        // clone
        MBeanNotificationInfo clone = (MBeanNotificationInfo) n.clone();
        check(clone != n && clone.equals(n), "clone equals original");
        check(n.toString() != null, "toString non-null");
    }

    // ---- deepened: sharing, order sensitivity, serialization ----------------

    static void testDeepened() throws Exception {
        String[] types = {"jck.a.b", "jck.c.d"};
        MBeanNotificationInfo viaNull = new MBeanNotificationInfo(null, "n", "d");
        check(viaNull.getNotifTypes().length == 0, "null types -> empty");
        check(viaNull.getNotifTypes() == viaNull.getNotifTypes(),
                "empty notif types shared between calls");
        MBeanNotificationInfo nonEmpty = new MBeanNotificationInfo(types, "Notif", "notif desc");
        String[] got = nonEmpty.getNotifTypes();
        got[0] = null;
        check(nonEmpty.getNotifTypes()[0] != null, "non-empty getNotifTypes defensive copy");

        check(!nonEmpty.equals(new MBeanNotificationInfo(
                new String[]{"jck.c.d", "jck.a.b"}, "Notif", "notif desc")),
                "equals is order-sensitive over notif types");
        check(!nonEmpty.equals(new MBeanNotificationInfo(new String[]{"only.one"}, "Notif", "notif desc")),
                "different type count not equal");
        check(nonEmpty.equals(new MBeanNotificationInfo(types, "Notif", "notif desc")),
                "identical reconstruction equal");
        check(nonEmpty.hashCode() == new MBeanNotificationInfo(types, "Notif", "notif desc").hashCode(),
                "equal objects equal hashCode");

        ImmutableDescriptor d = new ImmutableDescriptor("units=ms");
        MBeanNotificationInfo withD = new MBeanNotificationInfo(types, "Notif", "notif desc", d);
        check("ms".equals(withD.getDescriptor().getFieldValue("units")), "descriptor ctor round-trip");
        check(new MBeanNotificationInfo(types, "Notif", "notif desc", null).getDescriptor()
                .getFieldNames().length == 0, "null descriptor -> empty");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(withD);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        MBeanNotificationInfo read = (MBeanNotificationInfo) ois.readObject();
        check(read.equals(withD), "serialization round-trip equals");
        check(read.getNotifTypes().length == 2, "notif types survive serialization");
        check("ms".equals(read.getDescriptor().getFieldValue("units")), "descriptor survives serialization");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
