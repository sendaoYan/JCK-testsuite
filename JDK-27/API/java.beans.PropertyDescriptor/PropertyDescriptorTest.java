/*
 * @test
 * @summary java.beans.PropertyDescriptor conformance: introspect read/write methods and FeatureDescriptor state.
 * @run main PropertyDescriptorTest
 */

import java.beans.*;

public class PropertyDescriptorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

public static class Bean {
    private int age;
    public int getAge() { return age; }
    public void setAge(int a) { age = a; }
}
    public static void main(String[] args) throws Exception {
        PropertyDescriptor pd = new PropertyDescriptor("age", Bean.class);
        check("age".equals(pd.getName()), "name");
        check(pd.getPropertyType() == int.class, "type");
        check(pd.getReadMethod() != null, "read method");
        check(pd.getWriteMethod() != null, "write method");
        pd.setDisplayName("Age"); check("Age".equals(pd.getDisplayName()), "display name");
        pd.setShortDescription("age in years"); check("age in years".equals(pd.getShortDescription()), "short desc");
        pd.setValue("k", "v"); check("v".equals(pd.getValue("k")), "attribute value");
        pd.setExpert(true); check(pd.isExpert(), "expert");
        pd.setHidden(true); check(pd.isHidden(), "hidden");
        pd.setPreferred(true); check(pd.isPreferred(), "preferred");
        pd.setBound(true); check(pd.isBound(), "bound");
        pd.setConstrained(true); check(pd.isConstrained(), "constrained");
        check(pd.attributeNames() != null, "attribute names");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
