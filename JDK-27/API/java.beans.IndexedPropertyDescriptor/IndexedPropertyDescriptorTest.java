/*
 * @test
 * @summary java.beans.IndexedPropertyDescriptor conformance: introspect indexed read/write methods.
 * @run main IndexedPropertyDescriptorTest
 */

import java.beans.*;

public class IndexedPropertyDescriptorTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

public static class Bean {
    private int[] items;
    public int[] getItems() { return items; }
    public void setItems(int[] it) { items = it; }
    public int getItems(int i) { return items[i]; }
    public void setItems(int i, int v) { items[i] = v; }
}
    public static void main(String[] args) throws Exception {
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor("items", Bean.class);
        check("items".equals(ipd.getName()), "name");
        check(ipd.getPropertyType() == int[].class, "array type");
        check(ipd.getIndexedPropertyType() == int.class, "indexed type");
        check(ipd.getReadMethod() != null, "read method");
        check(ipd.getWriteMethod() != null, "write method");
        check(ipd.getIndexedReadMethod() != null, "indexed read");
        check(ipd.getIndexedWriteMethod() != null, "indexed write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
