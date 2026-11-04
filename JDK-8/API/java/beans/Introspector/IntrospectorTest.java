/*
 * @test
 * @summary java.beans.Introspector conformance: getBeanInfo and decapitalize.
 * @run main IntrospectorTest
 */

import java.beans.*;

public class IntrospectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class MyBean {
        private String name;
        public String getName() { return name; }
        public void setName(String v) { name = v; }
    }

    public static void main(String[] args) throws Exception {
        BeanInfo bi = Introspector.getBeanInfo(MyBean.class);
        boolean found = false;
        for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
            if ("name".equals(pd.getName())) {
                found = true;
                check(pd.getPropertyType() == String.class, "property type");
            }
        }
        check(found, "name property found");
        check("foo".equals(Introspector.decapitalize("Foo")), "decapitalize");
        Introspector.flushCaches();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
