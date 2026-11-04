/*
 * @test
 * @summary java.beans.BeanInfo conformance: interface reflection plus
 *          real introspected BeanInfo behavior. Icon constant exact values,
 *          complete 8-method interface inventory via reflection with exact
 *          return types, and behavioral contract of an Introspector-produced
 *          BeanInfo on a plain bean: default indexes -1, icons null for all
 *          constants and out-of-range codes, additional info null, empty
 *          (non-null) event set array, method descriptor coverage of every
 *          public method (find-by-name), MethodDescriptor.getMethod()
 *          round-trip, and non-defensive internal arrays (probe, restored).
 * @run main BeanInfoTest
 */

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class SampleBean {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int compute(int x) { return x * 2; }
    }

    // existing checks (kept verbatim in behavior)
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

    // deepening
    static void testIconConstants() {
        check(BeanInfo.ICON_COLOR_16x16 == 1, "ICON_COLOR_16x16 == 1");
        check(BeanInfo.ICON_COLOR_32x32 == 2, "ICON_COLOR_32x32 == 2");
        check(BeanInfo.ICON_MONO_16x16 == 3, "ICON_MONO_16x16 == 3");
        check(BeanInfo.ICON_MONO_32x32 == 4, "ICON_MONO_32x32 == 4");
    }

    static void testInterfaceInventory() throws Exception {
        Class<?> c = BeanInfo.class;
        String[] expected = { "getAdditionalBeanInfo", "getBeanDescriptor", "getDefaultEventIndex",
                              "getDefaultPropertyIndex", "getEventSetDescriptors", "getIcon",
                              "getMethodDescriptors", "getPropertyDescriptors" };
        for (String name : expected) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) found = true;
            check(found, "interface declares " + name);
        }
        List<String> distinct = new ArrayList<String>();
        for (Method m : c.getMethods()) {
            if (!distinct.contains(m.getName())) distinct.add(m.getName());
        }
        check(distinct.size() == expected.length, "exactly the 8 documented public methods");
        // exact return types
        check(c.getMethod("getAdditionalBeanInfo").getReturnType() == BeanInfo[].class,
                "getAdditionalBeanInfo returns BeanInfo[]");
        check(c.getMethod("getBeanDescriptor").getReturnType() == BeanDescriptor.class,
                "getBeanDescriptor returns BeanDescriptor");
        check(c.getMethod("getDefaultEventIndex").getReturnType() == int.class,
                "getDefaultEventIndex returns int");
        check(c.getMethod("getDefaultPropertyIndex").getReturnType() == int.class,
                "getDefaultPropertyIndex returns int");
        check(c.getMethod("getEventSetDescriptors").getReturnType() == EventSetDescriptor[].class,
                "getEventSetDescriptors returns EventSetDescriptor[]");
        check(c.getMethod("getIcon", int.class).getReturnType() == java.awt.Image.class,
                "getIcon returns java.awt.Image");
        check(c.getMethod("getMethodDescriptors").getReturnType() == MethodDescriptor[].class,
                "getMethodDescriptors returns MethodDescriptor[]");
        check(c.getMethod("getPropertyDescriptors").getReturnType() == PropertyDescriptor[].class,
                "getPropertyDescriptors returns PropertyDescriptor[]");
    }

    static PropertyDescriptor findProp(BeanInfo bi, String name) {
        for (PropertyDescriptor pd : bi.getPropertyDescriptors()) if (name.equals(pd.getName())) return pd;
        return null;
    }
    static MethodDescriptor findMethod(BeanInfo bi, String name) {
        for (MethodDescriptor md : bi.getMethodDescriptors()) if (name.equals(md.getName())) return md;
        return null;
    }

    static void testRealBeanInfoBehavior() throws Exception {
        Introspector.flushCaches();
        BeanInfo bi = Introspector.getBeanInfo(SampleBean.class);

        check(bi.getDefaultPropertyIndex() == -1, "default property index -1 without annotation");
        check(bi.getDefaultEventIndex() == -1, "default event index -1 without annotation");

        check(bi.getIcon(BeanInfo.ICON_COLOR_16x16) == null, "color 16 icon null");
        check(bi.getIcon(BeanInfo.ICON_COLOR_32x32) == null, "color 32 icon null");
        check(bi.getIcon(BeanInfo.ICON_MONO_16x16) == null, "mono 16 icon null");
        check(bi.getIcon(BeanInfo.ICON_MONO_32x32) == null, "mono 32 icon null");
        check(bi.getIcon(0) == null, "out-of-range icon code 0 null");
        check(bi.getIcon(99) == null, "out-of-range icon code 99 null");
        check(bi.getIcon(-1) == null, "negative icon code null");

        check(bi.getAdditionalBeanInfo() == null, "additional BeanInfo null by default");

        EventSetDescriptor[] esds = bi.getEventSetDescriptors();
        check(esds != null, "event set array non-null from Introspector");
        check(esds.length == 0, "plain bean has zero event sets");

        MethodDescriptor[] mds = bi.getMethodDescriptors();
        check(mds != null && mds.length > 0, "method descriptors non-empty");
        check(mds.length == SampleBean.class.getMethods().length,
                "one method descriptor per public method (incl. Object methods)");
        for (Method m : SampleBean.class.getMethods()) {
            check(findMethod(bi, m.getName()) != null, "method descriptor present for " + m.getName());
        }
        MethodDescriptor getName = findMethod(bi, "getName");
        check(getName != null && getName.getMethod() != null, "MethodDescriptor.getMethod non-null");
        check("getName".equals(getName.getMethod().getName()), "getMethod name round-trip");
        check(getName.getMethod().getDeclaringClass() == SampleBean.class, "getMethod declaring class");
        check(findMethod(bi, "compute").getMethod().getParameterTypes().length == 1,
                "compute descriptor keeps parameter info");

        check(findProp(bi, "name") != null, "property descriptors still reachable");
        check(findProp(bi, "name").getReadMethod().getName().equals("getName"), "name read method");

        // returned array is the internal storage, not a defensive copy (probe; restored)
        PropertyDescriptor[] arr1 = bi.getPropertyDescriptors();
        PropertyDescriptor[] arr2 = bi.getPropertyDescriptors();
        check(arr1 == arr2, "getPropertyDescriptors same reference across calls (no copy)");
        PropertyDescriptor saved = arr1[arr1.length - 1];
        arr1[arr1.length - 1] = null;
        check(bi.getPropertyDescriptors()[arr1.length - 1] == null, "mutation visible (shared storage)");
        arr1[arr1.length - 1] = saved;
        MethodDescriptor[] m1 = bi.getMethodDescriptors();
        MethodDescriptor[] m2 = bi.getMethodDescriptors();
        check(m1 == m2, "getMethodDescriptors same reference across calls (no copy)");
        Introspector.flushCaches();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testIconConstants();
        testInterfaceInventory();
        testRealBeanInfoBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
