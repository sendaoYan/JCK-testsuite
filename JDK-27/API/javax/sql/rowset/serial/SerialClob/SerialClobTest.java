/*
 * @test
 * @summary javax.sql.rowset.serial.SerialClob conformance: white-box structure + black-box behavior.
 * @run main SerialClobTest
 */

public class SerialClobTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String readAll(java.io.Reader r) throws Exception {
        java.io.StringWriter w = new java.io.StringWriter();
        char[] tmp = new char[64];
        int n;
        while ((n = r.read(tmp)) != -1) w.write(tmp, 0, n);
        return w.toString();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.serial.SerialClob");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 3, "3 direct interface(s)");
        check(ifs[0].getName().equals("java.sql.Clob"), "interface 0");
        check(ifs[1].getName().equals("java.io.Serializable"), "interface 1");
        check(ifs[2].getName().equals("java.lang.Cloneable"), "interface 2");
        check(c.getDeclaredMethods().length == 19, "declared method count 19");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        char[] chars = "hello".toCharArray();
        javax.sql.rowset.serial.SerialClob cl = new javax.sql.rowset.serial.SerialClob(chars);
        check(cl.length() == 5, "length");
        check("hello".equals(cl.getSubString(1, 5)), "getSubString full");
        check("ell".equals(cl.getSubString(2, 3)), "getSubString 1-based slice");
        check("".equals(cl.getSubString(3, 0)), "getSubString zero length");

        try { cl.getSubString(0, 1); check(false, "getSubString pos 0 should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getSubString pos 0 throws SerialException"); }
        try { cl.getSubString(6, 1); check(false, "getSubString pos > length should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getSubString pos > length throws SerialException"); }
        try { cl.getSubString(4, 3); check(false, "getSubString pos+length > length should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getSubString pos+length > length throws SerialException"); }

        // Character stream (char[]-constructed SerialClob provides a reader).
        check("hello".equals(readAll(cl.getCharacterStream())), "getCharacterStream full");
        check(cl.getCharacterStream(2, 3) != null, "getCharacterStream(pos,len) non-null");
        try { cl.getCharacterStream(0, 1); check(false, "getCharacterStream pos 0 should throw"); }
        catch (java.sql.SQLException e) { check(true, "getCharacterStream pos 0 throws SQLException"); }
        try { cl.getCharacterStream(4, 3); check(false, "getCharacterStream pos+length > length should throw"); }
        catch (java.sql.SQLException e) { check(true, "getCharacterStream pos+length > length throws SQLException"); }

        // getAsciiStream is unsupported when built from a char[].
        try { cl.getAsciiStream(); check(false, "getAsciiStream should throw for char[]-constructed"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getAsciiStream throws SerialException for char[]-constructed"); }

        // position(String, start) is 1-based; -1 when not found or start out of range.
        check(cl.position("ell", 1) == 2, "position string");
        check(cl.position("lo", 3) == 4, "position string from offset");
        check(cl.position("xyz", 1) == -1, "position not found returns -1");
        check(cl.position("ell", 6) == -1, "position start beyond length returns -1");

        // position(Clob, start).
        javax.sql.rowset.serial.SerialClob pat = new javax.sql.rowset.serial.SerialClob("lo".toCharArray());
        check(cl.position(pat, 1) == 4, "position Clob pattern");

        // setString appends and overwrites.
        int n = cl.setString(6, "XYZ");
        check(n == 3, "setString append returns 3");
        check(cl.length() == 8, "length after setString append");
        check("XYZ".equals(cl.getSubString(6, 3)), "getSubString appended");

        javax.sql.rowset.serial.SerialClob cl2 = new javax.sql.rowset.serial.SerialClob("abcdef".toCharArray());
        int m = cl2.setString(2, "XY");
        check(m == 2, "setString overwrite returns 2");
        check("aXYdef".equals(cl2.getSubString(1, 6)), "setString overwrite content");

        // setString(pos, str, offset, length).
        javax.sql.rowset.serial.SerialClob cl3 = new javax.sql.rowset.serial.SerialClob("abcdef".toCharArray());
        int k = cl3.setString(2, "wxyz", 1, 2);
        check(k == 2, "setString offset/length returns 2");
        check("axydef".equals(cl3.getSubString(1, 6)), "setString offset/length content");

        // truncate.
        javax.sql.rowset.serial.SerialClob cl4 = new javax.sql.rowset.serial.SerialClob("abcdef".toCharArray());
        cl4.truncate(3);
        check(cl4.length() == 3, "truncate to 3");
        check("abc".equals(cl4.getSubString(1, 3)), "truncate content");
        javax.sql.rowset.serial.SerialClob cl5 = new javax.sql.rowset.serial.SerialClob("abc".toCharArray());
        cl5.truncate(0);
        check(cl5.length() == 0, "truncate to 0");

        // Empty char[].
        javax.sql.rowset.serial.SerialClob empty = new javax.sql.rowset.serial.SerialClob(new char[0]);
        check(empty.length() == 0, "empty clob length 0");

        // equals/hashCode/clone.
        javax.sql.rowset.serial.SerialClob sameA = new javax.sql.rowset.serial.SerialClob("abc".toCharArray());
        javax.sql.rowset.serial.SerialClob sameB = new javax.sql.rowset.serial.SerialClob("abc".toCharArray());
        check(sameA.equals(sameB), "equal clobs equal");
        check(sameA.hashCode() == sameB.hashCode(), "equal clobs same hashCode");
        check(!sameA.equals(new javax.sql.rowset.serial.SerialClob("abd".toCharArray())), "different clobs not equal");
        javax.sql.rowset.serial.SerialClob clone = (javax.sql.rowset.serial.SerialClob) sameA.clone();
        check(clone != sameA, "clone distinct instance");
        check(clone.equals(sameA), "clone equals original");

        // free() invalidates the clob.
        javax.sql.rowset.serial.SerialClob cf = new javax.sql.rowset.serial.SerialClob("xyz".toCharArray());
        cf.free();
        cf.free(); // no-op
        try { cf.length(); check(false, "length after free should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "length after free throws SerialException"); }
        try { cf.getSubString(1, 1); check(false, "getSubString after free should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getSubString after free throws SerialException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
