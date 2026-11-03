/*
 * @test
 * @summary javax.management.modelmbean.RequiredModelMBean conformance: white-box structure + black-box behavior.
 * @run main RequiredModelMBeanTest
 */

import javax.management.AttributeNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotificationEmitter;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.RequiredModelMBean;

public class RequiredModelMBeanTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.RequiredModelMBean");
        check(!c.isInterface(), "not interface");
        check(ModelMBean.class.isAssignableFrom(c), "implements ModelMBean");
        check(MBeanRegistration.class.isAssignableFrom(c), "implements MBeanRegistration");
        check(NotificationEmitter.class.isAssignableFrom(c), "implements NotificationEmitter");
        check(c.getConstructors().length >= 2, "has no-arg and ModelMBeanInfo constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RequiredModelMBean mb = new RequiredModelMBean();
        MBeanInfo defaultInfo = mb.getMBeanInfo();
        check(defaultInfo != null, "getMBeanInfo non-null");
        check(defaultInfo.getClassName() != null, "default info className non-null");

        ModelMBeanInfoSupport info = new ModelMBeanInfoSupport(
                "MyMBean", "desc",
                new javax.management.modelmbean.ModelMBeanAttributeInfo[0],
                new javax.management.modelmbean.ModelMBeanConstructorInfo[0],
                new javax.management.modelmbean.ModelMBeanOperationInfo[0],
                new javax.management.modelmbean.ModelMBeanNotificationInfo[0]);
        mb.setModelMBeanInfo(info);
        check("MyMBean".equals(mb.getMBeanInfo().getClassName()),
                "setModelMBeanInfo reflected");

        // No attributes in the info -> getAttribute throws AttributeNotFoundException.
        boolean anfe = false;
        try {
            mb.getAttribute("Missing");
        } catch (AttributeNotFoundException e) {
            anfe = true;
        }
        check(anfe, "getAttribute on missing attribute throws AttributeNotFoundException");

        // Registration/unregistration lifecycle through a private MBeanServer.
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        try {
            ObjectName on = new ObjectName("test:type=RequiredModelMBeanTest");
            ObjectInstance registered = mbs.registerMBean(mb, on);
            check(on.equals(registered.getObjectName()), "registerMBean returns name");
            check(mbs.isRegistered(on), "isRegistered after register");
            mbs.unregisterMBean(on);
            check(!mbs.isRegistered(on), "not registered after unregister");
        } finally {
            MBeanServerFactory.releaseMBeanServer(mbs);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
