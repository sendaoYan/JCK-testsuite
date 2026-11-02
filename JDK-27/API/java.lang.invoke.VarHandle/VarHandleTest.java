/*
 * @test
 * @summary java.lang.invoke.VarHandle conformance: array element get/set/cas and access modes.
 * @run main VarHandleTest
 */
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class VarHandleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        VarHandle vh = MethodHandles.arrayElementVarHandle(int[].class);
        int[] arr = {1, 2, 3};
        check(((Integer) vh.get(arr, 0)).intValue() == 1, "get");
        vh.set(arr, 0, 10);
        check(arr[0] == 10, "set");
        check(vh.compareAndSet(arr, 0, 10, 20), "compareAndSet");
        check(arr[0] == 20, "cas applied");
        check(vh.varType() == int.class, "varType");
        check(vh.coordinateTypes().size() == 2, "coordinateTypes");
        check(vh.isAccessModeSupported(VarHandle.AccessMode.GET), "supports GET");
        check(vh.toMethodHandle(VarHandle.AccessMode.GET) != null, "toMethodHandle");
        check(vh.accessModeType(VarHandle.AccessMode.SET) != null, "accessModeType");
        VarHandle.fullFence(); VarHandle.acquireFence(); VarHandle.releaseFence();
        passed++;
        System.out.println("PASS (" + passed + " assertions)");
    }
}
