/*
 * @test
 * @summary javax.sql.rowset.serial.SerialBlob conformance: white-box structure + black-box behavior.
 * @run main SerialBlobTest
 */

public class SerialBlobTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static byte[] readAll(java.io.InputStream is) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] tmp = new byte[64];
        int n;
        while ((n = is.read(tmp)) != -1) out.write(tmp, 0, n);
        return out.toByteArray();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.serial.SerialBlob");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 3, "3 direct interface(s)");
        check(ifs[0].getName().equals("java.sql.Blob"), "interface 0");
        check(ifs[1].getName().equals("java.io.Serializable"), "interface 1");
        check(ifs[2].getName().equals("java.lang.Cloneable"), "interface 2");
        check(c.getDeclaredMethods().length == 17, "declared method count 17");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        javax.sql.rowset.serial.SerialBlob b = new javax.sql.rowset.serial.SerialBlob(data);
        check(b.length() == 5, "length");

        // getBytes is 1-based.
        byte[] got = b.getBytes(1, 2);
        check(got.length == 2 && got[0] == 1 && got[1] == 2, "getBytes 1-based slice");
        byte[] all = b.getBytes(1, (int) b.length());
        check(java.util.Arrays.equals(all, data), "getBytes full range");
        byte[] overlong = b.getBytes(1, 100);
        check(overlong.length == 5, "getBytes length larger than blob is shortened to length");
        byte[] mid = b.getBytes(2, 3);
        check(java.util.Arrays.equals(mid, new byte[]{2, 3, 4}), "getBytes mid slice");

        // Invalid positions for getBytes.
        try { b.getBytes(0, 1); check(false, "getBytes pos 0 should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getBytes pos 0 throws SerialException"); }
        try { b.getBytes(6, 1); check(false, "getBytes pos > length should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getBytes pos > length throws SerialException"); }

        // Binary streams.
        check(java.util.Arrays.equals(readAll(b.getBinaryStream()), data), "getBinaryStream full");
        check(java.util.Arrays.equals(readAll(b.getBinaryStream(2, 3)), new byte[]{2, 3, 4}), "getBinaryStream(pos,len)");
        try { b.getBinaryStream(0, 1); check(false, "getBinaryStream pos 0 should throw"); }
        catch (java.sql.SQLException e) { check(true, "getBinaryStream pos 0 throws SQLException"); }

        // position(byte[], start) is 1-based; -1 when not found or start out of range.
        check(b.position(new byte[]{3, 4}, 1) == 3, "position byte pattern");
        check(b.position(new byte[]{3, 4, 5}, 2) == 3, "position byte pattern from offset");
        check(b.position(new byte[]{9, 9}, 1) == -1, "position not found returns -1");
        check(b.position(new byte[]{3, 4}, 6) == -1, "position start beyond length returns -1");

        // position(Blob, start).
        javax.sql.rowset.serial.SerialBlob pattern = new javax.sql.rowset.serial.SerialBlob(new byte[]{4, 5});
        check(b.position(pattern, 1) == 4, "position Blob pattern");

        // setBytes overwrite in place.
        int n = b.setBytes(2, new byte[]{7, 8, 9});
        check(n == 3, "setBytes overwrite returns 3");
        check(java.util.Arrays.equals(b.getBytes(1, (int) b.length()), new byte[]{1, 7, 8, 9, 5}), "setBytes overwrite content");

        // setBytes append at end extends the blob.
        javax.sql.rowset.serial.SerialBlob b2 = new javax.sql.rowset.serial.SerialBlob(data);
        int m = b2.setBytes(6, new byte[]{7, 8});
        check(m == 2, "setBytes append returns 2");
        check(b2.length() == 7, "length after setBytes append");
        check(java.util.Arrays.equals(b2.getBytes(6, 2), new byte[]{7, 8}), "appended bytes readable");

        // setBytes with offset/length.
        javax.sql.rowset.serial.SerialBlob b3 = new javax.sql.rowset.serial.SerialBlob(data);
        int k = b3.setBytes(2, new byte[]{7, 8, 9, 0}, 1, 3);
        check(k == 3, "setBytes offset/length returns 3");
        check(java.util.Arrays.equals(b3.getBytes(1, (int) b3.length()), new byte[]{1, 8, 9, 0, 5}), "setBytes offset/length content");

        // setBytes invalid position.
        try { b2.setBytes(0, new byte[]{1}); check(false, "setBytes pos 0 should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "setBytes pos 0 throws SerialException"); }

        // truncate.
        javax.sql.rowset.serial.SerialBlob b4 = new javax.sql.rowset.serial.SerialBlob(data);
        b4.truncate(3);
        check(b4.length() == 3, "truncate to 3");
        check(java.util.Arrays.equals(b4.getBytes(1, 3), new byte[]{1, 2, 3}), "truncate content");
        javax.sql.rowset.serial.SerialBlob b5 = new javax.sql.rowset.serial.SerialBlob(data);
        b5.truncate(0);
        check(b5.length() == 0, "truncate to 0");
        try { b5.truncate(1); check(false, "truncate beyond length should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "truncate beyond length throws SerialException"); }

        // Empty byte array.
        javax.sql.rowset.serial.SerialBlob empty = new javax.sql.rowset.serial.SerialBlob(new byte[0]);
        check(empty.length() == 0, "empty blob length 0");
        try { empty.getBytes(1, 0); check(false, "getBytes on empty blob should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getBytes on empty blob throws SerialException"); }

        // equals/hashCode/clone.
        javax.sql.rowset.serial.SerialBlob same = new javax.sql.rowset.serial.SerialBlob(data);
        check(b2.equals(same) == false, "blobs with different content not equal");
        javax.sql.rowset.serial.SerialBlob same2 = new javax.sql.rowset.serial.SerialBlob(data);
        check(same2.equals(same), "blobs with same content equal");
        check(same2.hashCode() == same.hashCode(), "equal blobs same hashCode");
        javax.sql.rowset.serial.SerialBlob clone = (javax.sql.rowset.serial.SerialBlob) same2.clone();
        check(clone != same2, "clone distinct instance");
        check(clone.equals(same2), "clone equals original");

        // free() invalidates the blob; subsequent reads throw SerialException.
        javax.sql.rowset.serial.SerialBlob bf = new javax.sql.rowset.serial.SerialBlob(data);
        bf.free();
        bf.free(); // subsequent free() is a no-op
        try { bf.length(); check(false, "length after free should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "length after free throws SerialException"); }
        try { bf.getBytes(1, 1); check(false, "getBytes after free should throw"); }
        catch (javax.sql.rowset.serial.SerialException e) { check(true, "getBytes after free throws SerialException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
