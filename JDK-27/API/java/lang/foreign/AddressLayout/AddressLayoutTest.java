/*
 * @test
 * @summary java.lang.foreign.AddressLayout conformance: white-box structure + black-box behavior.
 * @run main/othervm --enable-native-access=ALL-UNNAMED AddressLayoutTest
 */
import java.lang.foreign.*;
import java.lang.reflect.*;
import java.nio.ByteOrder;
import java.util.Optional;

public class AddressLayoutTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.foreign.AddressLayout");
        check(c.isInterface(), "interface");
        check(ValueLayout.class.isAssignableFrom(c), "extends ValueLayout");
        check(c.getMethod("withTargetLayout", MemoryLayout.class).getReturnType() == AddressLayout.class,
              "withTargetLayout returns AddressLayout");
        check(c.getMethod("withoutTargetLayout").getReturnType() == AddressLayout.class,
              "withoutTargetLayout returns AddressLayout");
        check(c.getMethod("targetLayout").getReturnType() == Optional.class, "targetLayout returns Optional");
        check(c.getMethod("withName", String.class).getReturnType() == AddressLayout.class, "withName covariant");
        check(c.getMethod("withOrder", ByteOrder.class).getReturnType() == AddressLayout.class, "withOrder covariant");
        check(c.getMethod("withByteAlignment", long.class).getReturnType() == AddressLayout.class,
              "withByteAlignment covariant");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AddressLayout a = ValueLayout.ADDRESS;
        check(a.byteSize() == 4 || a.byteSize() == 8, "address byteSize is pointer width (4 or 8)");
        check(a.byteAlignment() == a.byteSize(), "address alignment equals size");
        check(a.order() == ByteOrder.nativeOrder(), "default order is native order");
        check(a.carrier() == MemorySegment.class, "address carrier is MemorySegment");
        check(a.targetLayout().isEmpty(), "plain ADDRESS has no target layout");

        AddressLayout t = a.withTargetLayout(ValueLayout.JAVA_INT);
        check(t.targetLayout().isPresent() && t.targetLayout().get() == ValueLayout.JAVA_INT,
              "withTargetLayout sets target layout");
        check(t.withoutTargetLayout().targetLayout().isEmpty(), "withoutTargetLayout clears target");

        AddressLayout be = a.withOrder(ByteOrder.BIG_ENDIAN);
        check(be.order() == ByteOrder.BIG_ENDIAN, "withOrder applies order");
        check(a.withOrder(ByteOrder.nativeOrder()).order() == ByteOrder.nativeOrder(), "withOrder roundtrip");

        AddressLayout named = a.withName("ptr");
        check("ptr".equals(named.name().orElse(null)), "withName sets name");
        check(named.withoutName().name().isEmpty(), "withoutName clears name");

        AddressLayout aligned = a.withByteAlignment(a.byteAlignment());
        check(aligned.byteAlignment() == a.byteAlignment(), "withByteAlignment preserves natural alignment");
        check(a.equals(a.withTargetLayout(ValueLayout.JAVA_INT).withoutTargetLayout()),
              "withTargetLayout/withoutTargetLayout roundtrip equals original");
    }

    // ---- Part 3: deep contract (pointer store/load + target-layout dereference) ----
    static void testDeepContract() {
        try (Arena arena = Arena.ofConfined()) {
            // store a segment address and load it back: identity by (address, scope)
            MemorySegment pointee = arena.allocate(8, 8);
            pointee.set(ValueLayout.JAVA_INT, 0, 1234);
            MemorySegment holder = arena.allocate(8, 8);
            holder.set(ValueLayout.ADDRESS, 0, pointee);
            MemorySegment loaded = holder.get(ValueLayout.ADDRESS, 0);
            // a bare ADDRESS load yields a ZERO-length segment at that address (measured)
            check(loaded.address() == pointee.address(),
                  "loaded address equals the stored segment address");
            check(loaded.byteSize() == 0,
                  "bare ADDRESS load yields a zero-length segment (no target layout)");
            check(loaded.scope().isAlive(),
                  "loaded segment carries a live scope (no dangling pointers)");

            // withTargetLayout turns the load into a typed dereference
            AddressLayout intPtr = ValueLayout.ADDRESS.withTargetLayout(ValueLayout.JAVA_INT);
            MemorySegment deref = holder.get(intPtr, 0);
            check(deref.byteSize() == 4, "target layout sizes the dereferenced view to 4");
            check(deref.get(ValueLayout.JAVA_INT, 0) == 1234,
                  "dereferenced view reads the pointee's value");
            // targetLayout() Optional round trip; equality excludes target-cleared views
            check(intPtr.targetLayout().isPresent()
                          && intPtr.targetLayout().get() == ValueLayout.JAVA_INT,
                  "targetLayout() reads back the layout");
            check(!intPtr.equals(ValueLayout.ADDRESS),
                  "target layout participates in equals");
            check(intPtr.withoutTargetLayout().equals(ValueLayout.ADDRESS),
                  "withoutTargetLayout restores value equality");

            // setAtIndex/getAtIndex for address arrays
            MemorySegment arr = arena.allocate(16, 8);
            arr.setAtIndex(ValueLayout.ADDRESS, 1, pointee);
            check(arr.getAtIndex(ValueLayout.ADDRESS, 1).address() == pointee.address(),
                  "setAtIndex/getAtIndex(ADDRESS, 1) round-trips");
            // storing MemorySegment.NULL and reading it back
            arr.setAtIndex(ValueLayout.ADDRESS, 0, MemorySegment.NULL);
            check(arr.getAtIndex(ValueLayout.ADDRESS, 0).address() == 0,
                  "NULL pointer round-trips through an address slot");
            // ADDRESS_UNALIGNED allows byte-slotted pointers
            MemorySegment odd = arena.allocate(9, 8);
            odd.set(ValueLayout.ADDRESS_UNALIGNED.withTargetLayout(ValueLayout.JAVA_INT), 1, pointee);
            check(odd.get(ValueLayout.ADDRESS_UNALIGNED.withTargetLayout(ValueLayout.JAVA_INT), 1)
                            .get(ValueLayout.JAVA_INT, 0) == 1234,
                  "unaligned address with target layout dereferences at offset 1");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
