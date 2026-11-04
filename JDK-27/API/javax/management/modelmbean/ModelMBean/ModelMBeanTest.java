/*
 * @test
 * @summary javax.management.modelmbean.ModelMBean conformance: white-box structure + black-box behavior.
 * @run main ModelMBeanTest
 */

import java.lang.reflect.Method;
import javax.management.DynamicMBean;
import javax.management.PersistentMBean;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationBroadcaster;
import javax.management.modelmbean.RequiredModelMBean;

public class ModelMBeanTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBean");
        check(c.isInterface(), "is interface");
        check(DynamicMBean.class.isAssignableFrom(c), "extends DynamicMBean");
        check(PersistentMBean.class.isAssignableFrom(c), "extends PersistentMBean");
        check(ModelMBeanNotificationBroadcaster.class.isAssignableFrom(c),
                "extends ModelMBeanNotificationBroadcaster");
        Method[] mm = c.getMethods();
        check(has(mm, "setModelMBeanInfo"), "has setModelMBeanInfo");
        check(has(mm, "setManagedResource"), "has setManagedResource");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Standard implementation proves the interface is usable.
        ModelMBean mb = new RequiredModelMBean();

        ModelMBeanInfoSupport info = new ModelMBeanInfoSupport(
                "MyMBean", "desc",
                new javax.management.modelmbean.ModelMBeanAttributeInfo[0],
                new javax.management.modelmbean.ModelMBeanConstructorInfo[0],
                new javax.management.modelmbean.ModelMBeanOperationInfo[0],
                new javax.management.modelmbean.ModelMBeanNotificationInfo[0]);
        mb.setModelMBeanInfo(info);
        check("MyMBean".equals(mb.getMBeanInfo().getClassName()),
                "setModelMBeanInfo reflected by getMBeanInfo");

        mb.setManagedResource(new Object(), "objectReference");
        check(true, "setManagedResource objectReference callable");

        boolean iae = false;
        try {
            mb.setManagedResource(new Object(), "unsupportedType");
        } catch (InvalidTargetObjectTypeException e) {
            iae = true;
        }
        check(iae, "setManagedResource unsupported type throws InvalidTargetObjectTypeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
