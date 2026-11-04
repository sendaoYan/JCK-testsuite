/*
 * @test
 * @summary java.beans.Introspector conformance: white-box structure + black-box behavior.
 * @run main IntrospectorTest
 */

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

public class IntrospectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class Bean {
        private int foo;
        public int getFoo() { return foo; }
        public void setFoo(int v) { foo = v; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Introspector");
        check(!c.isInterface(), "is a class");
    }

    static void testBlackBox() throws Exception {
        check("fooBar".equals(Introspector.decapitalize("FooBar")), "decapitalize FooBar");
        check("URL".equals(Introspector.decapitalize("URL")), "decapitalize acronym");

        BeanInfo info = Introspector.getBeanInfo(Bean.class);
        check(info.getBeanDescriptor() != null && Bean.class == info.getBeanDescriptor().getBeanClass(), "beanDescriptor");
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        boolean found = false;
        for (PropertyDescriptor pd : pds) if ("foo".equals(pd.getName())) found = true;
        check(found, "property foo discovered");

        BeanInfo info2 = Introspector.getBeanInfo(Bean.class, Object.class);
        check(info2.getBeanDescriptor() != null, "stopClass getBeanInfo");

        BeanInfo info3 = Introspector.getBeanInfo(Bean.class, Introspector.USE_ALL_BEANINFO);
        check(info3.getBeanDescriptor() != null, "flags getBeanInfo");

        Introspector.flushCaches();
        Introspector.flushFromCaches(Bean.class);
        check(true, "flushCaches/flushFromCaches no throw");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
