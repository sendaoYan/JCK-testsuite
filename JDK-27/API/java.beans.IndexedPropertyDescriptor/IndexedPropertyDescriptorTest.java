/*
 * @test
 * @summary java.beans.IndexedPropertyDescriptor conformance: white-box structure + black-box behavior.
 * @run main IndexedPropertyDescriptorTest
 */

import java.beans.IndexedPropertyDescriptor;
import java.lang.reflect.Method;

public class IndexedPropertyDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class Bean {
        private int[] items = new int[0];
        public int[] getItems() { return items; }
        public void setItems(int[] v) { items = v; }
        public int getItems(int i) { return items[i]; }
        public void setItems(int i, int v) { items[i] = v; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.IndexedPropertyDescriptor");
        check(java.beans.PropertyDescriptor.class.isAssignableFrom(c), "extends PropertyDescriptor");
    }

    static void testBlackBox() throws Exception {
        IndexedPropertyDescriptor pd = new IndexedPropertyDescriptor("items", Bean.class);
        check(int[].class == pd.getPropertyType(), "array propertyType");
        check(int.class == pd.getIndexedPropertyType(), "indexed propertyType");
        check(pd.getIndexedReadMethod() != null, "indexed readMethod");
        check(pd.getIndexedWriteMethod() != null, "indexed writeMethod");

        Method ir = Bean.class.getMethod("getItems", int.class);
        Method iw = Bean.class.getMethod("setItems", int.class, int.class);
        IndexedPropertyDescriptor pd2 = new IndexedPropertyDescriptor("items", Bean.class.getMethod("getItems"), Bean.class.getMethod("setItems", int[].class), ir, iw);
        check(int.class == pd2.getIndexedPropertyType(), "explicit ctor indexed type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
