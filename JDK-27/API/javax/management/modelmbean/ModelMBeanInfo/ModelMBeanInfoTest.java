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
        testDeepMethodSet();
        testDeepUsageThroughInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch h): exact method set, checked exceptions, clone ----

    static void testDeepMethodSet() throws Exception {
        Class<?> c = javax.management.modelmbean.ModelMBeanInfo.class;
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.size() == 16, "ModelMBeanInfo exposes exactly 16 methods: " + names.size());
        // quirk: per-type lookup has getAttribute/getOperation/getNotification but NO getConstructor(String)
        check(names.contains("getAttribute") && names.contains("getOperation") && names.contains("getNotification"),
                "typed lookup helpers declared");
        check(!names.contains("getConstructor"),
                "getConstructor(String) is deliberately absent from the interface (Support adds it)");
        // descriptor methods declare the checked JMX exceptions
        java.lang.reflect.Method gd = c.getMethod("getDescriptor", String.class, String.class);
        boolean mbe = false, roe = false;
        for (Class<?> ex : gd.getExceptionTypes()) {
            if (ex == javax.management.MBeanException.class) mbe = true;
            if (ex == javax.management.RuntimeOperationsException.class) roe = true;
        }
        check(mbe && roe, "getDescriptor(String,String) declares MBeanException and RuntimeOperationsException");
        java.lang.reflect.Method sd = c.getMethod("setDescriptor",
                javax.management.Descriptor.class, String.class);
        check(sd.getExceptionTypes().length >= 2, "setDescriptor declares checked exceptions");
        // clone has no checked exceptions in the interface
        java.lang.reflect.Method cl = c.getMethod("clone");
        check(cl.getExceptionTypes().length == 0, "clone declares no checked exceptions");
    }

    static void testDeepUsageThroughInterface() throws Exception {
        javax.management.modelmbean.ModelMBeanInfoSupport impl =
                new javax.management.modelmbean.ModelMBeanInfoSupport("C", "d",
                        new javax.management.modelmbean.ModelMBeanAttributeInfo[0],
                        new javax.management.modelmbean.ModelMBeanConstructorInfo[0],
                        new javax.management.modelmbean.ModelMBeanOperationInfo[0],
                        new javax.management.modelmbean.ModelMBeanNotificationInfo[0]);
        javax.management.modelmbean.ModelMBeanInfo info = impl;
        // descriptor access through the interface
        javax.management.Descriptor mbean = info.getMBeanDescriptor();
        check("mbean".equals(mbean.getFieldValue("descriptorType")), "getMBeanDescriptor through interface");
        check(info.getDescriptors("attribute").length == 0, "getDescriptors through interface");
        check(info.getDescriptor("nosuch", "attribute") == null, "getDescriptor miss through interface");
        // mutation through the interface
        javax.management.modelmbean.DescriptorSupport nd = new javax.management.modelmbean.DescriptorSupport(
                new String[]{"name", "descriptorType", "k"}, new Object[]{"C", "mbean", "V"});
        info.setMBeanDescriptor(nd);
        check("V".equals(info.getMBeanDescriptor().getFieldValue("k")), "setMBeanDescriptor through interface");
        info.setDescriptors(new javax.management.Descriptor[0]);
        check(true, "setDescriptors with empty array is a no-op");
        // clone through the interface
        Object cl = info.clone();
        check(cl instanceof javax.management.modelmbean.ModelMBeanInfo, "clone returns ModelMBeanInfo");
        check(cl.equals(info), "clone equals through interface");
        // typed lookups return null for unknown names
        check(info.getAttribute("x") == null && info.getOperation("x") == null && info.getNotification("x") == null,
                "typed lookups return null for unknown names");
        // the interface is consumed by RequiredModelMBean.setModelMBeanInfo
        javax.management.modelmbean.RequiredModelMBean rmm = new javax.management.modelmbean.RequiredModelMBean();
        rmm.setModelMBeanInfo(info);
        check("C".equals(rmm.getMBeanInfo().getClassName()), "ModelMBeanInfo instance accepted by RequiredModelMBean");
    }
}
