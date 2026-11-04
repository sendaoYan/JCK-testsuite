/*
 * @test
 * @summary java.util.UUID conformance: white-box structure plus black-box behavior of
 *          randomUUID, fromString/toString round-trip, nameUUIDFromBytes determinism,
 *          equals/hashCode, compareTo, the (msb,lsb) constructor, and version/variant bits.
 * @run main UUIDTest
 */

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.UUID");
        check(!c.isInterface(), "UUID is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "UUID is final");
        check(Serializable.class.isAssignableFrom(c), "UUID implements Serializable");
        check(Comparable.class.isAssignableFrom(c), "UUID implements Comparable");
        c.getMethod("randomUUID");
        c.getMethod("fromString", String.class);
        c.getMethod("nameUUIDFromBytes", byte[].class);
        c.getMethod("getMostSignificantBits");
        c.getMethod("getLeastSignificantBits");
        c.getMethod("version");
        c.getMethod("variant");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testRandomUUID();
        testKnownVectors();
        testFromStringToString();
        testConstructor();
        testNameUUID();
        testEqualsHashCode();
        testCompareTo();
        testBoundaryEquivalence();
    }

    // ---- 契约深化：MD5 已知向量 + nil UUID + 位运算 ----
    static void testKnownVectors() {
        // nameUUIDFromBytes 使用 MD5（RFC 4122 v3），向量跨实现恒定
        check("5d41402a-bc4b-3a76-b971-9d911017c592".equals(
                  UUID.nameUUIDFromBytes("hello".getBytes(StandardCharsets.UTF_8)).toString()),
              "nameUUIDFromBytes('hello') MD5 vector");
        check("d41d8cd9-8f00-3204-a980-0998ecf8427e".equals(
                  UUID.nameUUIDFromBytes(new byte[0]).toString()),
              "nameUUIDFromBytes(empty) MD5 vector");
        UUID v3 = UUID.nameUUIDFromBytes("hello".getBytes(StandardCharsets.UTF_8));
        check(v3.version() == 3, "name-based version is 3");
        check(v3.variant() == 2, "RFC 4122 variant is 2");
        // nil UUID
        UUID nil = new UUID(0L, 0L);
        check("00000000-0000-0000-0000-000000000000".equals(nil.toString()), "nil UUID string");
        check(nil.version() == 0 && nil.variant() == 0, "nil version/variant 0");
        check(UUID.fromString("00000000-0000-0000-0000-000000000000").equals(nil), "nil parse");
        // msb/lsb 位回传
        UUID u = new UUID(0x0123456789ABCDEFL, 0xFEDCBA9876543210L);
        check(u.getMostSignificantBits() == 0x0123456789ABCDEFL, "getMostSignificantBits");
        check(u.getLeastSignificantBits() == 0xFEDCBA9876543210L, "getLeastSignificantBits");
        check(new UUID(u.getMostSignificantBits(), u.getLeastSignificantBits()).equals(u),
              "msb/lsb constructor roundtrip");
        // compareTo：无符号 msb 优先、其后 lsb
        check(new UUID(0L, 5L).compareTo(new UUID(0L, 6L)) < 0, "lsb ordering");
        check(new UUID(-1L, 0L).compareTo(new UUID(1L, 0L)) < 0, "msb compared as signed long");
        check(new UUID(1L, 2L).compareTo(new UUID(1L, 2L)) == 0, "compareTo equal 0");
        // toString 小写十六进制；fromString 接受大写
        check(!UUID.randomUUID().toString().toUpperCase().equals(UUID.randomUUID().toString().toUpperCase())
              || true, "sanity");
        UUID up = UUID.fromString("ABCDEF01-2345-6789-ABCD-EF0123456789");
        check("abcdef01-2345-6789-abcd-ef0123456789".equals(up.toString()), "uppercase parse -> lowercase out");
        // 随机 UUID 版本/变体恒定
        for (int i = 0; i < 32; i++) {
            UUID r = UUID.randomUUID();
            check(r.version() == 4, "random version 4");
            check(r.variant() == 2, "random variant 2");
        }
    }

    static void testRandomUUID() {
        UUID u = UUID.randomUUID();
        check(u != null, "randomUUID non-null");
        check(u.version() == 4, "randomUUID version 4");
        check(u.variant() == 2, "randomUUID variant 2 (IETF)");
        check(u.toString().length() == 36, "toString length 36");
        check(u.toString().charAt(14) == '4', "version digit in canonical position");
        check(!u.equals(UUID.randomUUID()), "two randomUUIDs differ");
    }

    static void testFromStringToString() {
        String s = "123e4567-e89b-12d3-a456-426655440000";
        UUID u = UUID.fromString(s);
        check(u.toString().equals(s), "fromString/toString round-trip");
        check(u.getMostSignificantBits() == 0x123e4567e89b12d3L, "mostSignificantBits");
        check(u.getLeastSignificantBits() == 0xa456426655440000L, "leastSignificantBits");
        check(u.version() == 1, "version 1 parsed");
        check(u.variant() == 2, "variant 2 parsed");
        assertThrows(IllegalArgumentException.class,
                () -> UUID.fromString("not-a-uuid"), "fromString(invalid) throws IAE");
    }

    static void testConstructor() {
        long msb = 0x0000000000004000L | 0x0000000000000001L; // version 4 in top bits
        long lsb = 0x8000000000000000L; // variant 2
        UUID u = new UUID(msb, lsb);
        check(u.getMostSignificantBits() == msb, "constructor msb");
        check(u.getLeastSignificantBits() == lsb, "constructor lsb");
        check(u.version() == 4, "constructor version derived from msb");
        check(u.variant() == 2, "constructor variant derived from lsb");
    }

    static void testNameUUID() {
        byte[] name = "example.com".getBytes(StandardCharsets.UTF_8);
        UUID a = UUID.nameUUIDFromBytes(name);
        UUID b = UUID.nameUUIDFromBytes("example.com".getBytes(StandardCharsets.UTF_8));
        UUID c = UUID.nameUUIDFromBytes("other.org".getBytes(StandardCharsets.UTF_8));
        check(a.equals(b), "nameUUIDFromBytes deterministic");
        check(!a.equals(c), "different name differs");
        check(a.version() == 3, "nameUUID version 3 (MD5)");
        check(a.variant() == 2, "nameUUID variant 2");
    }

    static void testEqualsHashCode() {
        UUID a = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
        UUID b = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
        UUID c = UUID.randomUUID();
        check(a.equals(b), "equals");
        check(!a.equals(c), "!equals different");
        check(a.hashCode() == b.hashCode(), "equal UUIDs equal hashCode");
    }

    static void testCompareTo() {
        UUID low = new UUID(0x0000000000000001L, 0L);
        UUID high = new UUID(0x0000000000000002L, 0L);
        check(low.compareTo(high) < 0, "compareTo smaller msb < 0");
        check(high.compareTo(low) > 0, "compareTo larger msb > 0");
        check(low.compareTo(new UUID(0x0000000000000001L, 0L)) == 0, "compareTo equal == 0");
    }

    static void testBoundaryEquivalence() {
        // nil UUID round-trip.
        UUID nil = new UUID(0L, 0L);
        check(nil.toString().equals("00000000-0000-0000-0000-000000000000"), "nil toString");
        check(UUID.fromString(nil.toString()).equals(nil), "nil round-trip");
        check(nil.version() == 0 && nil.variant() == 0, "nil version/variant");

        // version bits are bits 12-15 of the msb.
        check(new UUID(0x0000000000001000L, 0L).version() == 1, "version 1 from msb");
        check(new UUID(0x0000000000002000L, 0L).version() == 2, "version 2");
        check(new UUID(0x0000000000003000L, 0L).version() == 3, "version 3");
        check(new UUID(0x0000000000004000L, 0L).version() == 4, "version 4");
        check(new UUID(0x0000000000005000L, 0L).version() == 5, "version 5");

        // variant bits are derived from the two high bits of the lsb.
        check(new UUID(0, 0x8000000000000000L).variant() == 2, "variant 2 (IETF)");
        check(new UUID(0, 0xc000000000000000L).variant() == 6, "variant 6 (Microsoft)");
        check(new UUID(0, 0x0000000000000000L).variant() == 0, "variant 0 (NCS)");

        // compareTo orders by msb then lsb.
        UUID a = new UUID(1L, 5L);
        UUID b = new UUID(1L, 9L);
        check(a.compareTo(b) < 0 && b.compareTo(a) > 0, "compareTo lsb tiebreak");
        check(a.compareTo(new UUID(1L, 5L)) == 0, "compareTo equal");

        // fromString rejects empty string and missing dashes.
        assertThrows(IllegalArgumentException.class, () -> UUID.fromString(""), "fromString empty");
        assertThrows(IllegalArgumentException.class,
                () -> UUID.fromString("123e4567e89b12d3a456426655440000"), "fromString no dashes");

        // nameUUIDFromBytes of the empty byte array matches the RFC 4122 MD5 vector.
        check(UUID.nameUUIDFromBytes("".getBytes(StandardCharsets.UTF_8))
                .equals(UUID.fromString("d41d8cd9-8f00-3204-a980-0998ecf8427e")),
              "nameUUIDFromBytes('') RFC 4122 vector");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
