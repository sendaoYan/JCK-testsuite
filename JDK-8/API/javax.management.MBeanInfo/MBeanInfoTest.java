/*
 * @test
 * @summary javax.management.MBeanInfo conformance: construction, accessors, clone.
 * @run main MBeanInfoTest
 */

import javax.management.*;
public class MBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MBeanAttributeInfo ai = new MBeanAttributeInfo("Count", "int", "count", true, false, false);
        MBeanConstructorInfo ci = new MBeanConstructorInfo("ctor", "desc", new MBeanParameterInfo[0]);
        MBeanOperationInfo oi = new MBeanOperationInfo("op", "desc", new MBeanParameterInfo[0], "void", MBeanOperationInfo.INFO);
        MBeanNotificationInfo ni = new MBeanNotificationInfo(new String[]{"a.b"}, "note", "desc");
        MBeanInfo info = new MBeanInfo("com.example.Foo", "desc",
            new MBeanAttributeInfo[]{ai}, new MBeanConstructorInfo[]{ci},
            new MBeanOperationInfo[]{oi}, new MBeanNotificationInfo[]{ni});
        check("com.example.Foo".equals(info.getClassName()), "class name");
        check("desc".equals(info.getDescription()), "description");
        check(info.getAttributes().length == 1 && "int".equals(info.getAttributes()[0].getType()), "attributes");
        check(info.getOperations().length == 1 && "void".equals(info.getOperations()[0].getReturnType()), "operations");
        check(info.getConstructors().length == 1, "constructors");
        check(info.getNotifications().length == 1 && info.getNotifications()[0].getNotifTypes().length == 1, "notifications");
        MBeanInfo copy = (MBeanInfo) info.clone();
        check(info.equals(copy), "clone equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
