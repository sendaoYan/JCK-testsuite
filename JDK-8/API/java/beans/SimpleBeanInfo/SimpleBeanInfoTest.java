/*
 * @test
 * @summary java.beans.SimpleBeanInfo conformance: default null/empty
 *          accessors for every BeanInfo method, getIcon null matrix (all
 *          four icon constants plus out-of-range and negative codes),
 *          loadImage never throws for null/missing/empty resource names
 *          (returns null, probe), public no-arg constructor, and subclass
 *          overriding accessors returns exactly what the override provides
 *          while inheriting the SimpleBeanInfo defaults for the rest.
 * @run main SimpleBeanInfoTest
 */

import java.beans.*;
public class SimpleBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class CustomSbi extends SimpleBeanInfo {
        @Override public PropertyDescriptor[] getPropertyDescriptors() {
            try {
                return new PropertyDescriptor[] { new PropertyDescriptor("custom", CustomSbi.class, null, null) };
            } catch (IntrospectionException e) {
                throw new IllegalStateException(e);
            }
        }
        @Override public BeanDescriptor getBeanDescriptor() { return new BeanDescriptor(CustomSbi.class); }
        @Override public int getDefaultPropertyIndex() { return 5; }
    }
    public static class CustomEvents extends SimpleBeanInfo {
        @Override public EventSetDescriptor[] getEventSetDescriptors() {
            return new EventSetDescriptor[0];
        }
        @Override public BeanInfo[] getAdditionalBeanInfo() { return new BeanInfo[0]; }
    }

    // existing checks (kept verbatim)
    static void testDefaults() {
        SimpleBeanInfo bi = new SimpleBeanInfo();
        check(bi.getBeanDescriptor() == null, "bean descriptor null");
        check(bi.getPropertyDescriptors() == null, "property descriptors null");
        check(bi.getMethodDescriptors() == null, "method descriptors null");
        check(bi.getDefaultPropertyIndex() == -1, "default property index");
        check(bi.getDefaultEventIndex() == -1, "default event index");
    }

    static void testStructure() throws Exception {
        check(SimpleBeanInfo.class.getConstructor() != null, "public no-arg constructor");
        check(BeanInfo.class.isAssignableFrom(SimpleBeanInfo.class), "implements BeanInfo");
        check(!java.lang.reflect.Modifier.isAbstract(SimpleBeanInfo.class.getModifiers()), "not abstract");
        check(SimpleBeanInfo.class.getConstructors().length == 1, "single public constructor");
    }

    static void testIconMatrix() {
        SimpleBeanInfo bi = new SimpleBeanInfo();
        check(bi.getIcon(BeanInfo.ICON_COLOR_16x16) == null, "color 16x16 icon null");
        check(bi.getIcon(BeanInfo.ICON_COLOR_32x32) == null, "color 32x32 icon null");
        check(bi.getIcon(BeanInfo.ICON_MONO_16x16) == null, "mono 16x16 icon null");
        check(bi.getIcon(BeanInfo.ICON_MONO_32x32) == null, "mono 32x32 icon null");
        check(bi.getIcon(0) == null, "icon code 0 null");
        check(bi.getIcon(99) == null, "icon code 99 null");
        check(bi.getIcon(-5) == null, "negative icon code null");
        check(bi.getIcon(Integer.MIN_VALUE) == null, "Integer.MIN_VALUE icon code null");
        check(bi.getAdditionalBeanInfo() == null, "additional bean info null");
        check(bi.getEventSetDescriptors() == null, "event set descriptors null");
    }

    static void testLoadImage() {
        SimpleBeanInfo bi = new SimpleBeanInfo();
        check(bi.loadImage(null) == null, "loadImage(null) returns null (probe, no throw)");
        check(bi.loadImage("no-such.png") == null, "loadImage(missing) returns null");
        check(bi.loadImage("") == null, "loadImage(empty) returns null");
    }

    static void testSubclass() {
        CustomSbi c = new CustomSbi();
        PropertyDescriptor[] pds = c.getPropertyDescriptors();
        check(pds != null && pds.length == 1, "subclass override honored: one descriptor");
        check("custom".equals(pds[0].getName()), "subclass override descriptor name");
        check(c.getBeanDescriptor() != null
                && c.getBeanDescriptor().getBeanClass() == CustomSbi.class, "overridden bean descriptor");
        check(c.getDefaultPropertyIndex() == 5, "overridden default property index");
        check(c.getMethodDescriptors() == null, "non-overridden accessors keep null default");
        check(c.getIcon(BeanInfo.ICON_COLOR_16x16) == null, "inherited getIcon null in subclass");
        check(c.getAdditionalBeanInfo() == null, "inherited additional info null in subclass");

        CustomEvents e = new CustomEvents();
        check(e.getEventSetDescriptors() != null && e.getEventSetDescriptors().length == 0,
                "overridden event sets empty array");
        check(e.getAdditionalBeanInfo() != null && e.getAdditionalBeanInfo().length == 0,
                "overridden additional info empty array");
        check(e.getPropertyDescriptors() == null, "other accessors still null");

        // two base instances behave identically
        SimpleBeanInfo a = new SimpleBeanInfo();
        SimpleBeanInfo b = new SimpleBeanInfo();
        check(a.getBeanDescriptor() == b.getBeanDescriptor(), "independent instances both null defaults");
        // BeanInfo-typed reference sees the same contract
        BeanInfo asBeanInfo = a;
        check(asBeanInfo.getIcon(BeanInfo.ICON_MONO_32x32) == null, "via BeanInfo reference icon null");
        check(asBeanInfo.getDefaultEventIndex() == -1, "via BeanInfo reference index -1");
    }

    public static void main(String[] args) throws Exception {
        testDefaults();
        testStructure();
        testIconMatrix();
        testLoadImage();
        testSubclass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
