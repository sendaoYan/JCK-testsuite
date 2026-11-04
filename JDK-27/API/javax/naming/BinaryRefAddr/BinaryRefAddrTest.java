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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
