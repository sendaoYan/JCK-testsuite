/*
 * @test
 * @summary java.beans.BeanDescriptor conformance: bean class + customizer class.
 * @run main BeanDescriptorTest
 */

import java.beans.*;

public class BeanDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class MyBean {}

    public static void main(String[] args) throws Exception {
        BeanDescriptor bd = new BeanDescriptor(MyBean.class);
        check(bd.getBeanClass() == MyBean.class, "bean class");
        check(bd.getCustomizerClass() == null, "customizer class null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
