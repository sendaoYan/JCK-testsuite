/*
 * @test
 * @summary java.beans.PropertyDescriptor conformance: white-box structure + black-box behavior.
 * @run main PropertyDescriptorTest
 */

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class PropertyDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class Bean {
        private int foo; private boolean bar;
        public int getFoo() { return foo; }
        public void setFoo(int v) { foo = v; }
        public boolean isBar() { return bar; }
        public void setBar(boolean b) { bar = b; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.PropertyDescriptor");
        check(java.beans.FeatureDescriptor.class.isAssignableFrom(c), "extends FeatureDescriptor");
    }

    static void testBlackBox() throws Exception {
        PropertyDescriptor pd = new PropertyDescriptor("foo", Bean.class);
        check("foo".equals(pd.getName()), "name");
        check(int.class == pd.getPropertyType(), "propertyType int");
        check(pd.getReadMethod() != null && "getFoo".equals(pd.getReadMethod().getName()), "readMethod");
        check(pd.getWriteMethod() != null && "setFoo".equals(pd.getWriteMethod().getName()), "writeMethod");
        check(!pd.isBound() && !pd.isConstrained(), "bound/constrained default false");
        pd.setBound(true); pd.setConstrained(true);
        check(pd.isBound() && pd.isConstrained(), "bound/constrained set");

        PropertyDescriptor pd2 = new PropertyDescriptor("bar", Bean.class);
        check(boolean.class == pd2.getPropertyType(), "boolean propertyType");
        check(pd2.getReadMethod().getName().equals("isBar"), "is* readMethod");

        Method g = Bean.class.getMethod("getFoo");
        Method s = Bean.class.getMethod("setFoo", int.class);
        PropertyDescriptor pd3 = new PropertyDescriptor("foo", g, s);
        check(int.class == pd3.getPropertyType(), "method-pair ctor");

        pd.setPropertyEditorClass(java.beans.PropertyEditorSupport.class);
        check(java.beans.PropertyEditorSupport.class == pd.getPropertyEditorClass(), "propertyEditorClass");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
