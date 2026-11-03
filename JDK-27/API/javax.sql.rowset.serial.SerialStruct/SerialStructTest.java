/*
 * @test
 * @summary javax.sql.rowset.serial.SerialStruct conformance: white-box structure + black-box behavior.
 * @run main SerialStructTest
 */

import java.util.Map;

public class SerialStructTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Minimal in-memory java.sql.Struct implementation (no database involved).
    static class StubStruct implements java.sql.Struct {
        final String typeName;
        final Object[] attributes;
        StubStruct(String typeName, Object[] attributes) { this.typeName = typeName; this.attributes = attributes; }
        public String getSQLTypeName() { return typeName; }
        public Object[] getAttributes() { return attributes; }
        public Object[] getAttributes(Map<String, Class<?>> map) { return attributes; }
    }

    // Minimal in-memory java.sql.SQLData implementation.
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
        Class<?> c = Class.forName("javax.sql.rowset.serial.SerialStruct");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 3, "3 direct interface(s)");
        check(ifs[0].getName().equals("java.sql.Struct"), "interface 0");
        check(ifs[1].getName().equals("java.io.Serializable"), "interface 1");
        check(ifs[2].getName().equals("java.lang.Cloneable"), "interface 2");
        check(c.getDeclaredMethods().length == 9, "declared method count 9");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Map<String, Class<?>> map = new java.util.HashMap<String, Class<?>>();
        Object[] attrs = new Object[]{"Bruce", "Wayne", 1939, "Batman"};

        javax.sql.rowset.serial.SerialStruct fromStruct = new javax.sql.rowset.serial.SerialStruct(new StubStruct("SUPERHERO", attrs), map);
        check("SUPERHERO".equals(fromStruct.getSQLTypeName()), "Struct ctor getSQLTypeName");
        Object[] got = fromStruct.getAttributes();
        check(java.util.Arrays.equals(got, attrs), "Struct ctor getAttributes");
        check(got != attrs, "getAttributes returns a copy");
        check(java.util.Arrays.equals(fromStruct.getAttributes(map), attrs), "Struct ctor getAttributes(map)");

        javax.sql.rowset.serial.SerialStruct fromSQLData = new javax.sql.rowset.serial.SerialStruct(new HeroData(), map);
        check("SUPERHERO".equals(fromSQLData.getSQLTypeName()), "SQLData ctor getSQLTypeName");
        check(java.util.Arrays.equals(fromSQLData.getAttributes(), attrs), "SQLData ctor getAttributes");
        check(java.util.Arrays.equals(fromSQLData.getAttributes(map), attrs), "SQLData ctor getAttributes(map)");

        // The two construction paths produce equal SerialStructs for the same type/attributes.
        check(fromStruct.equals(fromSQLData), "Struct and SQLData ctors produce equal structs");
        check(fromStruct.hashCode() == fromSQLData.hashCode(), "equal structs same hashCode");

        // A different SQL type name yields inequality.
        javax.sql.rowset.serial.SerialStruct otherType = new javax.sql.rowset.serial.SerialStruct(new StubStruct("VILLAIN", attrs), map);
        check(!fromStruct.equals(otherType), "different SQL type name not equal");

        // clone.
        javax.sql.rowset.serial.SerialStruct clone = (javax.sql.rowset.serial.SerialStruct) fromStruct.clone();
        check(clone != fromStruct, "clone distinct instance");
        check(clone.equals(fromStruct), "clone equals original");
        check(java.util.Arrays.equals(clone.getAttributes(), attrs), "clone attributes");

        // Nested Struct attribute is serialized to a SerialStruct (mapToSerial).
        Object[] nested = new Object[]{new StubStruct("POINT", new Object[]{1, 2})};
        javax.sql.rowset.serial.SerialStruct ssNested = new javax.sql.rowset.serial.SerialStruct(new StubStruct("SHAPE", nested), map);
        Object[] nestedGot = ssNested.getAttributes();
        check(nestedGot.length == 1 && nestedGot[0] instanceof javax.sql.rowset.serial.SerialStruct, "nested Struct converted to SerialStruct");
        javax.sql.rowset.serial.SerialStruct inner = (javax.sql.rowset.serial.SerialStruct) nestedGot[0];
        check("POINT".equals(inner.getSQLTypeName()), "nested SerialStruct type name");
        check(java.util.Arrays.equals(inner.getAttributes(), new Object[]{1, 2}), "nested SerialStruct attributes");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
