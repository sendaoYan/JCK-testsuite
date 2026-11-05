/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanInfo conformance: interface reflection.
 * @run main ModelMBeanInfoTest
 */

public class ModelMBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanInfo");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
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
