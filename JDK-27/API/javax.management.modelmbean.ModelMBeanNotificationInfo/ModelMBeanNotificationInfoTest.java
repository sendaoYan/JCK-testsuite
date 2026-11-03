/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanNotificationInfo conformance: white-box structure + black-box behavior.
 * @run main ModelMBeanNotificationInfoTest
 */

import javax.management.Descriptor;
import javax.management.MBeanNotificationInfo;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;

public class ModelMBeanNotificationInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanNotificationInfo");
        check(MBeanNotificationInfo.class.isAssignableFrom(c), "extends MBeanNotificationInfo");
        check(javax.management.DescriptorAccess.class.isAssignableFrom(c), "implements DescriptorAccess");
        check(c.getConstructors().length >= 3, "multiple public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ModelMBeanNotificationInfo ni = new ModelMBeanNotificationInfo(
                new String[]{"a.b", "c.d"}, "javax.management.Notification", "desc");
        check("javax.management.Notification".equals(ni.getName()), "name");
        check("desc".equals(ni.getDescription()), "description");
        check(ni.getNotifTypes().length == 2, "notif types length");
        check("a.b".equals(ni.getNotifTypes()[0]), "notif type 0");
        check("c.d".equals(ni.getNotifTypes()[1]), "notif type 1");

        Descriptor d = ni.getDescriptor();
        check(d != null, "descriptor non-null");
        check("javax.management.Notification".equals(d.getFieldValue("name")), "descriptor name");
        check("notification".equals(d.getFieldValue("descriptorType")), "descriptor type");

        // Descriptor-bearing constructor
        DescriptorSupport custom = new DescriptorSupport(
                "name=N2", "descriptorType=notification", "extra=q");
        ModelMBeanNotificationInfo ni2 = new ModelMBeanNotificationInfo(
                new String[]{"x.y"}, "N2", "d2", custom);
        check("q".equals(ni2.getDescriptor().getFieldValue("extra")), "descriptor ctor extra");

        // copy constructor
        ModelMBeanNotificationInfo copy = new ModelMBeanNotificationInfo(ni);
        check("javax.management.Notification".equals(copy.getName()), "copy ctor name");
        check(ni.equals(copy), "copy equals original");

        // clone
        ModelMBeanNotificationInfo cloned = (ModelMBeanNotificationInfo) ni.clone();
        check(ni.equals(cloned), "clone equals original");
        check(cloned.getNotifTypes().length == 2, "clone notif types");

        // setDescriptor (matching name + descriptorType)
        DescriptorSupport nd = new DescriptorSupport(
                "name=javax.management.Notification", "descriptorType=notification", "extra=r");
        ni.setDescriptor(nd);
        check("r".equals(ni.getDescriptor().getFieldValue("extra")), "setDescriptor updates descriptor");

        check(ni.toString().contains("javax.management.Notification"), "toString contains name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
