/*
 * @test
 * @summary java.beans.IndexedPropertyDescriptor conformance: indexed read/write methods.
 * @run main IndexedPropertyDescriptorTest
 */

import java.beans.*;

public class IndexedPropertyDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class IndexedBean {
        private String[] items;
        public String getItem(int i) { return items[i]; }
        public void setItem(int i, String v) { items[i] = v; }
        public String[] getItems() { return items; }
        public void setItems(String[] v) { items = v; }
    }

    public static void main(String[] args) throws Exception {
        IndexedPropertyDescriptor pd = new IndexedPropertyDescriptor("items", IndexedBean.class, "getItems", "setItems", "getItem", "setItem");
        check(pd.getIndexedReadMethod() != null, "indexed read method");
        check(pd.getIndexedWriteMethod() != null, "indexed write method");
        check(pd.getIndexedPropertyType() == String.class, "indexed property type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
