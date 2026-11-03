/*
 * @test
 * @summary java.beans.SimpleBeanInfo conformance: white-box structure + black-box behavior.
 * @run main SimpleBeanInfoTest
 */

import java.beans.BeanInfo;
import java.beans.SimpleBeanInfo;

public class SimpleBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.SimpleBeanInfo");
        check(BeanInfo.class.isAssignableFrom(c), "implements BeanInfo");
    }

    static void testBlackBox() throws Exception {
        SimpleBeanInfo info = new SimpleBeanInfo();
        check(info.getBeanDescriptor() == null, "beanDescriptor null");
        check(info.getPropertyDescriptors() == null, "propertyDescriptors null");
        check(info.getEventSetDescriptors() == null, "eventSetDescriptors null");
        check(info.getMethodDescriptors() == null, "methodDescriptors null");
        check(info.getDefaultPropertyIndex() == -1, "defaultPropertyIndex -1");
        check(info.getDefaultEventIndex() == -1, "defaultEventIndex -1");
        check(info.getIcon(BeanInfo.ICON_COLOR_16x16) == null, "icon null");
        check(info.getAdditionalBeanInfo() == null, "additionalBeanInfo null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
