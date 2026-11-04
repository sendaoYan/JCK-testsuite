/*
 * @test
 * @summary java.beans.BeanInfo conformance: white-box structure + black-box behavior.
 * @run main BeanInfoTest
 */

import java.beans.BeanInfo;

public class BeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.BeanInfo");
        check(c.isInterface(), "is an interface");
        check(BeanInfo.ICON_COLOR_16x16 == 1 && BeanInfo.ICON_COLOR_32x32 == 2 &&
              BeanInfo.ICON_MONO_16x16 == 3 && BeanInfo.ICON_MONO_32x32 == 4, "icon constants");
        boolean rc = false, ec = false, pc = false, mc = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("getBeanDescriptor")) rc = true;
            if (m.getName().equals("getEventSetDescriptors")) ec = true;
            if (m.getName().equals("getPropertyDescriptors")) pc = true;
            if (m.getName().equals("getMethodDescriptors")) mc = true;
        }
        check(rc && ec && pc && mc, "key methods declared");
    }

    static void testBlackBox() throws Exception {
        BeanInfo info = new java.beans.SimpleBeanInfo();
        check(info.getBeanDescriptor() == null, "default beanDescriptor null");
        check(info.getDefaultPropertyIndex() == -1, "defaultPropertyIndex -1");
        check(info.getDefaultEventIndex() == -1, "defaultEventIndex -1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
