/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanInfoSupport conformance: white-box structure + black-box behavior.
 * @run main ModelMBeanInfoSupportTest
 */

import javax.management.Descriptor;
import javax.management.MBeanInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanOperationInfo;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.DescriptorSupport;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

public class ModelMBeanInfoSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanInfoSupport");
        check(MBeanInfo.class.isAssignableFrom(c), "extends MBeanInfo");
        check(ModelMBeanInfo.class.isAssignableFrom(c), "implements ModelMBeanInfo");
        check(c.getConstructors().length >= 3, "multiple public constructors");
    }

    static ModelMBeanInfoSupport buildInfo() {
        ModelMBeanAttributeInfo attr = new ModelMBeanAttributeInfo(
                "Name", "java.lang.String", "attr desc", true, true, false);
        ModelMBeanConstructorInfo ctor = new ModelMBeanConstructorInfo(
                "Test", "ctor desc", new MBeanParameterInfo[0]);
        ModelMBeanOperationInfo oper = new ModelMBeanOperationInfo(
                "op", "oper desc", new MBeanParameterInfo[0], "void", MBeanOperationInfo.ACTION);
        ModelMBeanNotificationInfo notif = new ModelMBeanNotificationInfo(
                new String[]{"test.notif"}, "n", "notif desc");
        return new ModelMBeanInfoSupport(
                "Test", "desc",
                new ModelMBeanAttributeInfo[]{attr},
                new ModelMBeanConstructorInfo[]{ctor},
                new ModelMBeanOperationInfo[]{oper},
                new ModelMBeanNotificationInfo[]{notif});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ModelMBeanInfoSupport info = buildInfo();

        // Basic MBeanInfo contract
        check("Test".equals(info.getClassName()), "className");
        check("desc".equals(info.getDescription()), "description");
        check(info.getAttributes().length == 1, "attributes length");
        check(info.getConstructors().length == 1, "constructors length");
        check(info.getOperations().length == 1, "operations length");
        check(info.getNotifications().length == 1, "notifications length");
        check("Name".equals(info.getAttributes()[0].getName()), "attribute content");
        check("op".equals(info.getOperations()[0].getName()), "operation content");

        // MBean descriptor defaults
        Descriptor md = info.getMBeanDescriptor();
        check(md != null, "getMBeanDescriptor non-null");
        check("Test".equals(md.getFieldValue("name")), "mbean descriptor name");
        check("mbean".equals(md.getFieldValue("descriptorType")), "mbean descriptor type");
        check("Test".equals(md.getFieldValue("displayName")), "mbean descriptor displayName");
        check("never".equals(md.getFieldValue("persistPolicy")), "mbean descriptor persistPolicy");

        // getDescriptor() (no-arg, inherited) is the MBean descriptor
        Descriptor noArg = info.getDescriptor();
        check("Test".equals(noArg.getFieldValue("name")), "no-arg getDescriptor name");

        // typed getDescriptor
        check("Name".equals(info.getDescriptor("Name", "attribute").getFieldValue("name")),
                "getDescriptor attribute");
        check("op".equals(info.getDescriptor("op", "operation").getFieldValue("name")),
                "getDescriptor operation");
        check("Test".equals(info.getDescriptor("Test", "constructor").getFieldValue("name")),
                "getDescriptor constructor");
        check("n".equals(info.getDescriptor("n", "notification").getFieldValue("name")),
                "getDescriptor notification");
        check("Test".equals(info.getDescriptor("Test", "mbean").getFieldValue("name")),
                "getDescriptor mbean");

        // single-arg getDescriptor searches across types
        check("Name".equals(info.getDescriptor("Name").getFieldValue("name")),
                "single-arg getDescriptor");

        // getDescriptors("attribute")
        Descriptor[] attrs = info.getDescriptors("attribute");
        check(attrs.length == 1 && "Name".equals(attrs[0].getFieldValue("name")),
                "getDescriptors attribute array");
        Descriptor[] all = info.getDescriptors(null);
        check(all.length >= 5, "getDescriptors(null) returns all");

        // getAttribute/getOperation/getConstructor/getNotification
        check(info.getAttribute("Name") != null, "getAttribute");
        check(info.getOperation("op") != null, "getOperation");
        check(info.getConstructor("Test") != null, "getConstructor");
        check(info.getNotification("n") != null, "getNotification");
        check(info.getAttribute("Missing") == null, "getAttribute missing returns null");

        // setMBeanDescriptor
        DescriptorSupport newMd = new DescriptorSupport(
                "name=Test", "descriptorType=mbean", "extra=v");
        info.setMBeanDescriptor(newMd);
        check("v".equals(info.getMBeanDescriptor().getFieldValue("extra")), "setMBeanDescriptor");

        // setDescriptor for attribute
        DescriptorSupport newAttr = new DescriptorSupport(
                "name=Name", "descriptorType=attribute", "extra=w");
        info.setDescriptor(newAttr, "attribute");
        check("w".equals(info.getDescriptor("Name", "attribute").getFieldValue("extra")),
                "setDescriptor attribute updates");

        // clone is independent shallow clone
        ModelMBeanInfoSupport clone = (ModelMBeanInfoSupport) info.clone();
        check("Test".equals(clone.getClassName()), "clone className");
        check(clone.getMBeanDescriptor() != null, "clone mbean descriptor");
        clone.setMBeanDescriptor(new DescriptorSupport("name=Test", "descriptorType=mbean", "extra=changed"));
        check(!"changed".equals(info.getMBeanDescriptor().getFieldValue("extra")),
                "original unaffected by clone mbean descriptor mutation");

        // copy constructor from ModelMBeanInfo
        ModelMBeanInfoSupport copied = new ModelMBeanInfoSupport((ModelMBeanInfo) info);
        check("Test".equals(copied.getClassName()), "copy ctor className");

        // exceptions
        boolean re1 = false;
        try { info.getDescriptor((String) null); } catch (RuntimeOperationsException e) { re1 = true; }
        check(re1, "getDescriptor(null) throws RuntimeOperationsException");

        boolean re2 = false;
        try { info.setDescriptor(new DescriptorSupport(), "bogusType"); } catch (RuntimeOperationsException e) { re2 = true; }
        check(re2, "setDescriptor invalid type throws RuntimeOperationsException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
