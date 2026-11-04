/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanInfoSupport conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanInfoSupportTest
 */

import java.lang.reflect.Modifier;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfo;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;

public class OpenMBeanInfoSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static OpenMBeanInfoSupport makeInfo() {
        OpenMBeanAttributeInfoSupport ai = new OpenMBeanAttributeInfoSupport("Name", "desc", SimpleType.STRING, true, true, false);
        OpenMBeanConstructorInfoSupport ci = new OpenMBeanConstructorInfoSupport("Test", "desc", new OpenMBeanParameterInfoSupport[0]);
        OpenMBeanOperationInfoSupport oi = new OpenMBeanOperationInfoSupport("op", "desc", new OpenMBeanParameterInfoSupport[0], SimpleType.INTEGER, MBeanOperationInfo.INFO);
        return new OpenMBeanInfoSupport("Test", "desc",
            new OpenMBeanAttributeInfoSupport[]{ai},
            new OpenMBeanConstructorInfoSupport[]{ci},
            new OpenMBeanOperationInfoSupport[]{oi},
            new MBeanNotificationInfo[0]);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanInfoSupport");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(MBeanInfo.class.isAssignableFrom(c), "extends MBeanInfo");
        check(OpenMBeanInfo.class.isAssignableFrom(c), "implements OpenMBeanInfo");
        check(c.getMethod("getAttributes") != null, "has getAttributes");
        check(c.getMethod("getConstructors") != null, "has getConstructors");
        check(c.getMethod("getOperations") != null, "has getOperations");
        check(c.getMethod("getNotifications") != null, "has getNotifications");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        OpenMBeanInfoSupport info = makeInfo();
        check(info.getClassName().equals("Test"), "className");
        check(info.getDescription().equals("desc"), "description");

        MBeanAttributeInfo[] attrs = info.getAttributes();
        check(attrs.length == 1 && attrs[0].getName().equals("Name"), "attributes");
        check(attrs[0] instanceof OpenMBeanAttributeInfoSupport, "attribute concrete type");

        MBeanConstructorInfo[] ctors = info.getConstructors();
        check(ctors.length == 1 && ctors[0].getName().equals("Test"), "constructors");

        MBeanOperationInfo[] ops = info.getOperations();
        check(ops.length == 1 && ops[0].getName().equals("op"), "operations");
        check(((OpenMBeanOperationInfoSupport) ops[0]).getReturnOpenType() == SimpleType.INTEGER, "operation return type");

        check(info.getNotifications().length == 0, "notifications empty");

        // equals / hashCode
        OpenMBeanInfoSupport info2 = makeInfo();
        check(info.equals(info2), "equals");
        check(info.hashCode() == info2.hashCode(), "hashCode equal");
        check(!info.equals(null), "equals null false");
        check(!info.equals("x"), "equals non-info false");

        check(info.toString().startsWith("javax.management.openmbean.OpenMBeanInfoSupport("), "toString prefix");
        check(info.toString().contains("Test"), "toString contains className");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
