/*
 * @test
 * @summary java.beans.Introspector conformance: decapitalize and bean introspection.
 * @run main IntrospectorTest
 */

import java.beans.*;

public class IntrospectorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

public static class Bean {
    private int age;
    public int getAge() { return age; }
    public void setAge(int a) { age = a; }
}
    public static void main(String[] args) throws Exception {
        check("URL".equals(Introspector.decapitalize("URL")), "decapitalize acronym");
        check("age".equals(Introspector.decapitalize("Age")), "decapitalize");
        BeanInfo bi = Introspector.getBeanInfo(Bean.class);
        check(bi != null, "getBeanInfo");
        boolean found = false;
        for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
            if ("age".equals(pd.getName())) { found = true; check(pd.getPropertyType() == int.class, "age type"); }
        }
        check(found, "age property found");
        Introspector.flushCaches();
        check(Introspector.getBeanInfoSearchPath() != null, "search path");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
