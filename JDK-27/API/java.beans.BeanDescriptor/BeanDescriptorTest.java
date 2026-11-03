/*
 * @test
 * @summary java.beans.BeanDescriptor conformance: white-box structure + black-box behavior.
 * @run main BeanDescriptorTest
 */

import java.beans.BeanDescriptor;

public class BeanDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.BeanDescriptor");
        check(java.beans.FeatureDescriptor.class.isAssignableFrom(c), "extends FeatureDescriptor");
    }

    static void testBlackBox() throws Exception {
        BeanDescriptor bd = new BeanDescriptor(String.class);
        check(String.class == bd.getBeanClass(), "beanClass");
        check(bd.getCustomizerClass() == null, "customizer null default");
        BeanDescriptor bd2 = new BeanDescriptor(String.class, java.beans.PropertyEditorSupport.class);
        check(java.beans.PropertyEditorSupport.class == bd2.getCustomizerClass(), "customizerClass");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
