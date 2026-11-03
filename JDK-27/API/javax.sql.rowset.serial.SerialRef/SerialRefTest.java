/*
 * @test
 * @summary javax.sql.rowset.serial.SerialRef conformance: white-box structure + black-box behavior.
 * @run main SerialRefTest
 */

import java.util.Map;

public class SerialRefTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Minimal in-memory java.sql.Ref implementation (no database involved).
    static class StubRef implements java.sql.Ref {
        final String typeName;
        Object value;
        StubRef(String typeName, Object value) { this.typeName = typeName; this.value = value; }
        public String getBaseTypeName() { return typeName; }
        public Object getObject() { return value; }
        public Object getObject(Map<String, Class<?>> map) { return value; }
        public void setObject(Object value) { this.value = value; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.serial.SerialRef");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 3, "3 direct interface(s)");
        check(ifs[0].getName().equals("java.sql.Ref"), "interface 0");
        check(ifs[1].getName().equals("java.io.Serializable"), "interface 1");
        check(ifs[2].getName().equals("java.lang.Cloneable"), "interface 2");
        check(c.getDeclaredMethods().length == 9, "declared method count 9");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Object initial = "hero-object";
        javax.sql.rowset.serial.SerialRef sr = new javax.sql.rowset.serial.SerialRef(new StubRef("SUPERHERO", initial));

        check("SUPERHERO".equals(sr.getBaseTypeName()), "getBaseTypeName");
        check(initial.equals(sr.getObject()), "getObject resolves underlying Ref");

        // getObject(Map) looks up the SerialRef's stored object (the Ref) in the map.
        Map<String, Class<?>> empty = new java.util.HashMap<String, Class<?>>();
        check(sr.getObject(empty) == null, "getObject(Map) with empty map returns null");

        // setObject delegates to the underlying Ref; getObject() reflects the update.
        Object next = "sidekick-object";
        sr.setObject(next);
        check(next.equals(sr.getObject()), "setObject then getObject returns new value");
        check(!initial.equals(sr.getObject()), "setObject changed the referenced value");

        // clone clears the underlying reference but shares the stored object.
        javax.sql.rowset.serial.SerialRef clone = (javax.sql.rowset.serial.SerialRef) sr.clone();
        check(clone != sr, "clone distinct instance");
        check(sr.equals(clone), "clone equals original");
        check(sr.hashCode() == clone.hashCode(), "clone same hashCode");

        // null Ref is rejected.
        try { new javax.sql.rowset.serial.SerialRef(null); check(false, "null Ref should throw"); }
        catch (java.sql.SQLException e) { check(true, "null Ref throws SQLException"); }

        // null base type name is rejected.
        try { new javax.sql.rowset.serial.SerialRef(new StubRef(null, "x")); check(false, "null base type name should throw"); }
        catch (java.sql.SQLException e) { check(true, "null base type name throws SQLException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
