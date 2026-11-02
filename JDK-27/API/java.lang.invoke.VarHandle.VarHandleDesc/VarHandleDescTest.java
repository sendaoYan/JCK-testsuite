/*
 * @test
 * @summary java.lang.invoke.VarHandle.VarHandleDesc conformance: ofField/ofStaticField/ofArray and resolveConstantDesc.
 * @run main VarHandleDescTest
 */
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class VarHandleDescTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class Holder { public int x = 7; }
    public static void main(String[] args) throws Exception {
        ClassDesc holder = Holder.class.describeConstable().get();
        ClassDesc intT = int.class.describeConstable().get();
        VarHandle.VarHandleDesc field = VarHandle.VarHandleDesc.ofField(holder, "x", intT);
        check(field.varType().equals(intT), "field varType");
        VarHandle vh = (VarHandle) field.resolveConstantDesc(MethodHandles.lookup());
        check(((Integer) vh.get(new Holder())).intValue() == 7, "field resolve");
        check(VarHandle.VarHandleDesc.ofStaticField(holder, "x", intT) != null, "ofStaticField");
        VarHandle.VarHandleDesc arr = VarHandle.VarHandleDesc.ofArray(int[].class.describeConstable().get());
        check(arr.varType().descriptorString().equals("I"), "ofArray varType component int");
        VarHandle avh = (VarHandle) arr.resolveConstantDesc(MethodHandles.lookup());
        check(((Integer) avh.get(new int[]{5}, 0)).intValue() == 5, "array resolve");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
