/*
 * @test
 * @summary java.lang.invoke.ConstantBootstraps conformance: static bootstrap methods.
 * @run main ConstantBootstrapsTest
 */
import java.lang.invoke.ConstantBootstraps;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class ConstantBootstrapsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class Holder { public int x = 7; }
    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        check(ConstantBootstraps.nullConstant(lookup, "x", Object.class) == null, "nullConstant");
        check(ConstantBootstraps.enumConstant(lookup, "SECONDS", TimeUnit.class) == TimeUnit.SECONDS, "enumConstant");
        check(((Integer) ConstantBootstraps.getStaticFinal(lookup, "MAX_VALUE", int.class)).intValue() == Integer.MAX_VALUE, "getStaticFinal");
        check("hello".equals(ConstantBootstraps.explicitCast(lookup, "x", String.class, "hello")), "explicitCast");
        VarHandle fvh = ConstantBootstraps.fieldVarHandle(lookup, "x", VarHandle.class, Holder.class, int.class);
        check(fvh != null && ((Integer) fvh.get(new Holder())).intValue() == 7, "fieldVarHandle");
        VarHandle avh = ConstantBootstraps.arrayVarHandle(lookup, "x", VarHandle.class, int[].class);
        check(avh != null && ((Integer) avh.get(new int[]{9}, 0)).intValue() == 9, "arrayVarHandle");
        boolean hasPrimitive = false;
        for (Method m : ConstantBootstraps.class.getMethods()) if (m.getName().equals("primitiveClass")) hasPrimitive = true;
        check(hasPrimitive, "primitiveClass method present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
