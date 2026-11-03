/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanInfo conformance: white-box structure + black-box behavior.
 * @run main ModelMBeanInfoTest
 */

import java.lang.reflect.Method;
import javax.management.Descriptor;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;

public class ModelMBeanInfoTest {
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
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanInfo");
        check(c.isInterface(), "is interface");
        Method[] mm = c.getMethods();
        check(has(mm, "getDescriptors"), "has getDescriptors");
        check(has(mm, "setDescriptors"), "has setDescriptors");
        check(has(mm, "getDescriptor"), "has getDescriptor");
        check(has(mm, "setDescriptor"), "has setDescriptor");
        check(has(mm, "getMBeanDescriptor"), "has getMBeanDescriptor");
        check(has(mm, "setMBeanDescriptor"), "has setMBeanDescriptor");
        check(has(mm, "getAttribute"), "has getAttribute");
        check(has(mm, "getOperation"), "has getOperation");
        check(has(mm, "getNotification"), "has getNotification");
        check(has(mm, "getClassName"), "has getClassName");
        check(has(mm, "getDescription"), "has getDescription");
        check(has(mm, "getAttributes"), "has getAttributes");
        check(has(mm, "getConstructors"), "has getConstructors");
        check(has(mm, "getOperations"), "has getOperations");
        check(has(mm, "getNotifications"), "has getNotifications");
        check(has(mm, "clone"), "has clone");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Prove the interface is callable through its standard implementation.
        ModelMBeanInfo info = new ModelMBeanInfoSupport(
                "Test", "desc",
                new javax.management.modelmbean.ModelMBeanAttributeInfo[0],
                new javax.management.modelmbean.ModelMBeanConstructorInfo[0],
                new javax.management.modelmbean.ModelMBeanOperationInfo[0],
                new javax.management.modelmbean.ModelMBeanNotificationInfo[0]);
        check("Test".equals(info.getClassName()), "getClassName");
        check("desc".equals(info.getDescription()), "getDescription");
        check(info.getAttributes().length == 0, "getAttributes");
        check(info.getConstructors().length == 0, "getConstructors");
        check(info.getOperations().length == 0, "getOperations");
        check(info.getNotifications().length == 0, "getNotifications");
        Descriptor md = info.getMBeanDescriptor();
        check(md != null, "getMBeanDescriptor non-null");
        check("Test".equals(md.getFieldValue("name")), "mbean descriptor name");
        check("mbean".equals(md.getFieldValue("descriptorType")), "mbean descriptor type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
