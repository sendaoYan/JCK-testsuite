/*
 * @test
 * @summary javax.naming.BinaryRefAddr conformance: white-box structure + black-box behavior.
 * @run main BinaryRefAddrTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import javax.naming.BinaryRefAddr;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;

public class BinaryRefAddrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = BinaryRefAddr.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == RefAddr.class, "extends RefAddr");
        check(c.getDeclaredConstructor(String.class, byte[].class) != null, "ctor(String,byte[])");
        check(c.getDeclaredConstructor(String.class, byte[].class, int.class, int.class) != null,
                "ctor(String,byte[],int,int)");
        check(method(c, "getContent"), "getContent");
    }
    static boolean method(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = {1, 2, 3, 4, 5};
        BinaryRefAddr a = new BinaryRefAddr("bin", data);
        check(a.getType().equals("bin"), "type");
        byte[] out = (byte[]) a.getContent();
        check(Arrays.equals(out, data), "content round trip");

        // defensive copy: mutating the caller's array must not affect the addr
        data[0] = 99;
        check(((byte[]) a.getContent())[0] == 1, "constructor copies input");
        byte[] got1 = (byte[]) a.getContent();
        check(Arrays.equals(got1, new byte[] {1, 2, 3, 4, 5}), "getContent equal view");

        // offset/length constructor
        BinaryRefAddr part = new BinaryRefAddr("bin", new byte[] {10, 20, 30, 40}, 1, 2);
        check(Arrays.equals((byte[]) part.getContent(), new byte[] {20, 30}), "offset+length ctor");

        // equals/hashCode compare type + array contents, not array identity
        BinaryRefAddr b = new BinaryRefAddr("bin", new byte[] {1, 2, 3, 4, 5});
        check(a.equals(b) && b.equals(a), "equals by content");
        check(a.hashCode() == b.hashCode(), "hashCode equal pair");
        check(!a.equals(new BinaryRefAddr("bin", new byte[] {99, 2, 3, 4, 5})), "content differs");
        check(!a.equals(new BinaryRefAddr("bin", new byte[] {2})), "different length");
        check(!a.equals(new BinaryRefAddr("other", new byte[] {1, 2, 3, 4, 5})), "type mismatch");
        check(a.toString().startsWith("Address Type: bin"), "toString prefix");
        check(a.toString().contains("1 2 3 4 5"), "toString lists bytes");

        BinaryRefAddr empty = new BinaryRefAddr("e", new byte[0]);
        check(((byte[]) empty.getContent()).length == 0, "empty array");
        check(empty.equals(new BinaryRefAddr("e", new byte[0])), "empty equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dBraCopyIndependence();
        dBraSlicing();
        dBraEquality();
        dBraNegative();
        dBraSerialization();
        System.out.println("deep: BinaryRefAddr contracts OK");
    }

    static void dBraCopyIndependence() {
        byte[] src = { 1, 2, 3 };
        BinaryRefAddr b = new BinaryRefAddr("t", src);
        src[0] = 99;
        byte[] out = (byte[]) b.getContent();
        check(out[0] == 1, "constructor copies source array");
        out[1] = 77;
        byte[] again = (byte[]) b.getContent();
        check(again == out, "getContent hands out the stored array (no per-call copy)");
        check(new BinaryRefAddr("t", new byte[] { 5 }).getContent() instanceof byte[],
                "getContent is byte[] form");
    }

    static void dBraSlicing() {
        byte[] src = { 10, 20, 30, 40 };
        BinaryRefAddr b = new BinaryRefAddr("t", src, 1, 2);
        byte[] out = (byte[]) b.getContent();
        check(out.length == 2 && out[0] == 20 && out[1] == 30, "offset/length slicing");
        BinaryRefAddr full = new BinaryRefAddr("t", src, 0, src.length);
        check(((byte[]) full.getContent()).length == 4, "full slice");
        BinaryRefAddr empty = new BinaryRefAddr("t", src, 2, 0);
        check(((byte[]) empty.getContent()).length == 0, "zero-length slice");
    }

    static void dBraEquality() {
        BinaryRefAddr a = new BinaryRefAddr("t", new byte[] { 1, 2 });
        BinaryRefAddr b = new BinaryRefAddr("t", new byte[] { 1, 2 });
        check(a.equals(b) && b.equals(a), "equal type+bytes equal");
        check(a.hashCode() == b.hashCode(), "hashCode contract");
        check(!a.equals(new BinaryRefAddr("t", new byte[] { 2, 1 })), "byte order significant");
        check(!a.equals(new BinaryRefAddr("t", new byte[] { 1 })), "different length not equal");
        check(!a.equals(new StringRefAddr("t", "12")), "binary vs string not equal");
        check(!a.equals(null), "null never equal");
        BinaryRefAddr sliced = new BinaryRefAddr("t", new byte[] { 9, 1, 2, 9 }, 1, 2);
        check(sliced.equals(a), "slice equal to direct content");
    }

    static void dBraNegative() {
        boolean npe = false;
        try { new BinaryRefAddr("t", null); } catch (NullPointerException e) { npe = true; }
        check(npe, "null byte[] -> NullPointerException");
        boolean oob = false;
        try { new BinaryRefAddr("t", new byte[] { 1, 2 }, 1, 5); }
        catch (IndexOutOfBoundsException e) { oob = true; }
        catch (NegativeArraySizeException e) { oob = true; }
        check(oob, "offset+length beyond array -> IndexOutOfBounds family");
        oob = false;
        try { new BinaryRefAddr("t", new byte[] { 1, 2 }, -1, 1); }
        catch (IndexOutOfBoundsException | NegativeArraySizeException e) { oob = true; }
        check(oob, "negative offset -> IndexOutOfBounds family");
    }

    static void dBraSerialization() throws Exception {
        BinaryRefAddr b = new BinaryRefAddr("t", new byte[] { 1, 2, 3 }, 1, 2);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(b); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            BinaryRefAddr back = (BinaryRefAddr) oi.readObject();
            check(back.equals(b), "round trip equal");
            check("t".equals(back.getType()), "type survives");
            byte[] out = (byte[]) back.getContent();
            check(out.length == 2 && out[0] == 2, "sliced bytes survive");
        }
    }

}
