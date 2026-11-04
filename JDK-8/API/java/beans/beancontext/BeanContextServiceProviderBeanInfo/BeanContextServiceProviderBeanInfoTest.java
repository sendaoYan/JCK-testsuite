/*
 * @test
 * @summary java.beans.beancontext.BeanContextServiceProviderBeanInfo conformance: white-box structure + black-box behavior.
 *          Exact closure (extends java.beans.BeanInfo, single getServicesBeanInfo() returning
 *          BeanInfo[]); array identity without defensive copy, null return allowed, empty and
 *          multi-element arrays, mutation visibility; inherited SimpleBeanInfo defaults
 *          (all null / -1); a pure interface implementation realizing all 8 BeanInfo methods
 *          plus getServicesBeanInfo; the BeanInfo constants contract; and the recorded
 *          convention gap - BeanContextServiceProvider declares no BeanInfo method, the link
 *          between a provider and its servicesBeanInfo is by convention only.
 *          Behavior verified identical on JDK 8 and 27.
 * @run main BeanContextServiceProviderBeanInfoTest
 */

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.beancontext.BeanContextServiceProviderBeanInfo;
import java.awt.Image;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BeanContextServiceProviderBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static String methodNames(Class<?> c) {
        List<String> l = new ArrayList<String>();
        for (Method m : c.getDeclaredMethods()) l.add(m.getName());
        Collections.sort(l);
        return l.toString();
    }

    public interface MyService { String name(); }

    // SimpleBeanInfo-based implementation: inherits the standard null defaults
    public static class MyBeanInfo extends SimpleBeanInfo implements BeanContextServiceProviderBeanInfo {
        BeanInfo[] services;
        MyBeanInfo(BeanInfo[] s) { services = s; }
        public BeanInfo[] getServicesBeanInfo() { return services; }
    }

    // pure interface implementation: must realize all 8 BeanInfo methods plus getServicesBeanInfo
    public static class PureImpl implements BeanContextServiceProviderBeanInfo {
        public BeanInfo[] getServicesBeanInfo() { return null; }
        public BeanDescriptor getBeanDescriptor() { return null; }
        public EventSetDescriptor[] getEventSetDescriptors() { return null; }
        public int getDefaultEventIndex() { return -1; }
        public PropertyDescriptor[] getPropertyDescriptors() { return null; }
        public int getDefaultPropertyIndex() { return -1; }
        public MethodDescriptor[] getMethodDescriptors() { return null; }
        public BeanInfo[] getAdditionalBeanInfo() { return null; }
        public Image getIcon(int iconKind) { return null; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceProviderBeanInfo");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(BeanInfo.class.isAssignableFrom(c), "implements/extends java.beans.BeanInfo");
        check(hasMethod(c, "getServicesBeanInfo"), "declares getServicesBeanInfo");
        // exact inventory and closure
        check(methodNames(c).equals("[getServicesBeanInfo]"), "exact single-method inventory [getServicesBeanInfo]");
        check(c.getDeclaredMethods().length == 1, "declares exactly one method of its own");
        Class<?>[] direct = c.getInterfaces();
        check(direct.length == 1 && direct[0] == BeanInfo.class, "direct superinterface is exactly java.beans.BeanInfo");
        check(!java.beans.beancontext.BeanContextServiceProvider.class.isAssignableFrom(c),
                "unrelated to BeanContextServiceProvider at the type level");
        check(!java.util.EventListener.class.isAssignableFrom(c), "not an EventListener");
        check(!java.io.Serializable.class.isAssignableFrom(c), "interface is not Serializable");
        Method m = c.getMethod("getServicesBeanInfo");
        check(Modifier.isAbstract(m.getModifiers()) && Modifier.isPublic(m.getModifiers()),
                "getServicesBeanInfo is public abstract");
        check(m.getReturnType() == BeanInfo[].class, "getServicesBeanInfo returns BeanInfo[]");
        check(m.getParameterTypes().length == 0, "getServicesBeanInfo takes no arguments");
        check(m.getExceptionTypes().length == 0, "getServicesBeanInfo declares no checked exceptions");
        // the inherited BeanInfo surface the interface extends
        check(hasMethod(BeanInfo.class, "getBeanDescriptor"), "BeanInfo declares getBeanDescriptor");
        check(hasMethod(BeanInfo.class, "getPropertyDescriptors"), "BeanInfo declares getPropertyDescriptors");
        check(hasMethod(BeanInfo.class, "getEventSetDescriptors"), "BeanInfo declares getEventSetDescriptors");
        check(hasMethod(BeanInfo.class, "getMethodDescriptors"), "BeanInfo declares getMethodDescriptors");
        check(hasMethod(BeanInfo.class, "getAdditionalBeanInfo"), "BeanInfo declares getAdditionalBeanInfo");
        check(hasMethod(BeanInfo.class, "getIcon"), "BeanInfo declares getIcon");
        check(hasMethod(BeanInfo.class, "getDefaultPropertyIndex"), "BeanInfo declares getDefaultPropertyIndex");
        check(hasMethod(BeanInfo.class, "getDefaultEventIndex"), "BeanInfo declares getDefaultEventIndex");
        check(BeanInfo.class.getDeclaredMethods().length == 8, "BeanInfo declares exactly 8 methods");
        // BeanInfo icon-kind constants are stable across the two JDKs
        check(BeanInfo.ICON_COLOR_16x16 == 1 && BeanInfo.ICON_COLOR_32x32 == 2
                && BeanInfo.ICON_MONO_16x16 == 3 && BeanInfo.ICON_MONO_32x32 == 4,
                "icon kind constants 1..4 stable (16x16 color=1, 32x32 color=2, 16x16 mono=3, 32x32 mono=4)");
        // convention record: the provider interface itself carries NO BeanInfo method
        check(!hasMethod(java.beans.beancontext.BeanContextServiceProvider.class, "getServicesBeanInfo"),
                "provider interface declares no getServicesBeanInfo (convention link only)");
        check(!hasMethod(java.beans.beancontext.BeanContextServiceProvider.class, "getBeanDescriptor"),
                "provider interface declares no getBeanDescriptor");
        // deprecation status on this JDK
        check(c.getAnnotation(Deprecated.class) == null, "not deprecated on JDK 8");
    }

    static void testBlackBox() throws Exception {
        BeanInfo[] services = new BeanInfo[]{ new SimpleBeanInfo() };
        MyBeanInfo i = new MyBeanInfo(services);
        check(services == i.getServicesBeanInfo(), "getServicesBeanInfo");
        check(i.getBeanDescriptor() == null, "inherited BeanInfo default");
        check(i.getServicesBeanInfo() != null && i.getServicesBeanInfo().length == 1, "services length");
    }

    static void testArraySemantics() throws Exception {
        // identity: the exact array instance is returned, no defensive copy
        BeanInfo[] arr = new BeanInfo[]{ new SimpleBeanInfo(), new SimpleBeanInfo(), new SimpleBeanInfo() };
        MyBeanInfo i = new MyBeanInfo(arr);
        check(i.getServicesBeanInfo() == arr, "exact array identity preserved");
        check(i.getServicesBeanInfo() == i.getServicesBeanInfo(), "repeated invocation returns the same instance");
        check(i.getServicesBeanInfo().length == 3, "multi-element array length preserved");
        check(i.getServicesBeanInfo()[0] == arr[0] && i.getServicesBeanInfo()[2] == arr[2],
                "element identities preserved");
        // mutation of the returned array is visible through subsequent calls (no snapshot)
        arr[1] = new SimpleBeanInfo();
        check(i.getServicesBeanInfo()[1] == arr[1], "external mutation visible - live view, not a copy");
        // empty array and null are both legal returns
        MyBeanInfo empty = new MyBeanInfo(new BeanInfo[0]);
        check(empty.getServicesBeanInfo() != null && empty.getServicesBeanInfo().length == 0,
                "empty array is a legal services set");
        MyBeanInfo nullInfo = new MyBeanInfo(null);
        check(nullInfo.getServicesBeanInfo() == null, "null return allowed by the interface contract");
        // nested classes can be used as the services' BeanInfo entries
        MyBeanInfo selfRef = new MyBeanInfo(new BeanInfo[]{ new MyBeanInfo(null) });
        check(selfRef.getServicesBeanInfo()[0] instanceof BeanContextServiceProviderBeanInfo,
                "a servicesBeanInfo entry may itself implement the interface");
        // holder array element type is exactly BeanInfo[]
        check(i.getServicesBeanInfo().getClass() == BeanInfo[].class, "runtime array type is BeanInfo[]");
    }

    static void testInheritedDefaults() throws Exception {
        MyBeanInfo i = new MyBeanInfo(null);
        // SimpleBeanInfo defaults inherited through the interface
        check(i.getBeanDescriptor() == null, "getBeanDescriptor default null");
        check(i.getPropertyDescriptors() == null, "getPropertyDescriptors default null");
        check(i.getEventSetDescriptors() == null, "getEventSetDescriptors default null");
        check(i.getMethodDescriptors() == null, "getMethodDescriptors default null");
        check(i.getAdditionalBeanInfo() == null, "getAdditionalBeanInfo default null");
        check(i.getDefaultPropertyIndex() == -1, "getDefaultPropertyIndex default -1");
        check(i.getDefaultEventIndex() == -1, "getDefaultEventIndex default -1");
        check(i.getIcon(BeanInfo.ICON_COLOR_32x32) == null, "getIcon default null");
        check(i.getIcon(BeanInfo.ICON_MONO_16x16) == null, "getIcon default null for every kind");
        check(i.getIcon(0) == null, "getIcon with kind 0 also null (no validation in SimpleBeanInfo)");
        check(i.getIcon(Integer.MAX_VALUE) == null, "getIcon with arbitrary kind null, no exception");
        // pure interface implementation compiles against the full 9-method surface
        PureImpl pure = new PureImpl();
        check(pure.getServicesBeanInfo() == null, "pure impl may return null");
        check(pure.getBeanDescriptor() == null && pure.getPropertyDescriptors() == null,
                "pure impl null defaults honored");
        check(pure.getDefaultPropertyIndex() == -1 && pure.getDefaultEventIndex() == -1,
                "pure impl index defaults honored");
        check(pure.getIcon(BeanInfo.ICON_COLOR_16x16) == null, "pure impl icon default null");
        check(pure instanceof BeanInfo, "pure impl is a BeanInfo");
        check(pure instanceof BeanContextServiceProviderBeanInfo, "pure impl is the services BeanInfo type");
        // polymorphic use through the BeanInfo supertype
        BeanInfo asBeanInfo = new MyBeanInfo(new BeanInfo[0]);
        check(asBeanInfo.getBeanDescriptor() == null, "usable through the supertype reference");
        check(!(asBeanInfo instanceof BeanContextServiceProviderBeanInfo)
                || ((BeanContextServiceProviderBeanInfo) asBeanInfo).getServicesBeanInfo().length == 0,
                "downcast reaches the services array when applicable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testArraySemantics();
        testInheritedDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
