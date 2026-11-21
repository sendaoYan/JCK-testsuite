/*
 * @test
 * @summary JVMS 6.3: OutOfMemoryError is thrown when insufficient memory is
 *          available to satisfy an allocation, and the VM remains usable after
 *          the error is thrown.
 * @run main OutOfMemoryErrorTest
 */

public class OutOfMemoryErrorTest {
    public static void main(String[] args) {
        // JVMS 6.3 "Virtual Machine Errors": "OutOfMemoryError is thrown if
        // insufficient memory is available to satisfy an allocation request."
        // The oversized array (Integer.MAX_VALUE/2 longs ~ 8 GB) exceeds every
        // test JVM heap cap used by the harness (<= 4096m), so the allocation
        // must fail with OutOfMemoryError regardless of the box's RAM.
        boolean oom = false;
        try {
            long[] huge = new long[Integer.MAX_VALUE / 2];
            huge[0] = 1L; // keep the compiler honest; unreachable under the caps
        } catch (OutOfMemoryError expected) {
            oom = true;
        }
        if (!oom) {
            throw new AssertionError("no OutOfMemoryError thrown for oversized allocation");
        }

        // JVMS 6.3 describes the error as recoverable: the VM must remain usable.
        byte[] small = new byte[4096];
        small[0] = 42;
        if (small[0] != 42) {
            throw new AssertionError("VM not usable after OutOfMemoryError");
        }

        System.out.println("PASS");
    }
}
