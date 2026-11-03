/*
 * @test
 * @summary javax.sql.rowset.serial.SQLOutputImpl conformance: white-box structure + black-box behavior.
 * @run main SQLOutputImplTest
 */

import java.util.Map;

public class SQLOutputImplTest {
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

    static class StubStruct implements java.sql.Struct {
        final String typeName;
        final Object[] attributes;
        StubStruct(String typeName, Object[] attributes) { this.typeName = typeName; this.attributes = attributes; }
        public String getSQLTypeName() { return typeName; }
        public Object[] getAttributes() { return attributes; }
        public Object[] getAttributes(Map<String, Class<?>> map) { return attributes; }
    }

    // In-memory Clob that provides both character and ASCII streams (required by SerialClob(Clob)).
    static class StubClob implements java.sql.Clob {
        final String data;
        StubClob(String data) { this.data = data; }
        public long length() { return data.length(); }
        public java.io.Reader getCharacterStream() { return new java.io.StringReader(data); }
        public java.io.InputStream getAsciiStream() { return new java.io.ByteArrayInputStream(data.getBytes(java.nio.charset.StandardCharsets.US_ASCII)); }
        public String getSubString(long pos, int length) { throw new UnsupportedOperationException(); }
        public long position(String searchstr, long start) { throw new UnsupportedOperationException(); }
        public long position(java.sql.Clob searchstr, long start) { throw new UnsupportedOperationException(); }
        public int setString(long pos, String str) { throw new UnsupportedOperationException(); }
        public int setString(long pos, String str, int offset, int len) { throw new UnsupportedOperationException(); }
        public java.io.OutputStream setAsciiStream(long pos) { throw new UnsupportedOperationException(); }
        public java.io.Writer setCharacterStream(long pos) { throw new UnsupportedOperationException(); }
        public void truncate(long len) { throw new UnsupportedOperationException(); }
        public java.io.Reader getCharacterStream(long pos, long length) { throw new UnsupportedOperationException(); }
        public void free() { }
    }

    static class HeroData implements java.sql.SQLData {
        public String getSQLTypeName() { return "SUPERHERO"; }
        public void readSQL(java.sql.SQLInput stream, String typeName) { }
        public void writeSQL(java.sql.SQLOutput stream) throws java.sql.SQLException {
            stream.writeString("Bruce");
            stream.writeString("Wayne");
            stream.writeInt(1939);
            stream.writeString("Batman");
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.serial.SQLOutputImpl");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct interface(s)");
        check(ifs[0].getName().equals("java.sql.SQLOutput"), "interface 0");
        check(c.getDeclaredMethods().length == 27, "declared method count 27");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Map<String, Class<?>> map = new java.util.HashMap<String, Class<?>>();
        java.util.Vector<Object> v = new java.util.Vector<Object>();
        javax.sql.rowset.serial.SQLOutputImpl out = new javax.sql.rowset.serial.SQLOutputImpl(v, map);

        out.writeInt(42);                          // 0
        out.writeLong(99L);                        // 1
        out.writeString("abc");                    // 2
        out.writeBoolean(true);                    // 3
        out.writeDouble(1.5);                      // 4
        out.writeByte((byte) 7);                   // 5
        out.writeShort((short) 8);                 // 6
        out.writeFloat(2.5f);                      // 7
        out.writeBigDecimal(new java.math.BigDecimal("3.14")); // 8
        byte[] bytes = new byte[]{1, 2};           // 9
        out.writeBytes(bytes);
        java.sql.Date d = java.sql.Date.valueOf("2024-01-02"); // 10
        out.writeDate(d);
        java.sql.Time t = java.sql.Time.valueOf("10:20:30"); // 11
        out.writeTime(t);
        java.sql.Timestamp ts = java.sql.Timestamp.valueOf("2024-01-02 10:20:30"); // 12
        out.writeTimestamp(ts);
        out.writeNString("n");                     // 13

        javax.sql.rowset.serial.SerialBlob blob = new javax.sql.rowset.serial.SerialBlob(new byte[]{9, 8});
        StubClob clob = new StubClob("clob");
        java.net.URL url = new java.net.URL("https://example.com/");
        StubArray arr = new StubArray(new Object[]{"a", "b"});
        StubRef ref = new StubRef();
        StubStruct struct = new StubStruct("STRUCTTYPE", new Object[]{"x", 1});

        out.writeRef(ref);                         // 14 -> SerialRef
        out.writeBlob(blob);                       // 15 -> SerialBlob
        out.writeClob(clob);                       // 16 -> SerialClob
        out.writeArray(arr);                       // 17 -> SerialArray
        out.writeURL(url);                         // 18 -> SerialDatalink
        out.writeStruct(struct);                   // 19 -> SerialStruct
        out.writeObject(new HeroData());           // 20 -> SerialStruct (from SQLData)
        out.writeObject(null);                     // 21 -> null

        check(v.size() == 22, "22 values written");

        check(v.get(0).equals(Integer.valueOf(42)), "writeInt");
        check(v.get(1).equals(Long.valueOf(99L)), "writeLong");
        check(v.get(2).equals("abc"), "writeString");
        check(v.get(3).equals(Boolean.TRUE), "writeBoolean");
        check(v.get(4).equals(Double.valueOf(1.5)), "writeDouble");
        check(v.get(5).equals(Byte.valueOf((byte) 7)), "writeByte");
        check(v.get(6).equals(Short.valueOf((short) 8)), "writeShort");
        check(v.get(7).equals(Float.valueOf(2.5f)), "writeFloat");
        check(v.get(8).equals(new java.math.BigDecimal("3.14")), "writeBigDecimal");
        check(v.get(9) == bytes, "writeBytes stores the same array reference");
        check(v.get(10).equals(d), "writeDate");
        check(v.get(11).equals(t), "writeTime");
        check(v.get(12).equals(ts), "writeTimestamp");
        check(v.get(13).equals("n"), "writeNString");

        check(v.get(14) instanceof javax.sql.rowset.serial.SerialRef, "writeRef wraps SerialRef");
        check(((javax.sql.rowset.serial.SerialRef) v.get(14)).getBaseTypeName().equals("SUPERHERO"), "writeRef base type name");

        check(v.get(15) instanceof javax.sql.rowset.serial.SerialBlob, "writeBlob wraps SerialBlob");
        check(((javax.sql.rowset.serial.SerialBlob) v.get(15)).length() == 2, "writeBlob content length");

        check(v.get(16) instanceof javax.sql.rowset.serial.SerialClob, "writeClob wraps SerialClob");
        check(((javax.sql.rowset.serial.SerialClob) v.get(16)).getSubString(1, 4).equals("clob"), "writeClob content");

        check(v.get(17) instanceof javax.sql.rowset.serial.SerialArray, "writeArray wraps SerialArray");
        check(java.util.Arrays.equals((Object[]) ((javax.sql.rowset.serial.SerialArray) v.get(17)).getArray(), new Object[]{"a", "b"}), "writeArray content");

        check(v.get(18) instanceof javax.sql.rowset.serial.SerialDatalink, "writeURL wraps SerialDatalink");
        check(((javax.sql.rowset.serial.SerialDatalink) v.get(18)).getDatalink().equals(url), "writeURL content");

        check(v.get(19) instanceof javax.sql.rowset.serial.SerialStruct, "writeStruct wraps SerialStruct");
        check(((javax.sql.rowset.serial.SerialStruct) v.get(19)).getSQLTypeName().equals("STRUCTTYPE"), "writeStruct type name");

        check(v.get(20) instanceof javax.sql.rowset.serial.SerialStruct, "writeObject(SQLData) wraps SerialStruct");
        check(((javax.sql.rowset.serial.SerialStruct) v.get(20)).getSQLTypeName().equals("SUPERHERO"), "writeObject type name");
        check(java.util.Arrays.equals(((javax.sql.rowset.serial.SerialStruct) v.get(20)).getAttributes(),
                new Object[]{"Bruce", "Wayne", 1939, "Batman"}), "writeObject attributes");

        check(v.get(21) == null, "writeObject(null) writes SQL NULL");

        // null UDT values are written as null.
        java.util.Vector<Object> v2 = new java.util.Vector<Object>();
        javax.sql.rowset.serial.SQLOutputImpl out2 = new javax.sql.rowset.serial.SQLOutputImpl(v2, map);
        out2.writeRef(null);
        out2.writeBlob(null);
        out2.writeClob(null);
        out2.writeArray(null);
        out2.writeURL(null);
        check(v2.size() == 5, "5 null UDT values written");
        check(v2.get(0) == null && v2.get(1) == null && v2.get(2) == null && v2.get(3) == null && v2.get(4) == null, "null UDT values stored as null");

        // Null constructor parameters are rejected.
        try { new javax.sql.rowset.serial.SQLOutputImpl(null, map); check(false, "null Vector should throw"); }
        catch (java.sql.SQLException e) { check(true, "null Vector throws SQLException"); }
        try { new javax.sql.rowset.serial.SQLOutputImpl(v, null); check(false, "null map should throw"); }
        catch (java.sql.SQLException e) { check(true, "null map throws SQLException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
