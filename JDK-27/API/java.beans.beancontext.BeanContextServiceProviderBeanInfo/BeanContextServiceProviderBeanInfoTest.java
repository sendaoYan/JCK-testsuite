/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextServiceProviderBeanInfo conformance: white-box structure + black-box behavior.
 * @run main BeanContextServiceProviderBeanInfoTest
 */

import java.beans.BeanInfo;
import java.beans.SimpleBeanInfo;
import java.beans.beancontext.BeanContextServiceProviderBeanInfo;

public class BeanContextServiceProviderBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class MyBeanInfo extends SimpleBeanInfo implements BeanContextServiceProviderBeanInfo {
        BeanInfo[] services;
        MyBeanInfo(BeanInfo[] s) { services = s; }
        public BeanInfo[] getServicesBeanInfo() { return services; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceProviderBeanInfo");
        check(c.isInterface(), "is an interface");
        check(BeanInfo.class.isAssignableFrom(c), "extends BeanInfo");
    }

    static void testBlackBox() throws Exception {
        BeanInfo[] services = new BeanInfo[]{ new SimpleBeanInfo() };
        MyBeanInfo i = new MyBeanInfo(services);
        check(services == i.getServicesBeanInfo(), "getServicesBeanInfo");
        check(i.getBeanDescriptor() == null, "inherited BeanInfo default");
        check(i.getServicesBeanInfo() != null && i.getServicesBeanInfo().length == 1, "services length");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
