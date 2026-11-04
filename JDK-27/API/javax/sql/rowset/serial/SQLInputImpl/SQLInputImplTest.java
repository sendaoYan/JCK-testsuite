/*
 * @test
 * @summary javax.sql.rowset.serial.SQLInputImpl conformance: white-box structure + black-box behavior.
 * @run main SQLInputImplTest
 */

import java.util.Map;

public class SQLInputImplTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class StubArray implements java.sql.Array {
        final Object[] elements;
        StubArray(Object[] elements) { this.elements = elements; }
        public Object getArray() { return elements; }
        public Object getArray(Map<String, Class<?>> map) { return elements; }
        public Object getArray(long index, int count) { throw new UnsupportedOperationException(); }
        public Object getArray(long index, int count, Map<String, Class<?>> map) { throw new UnsupportedOperationException(); }
        public int getBaseType() { return java.sql.Types.VARCHAR; }
        public String getBaseTypeName() { return "VARCHAR"; }
        public java.sql.ResultSet getResultSet() { throw new UnsupportedOperationException(); }
        public java.sql.ResultSet getResultSet(long index, int count) { throw new UnsupportedOperationException(); }
        public java.sql.ResultSet getResultSet(Map<String, Class<?>> map) { throw new UnsupportedOperationException(); }
        public java.sql.ResultSet getResultSet(long index, int count, Map<String, Class<?>> map) { throw new UnsupportedOperationException(); }
        public void free() { }
    }

    static class StubRef implements java.sql.Ref {
        public String getBaseTypeName() { return "SUPERHERO"; }
        public Object getObject() { return "hero"; }
        public Object getObject(Map<String, Class<?>> map) { return "hero"; }
        public void setObject(Object value) { }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.serial.SQLInputImpl");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct interface(s)");
        check(ifs[0].getName().equals("java.sql.SQLInput"), "interface 0");
        check(c.getDeclaredMethods().length == 28, "declared method count 28");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Map<String, Class<?>> map = new java.util.HashMap<String, Class<?>>();
        javax.sql.rowset.serial.SerialBlob blob = new javax.sql.rowset.serial.SerialBlob(new byte[]{9, 8});
        javax.sql.rowset.serial.SerialClob clob = new javax.sql.rowset.serial.SerialClob("clob-data".toCharArray());
        java.net.URL url = new java.net.URL("https://example.com/");
        StubArray arr = new StubArray(new Object[]{"a", "b"});
        StubRef ref = new StubRef();

        Object[] attrs = new Object[]{
            Integer.valueOf(42),                    // readInt
            "hello",                                // readString
            Boolean.TRUE,                           // readBoolean
            Double.valueOf(1.5),                    // readDouble
            Long.valueOf(99L),                      // readLong
            Byte.valueOf((byte) 7),                 // readByte
            Short.valueOf((short) 8),               // readShort
            Float.valueOf(2.5f),                    // readFloat
            new java.math.BigDecimal("3.14"),       // readBigDecimal
            new byte[]{1, 2, 3},                    // readBytes
            java.sql.Date.valueOf("2024-01-02"),    // readDate
            java.sql.Time.valueOf("10:20:30"),      // readTime
            java.sql.Timestamp.valueOf("2024-01-02 10:20:30"), // readTimestamp
            null,                                   // readString -> null (wasNull)
            blob,                                   // readBlob
            clob,                                   // readClob
            url,                                    // readURL
            arr,                                    // readArray
            ref,                                    // readRef
            "object-attr",                          // readObject
            "nstring",                              // readNString
            new java.io.StringReader("chars"),      // readCharacterStream
            new java.io.ByteArrayInputStream(new byte[]{5}), // readBinaryStream
        };

        javax.sql.rowset.serial.SQLInputImpl in = new javax.sql.rowset.serial.SQLInputImpl(attrs, map);

        check(in.readInt() == 42, "readInt");
        check(in.wasNull() == false, "wasNull false after non-null int");
        check("hello".equals(in.readString()), "readString");
        check(in.readBoolean() == true, "readBoolean");
        check(in.readDouble() == 1.5, "readDouble");
        check(in.readLong() == 99L, "readLong");
        check(in.readByte() == (byte) 7, "readByte");
        check(in.readShort() == (short) 8, "readShort");
        check(in.readFloat() == 2.5f, "readFloat");
        check(new java.math.BigDecimal("3.14").equals(in.readBigDecimal()), "readBigDecimal");
        check(java.util.Arrays.equals(in.readBytes(), new byte[]{1, 2, 3}), "readBytes");
        check(java.sql.Date.valueOf("2024-01-02").equals(in.readDate()), "readDate");
        check(java.sql.Time.valueOf("10:20:30").equals(in.readTime()), "readTime");
        check(java.sql.Timestamp.valueOf("2024-01-02 10:20:30").equals(in.readTimestamp()), "readTimestamp");

        // A null attribute: readString returns null and wasNull() becomes true.
        check(in.readString() == null, "readString null returns null");
        check(in.wasNull() == true, "wasNull true after null read");

        check(in.readBlob() == blob, "readBlob returns the stored Blob");
        check(in.wasNull() == false, "wasNull reset after non-null read");
        check(in.readClob() == clob, "readClob returns the stored Clob");
        check(in.readURL().equals(url), "readURL");
        check(in.readArray() == arr, "readArray returns the stored Array");
        check(in.readRef() == ref, "readRef returns the stored Ref");
        check("object-attr".equals(in.readObject()), "readObject returns plain attribute");
        check("nstring".equals(in.readNString()), "readNString");
        check(in.readCharacterStream() != null, "readCharacterStream returns Reader");
        check(in.readBinaryStream() != null, "readBinaryStream returns InputStream");

        // Reading past the end throws SQLException.
        try { in.readInt(); check(false, "read past end should throw"); }
        catch (java.sql.SQLException e) { check(true, "read past end throws SQLException"); }

        // Null typed primitive read: 0/false return value plus wasNull true.
        javax.sql.rowset.serial.SQLInputImpl nullIn = new javax.sql.rowset.serial.SQLInputImpl(new Object[]{null}, map);
        check(nullIn.readInt() == 0, "readInt null returns 0");
        check(nullIn.wasNull() == true, "readInt null wasNull true");

        javax.sql.rowset.serial.SQLInputImpl nullBool = new javax.sql.rowset.serial.SQLInputImpl(new Object[]{null}, map);
        check(nullBool.readBoolean() == false, "readBoolean null returns false");
        check(nullBool.wasNull() == true, "readBoolean null wasNull true");

        // wasNull defaults to false before any read.
        javax.sql.rowset.serial.SQLInputImpl fresh = new javax.sql.rowset.serial.SQLInputImpl(new Object[]{"x"}, map);
        check(fresh.wasNull() == false, "wasNull defaults false before first read");

        // Null constructor parameters are rejected.
        try { new javax.sql.rowset.serial.SQLInputImpl(null, map); check(false, "null attributes should throw"); }
        catch (java.sql.SQLException e) { check(true, "null attributes throws SQLException"); }
        try { new javax.sql.rowset.serial.SQLInputImpl(new Object[0], null); check(false, "null map should throw"); }
        catch (java.sql.SQLException e) { check(true, "null map throws SQLException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
