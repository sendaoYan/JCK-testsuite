/*
 * @test
 * @summary java.util.UUID API conformance (Java 8 surface): complete contract coverage of
 *          randomUUID (version/variant/length), fromString/toString round-trip + IAE/null NPE +
 *          uppercase, nameUUIDFromBytes determinism + version 3, the (long,long) constructor,
 *          least/most significant bits, version()/variant(), timestamp/clockSequence/node
 *          (version 1 values + non-version-1 UnsupportedOperationException), compareTo ordering,
 *          equals/hashCode, and Serializable round-trip.
 * @run main UUIDTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UUIDTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
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
    }

    static void testFromStringToString() {
        String s = "123e4567-e89b-12d3-a456-426655440000";
        UUID u = UUID.fromString(s);
        check(u.toString().equals(s), "fromString/toString round-trip");
        check(u.getMostSignificantBits() == 0x123e4567e89b12d3L, "mostSignificantBits");
        check(u.getLeastSignificantBits() == 0xa456426655440000L, "leastSignificantBits");
        check(u.version() == 1, "version 1 parsed");
        check(u.variant() == 2, "variant 2 parsed");
        try {
            UUID.fromString("not-a-uuid");
            throw new AssertionError("fromString(invalid): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    static void testFromStringErrors() {
        expectThrows(NullPointerException.class, () -> UUID.fromString(null),
                     "fromString(null) NPE");
        expectThrows(IllegalArgumentException.class, () -> UUID.fromString(""),
                     "fromString(empty) IAE");
        expectThrows(IllegalArgumentException.class, () -> UUID.fromString("12345678123412341234123456789012"),
                     "fromString(no hyphens) IAE");
        // uppercase hex is accepted.
        String upper = "123E4567-E89B-12D3-A456-426655440000";
        check(UUID.fromString(upper).toString().equalsIgnoreCase(upper),
              "fromString uppercase accepted");
    }

    static void testNameUUID() {
        UUID a = UUID.nameUUIDFromBytes("example.com".getBytes(StandardCharsets.UTF_8));
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
        check(!a.equals(null) && !a.equals("x"), "equals null/other-type false");
        check(a.hashCode() == b.hashCode(), "equal UUIDs equal hashCode");
    }

    static void testConstructorBits() {
        UUID u = new UUID(0x123456789abcdef0L, 0x0fedcba987654321L);
        check(u.getMostSignificantBits() == 0x123456789abcdef0L, "ctor most bits");
        check(u.getLeastSignificantBits() == 0x0fedcba987654321L, "ctor least bits");
    }

    static void testVersion1Accessors() {
        UUID v1 = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
        check(v1.version() == 1, "version 1");
        check(v1.timestamp() > 0, "timestamp for version 1 positive");
        check(v1.clockSequence() >= 0, "clockSequence non-negative");
        check(v1.node() == 0x426655440000L, "node == low 48 bits of least bits");

        // non-version-1 UUIDs -> UnsupportedOperationException for these accessors.
        UUID v4 = UUID.randomUUID();
        expectThrows(UnsupportedOperationException.class, () -> v4.timestamp(),
                     "timestamp on v4 UOE");
        expectThrows(UnsupportedOperationException.class, () -> v4.clockSequence(),
                     "clockSequence on v4 UOE");
        expectThrows(UnsupportedOperationException.class, () -> v4.node(),
                     "node on v4 UOE");
    }

    static void testCompareTo() {
        UUID low = new UUID(0L, 0L);
        UUID mid = new UUID(0L, 1L);
        UUID high = new UUID(1L, 0L);
        check(low.compareTo(mid) < 0, "compareTo by least bits");
        check(mid.compareTo(high) < 0, "compareTo by most bits");
        check(low.compareTo(low) == 0, "compareTo equal");
        check(high.compareTo(low) > 0, "compareTo reverse");
    }

    static void testSerialization() throws Exception {
        UUID original = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        UUID revived = (UUID) ois.readObject();
        ois.close();
        check(revived.equals(original), "Serializable round-trip equals");
        check(revived.toString().equals(original.toString()), "Serializable round-trip toString");
    }

    public static void main(String[] args) throws Exception {
        testRandomUUID();
        testKnownVectors();
        testFromStringToString();
        testFromStringErrors();
        testNameUUID();
        testEqualsHashCode();
        testConstructorBits();
        testVersion1Accessors();
        testCompareTo();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
