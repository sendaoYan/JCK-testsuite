/*
 * @test
 * @summary javax.sql.rowset.serial.SerialArray conformance: white-box structure + black-box behavior.
 * @run main SerialArrayTest
 */

import java.util.Map;

public class SerialArrayTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Minimal in-memory java.sql.Array implementation (no database involved).
    static class StubArray implements java.sql.Array {
        final Object[] elements;
        final String baseTypeName;
        final int baseType;
        StubArray(Object[] elements, String baseTypeName, int baseType) {
            this.elements = elements; this.baseTypeName = baseTypeName; this.baseType = baseType;
        }
        public Object getArray() { return elements; }
        public Object getArray(Map<String, Class<?>> map) { return elements; }
        public Object getArray(long index, int count) { throw new UnsupportedOperationException(); }
        public Object getArray(long index, int count, Map<String, Class<?>> map) { throw new UnsupportedOperationException(); }
        public int getBaseType() { return baseType; }
        public String getBaseTypeName() { return baseTypeName; }
        public java.sql.ResultSet getResultSet() { throw new UnsupportedOperationException(); }
        public java.sql.ResultSet getResultSet(long index, int count) { throw new UnsupportedOperationException(); }
        public java.sql.ResultSet getResultSet(Map<String, Class<?>> map) { throw new UnsupportedOperationException(); }
        public java.sql.ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) { throw new UnsupportedOperationException(); }
        public void free() { }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.serial.SerialArray");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 3, "3 direct interface(s)");
        check(ifs[0].getName().equals("java.sql.Array"), "interface 0");
        check(ifs[1].getName().equals("java.io.Serializable"), "interface 1");
        check(ifs[2].getName().equals("java.lang.Cloneable"), "interface 2");
        check(c.getDeclaredMethods().length == 17, "declared method count 17");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        String[] coffees = new String[]{"Espresso", "Colombian", "French Roast", "Cappuccino"};
        Map<String, Class<?>> map = new java.util.HashMap<String, Class<?>>();
        javax.sql.rowset.serial.SerialArray sa = new javax.sql.rowset.serial.SerialArray(
            new StubArray(coffees, "VARCHAR", java.sql.Types.VARCHAR));

        check(sa.getBaseType() == java.sql.Types.VARCHAR, "getBaseType");
        check("VARCHAR".equals(sa.getBaseTypeName()), "getBaseTypeName");

        Object[] all = (Object[]) sa.getArray();
        check(java.util.Arrays.equals(all, coffees), "getArray equals original");
        check(all != coffees, "getArray returns a copy");

        Object[] allMap = (Object[]) sa.getArray(map);
        check(java.util.Arrays.equals(allMap, coffees), "getArray(Map) equals original");

        Object[] slice = (Object[]) sa.getArray(1, 2);
        check(java.util.Arrays.equals(slice, new Object[]{"Colombian", "French Roast"}), "getArray(index,count)");

        Object[] sliceMap = (Object[]) sa.getArray(1, 2, map);
        check(java.util.Arrays.equals(sliceMap, new Object[]{"Colombian", "French Roast"}), "getArray(index,count,map)");

        // getResultSet is unsupported and always throws SerialException.
        try { sa.getResultSet(); check(false, "getResultSet() should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getResultSet() throws SerialException"); }
        try { sa.getResultSet(map); check(false, "getResultSet(map) should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getResultSet(map) throws SerialException"); }
        try { sa.getResultSet(1, 1); check(false, "getResultSet(index,count) should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getResultSet(index,count) throws SerialException"); }
        try { sa.getResultSet(1, 1, map); check(false, "getResultSet(index,count,map) should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getResultSet(index,count,map) throws SerialException"); }

        // equals/hashCode/clone.
        javax.sql.rowset.serial.SerialArray same = new javax.sql.rowset.serial.SerialArray(
            new StubArray(coffees, "VARCHAR", java.sql.Types.VARCHAR));
        check(sa.equals(same), "equal SerialArrays equal");
        check(sa.hashCode() == same.hashCode(), "equal SerialArrays same hashCode");
        String[] other = new String[]{"Espresso", "Colombian", "French Roast", "Decaf"};
        check(!sa.equals(new javax.sql.rowset.serial.SerialArray(
            new StubArray(other, "VARCHAR", java.sql.Types.VARCHAR))), "different elements not equal");
        javax.sql.rowset.serial.SerialArray clone = (javax.sql.rowset.serial.SerialArray) sa.clone();
        check(clone != sa, "clone distinct instance");
        check(clone.equals(sa), "clone equals original");

        // free() invalidates the array.
        javax.sql.rowset.serial.SerialArray sf = new javax.sql.rowset.serial.SerialArray(
            new StubArray(coffees, "VARCHAR", java.sql.Types.VARCHAR));
        sf.free();
        sf.free(); // no-op
        try { sf.getArray(); check(false, "getArray after free should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getArray after free throws SerialException"); }
        try { sf.getBaseType(); check(false, "getBaseType after free should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getBaseType after free throws SerialException"); }
        try { sf.getBaseTypeName(); check(false, "getBaseTypeName after free should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getBaseTypeName after free throws SerialException"); }

        // null inputs are rejected with SQLException.
        try { new javax.sql.rowset.serial.SerialArray((java.sql.Array) null); check(false, "null Array should throw"); }
        catch (java.sql.SQLException e) { check(true, "null Array throws SQLException"); }
        try { new javax.sql.rowset.serial.SerialArray(new StubArray(coffees, "VARCHAR", java.sql.Types.VARCHAR), null); check(false, "null map should throw"); }
        catch (java.sql.SQLException e) { check(true, "null map throws SQLException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
