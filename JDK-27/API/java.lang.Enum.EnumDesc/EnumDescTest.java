/*
 * @test
 * @summary java.lang.Enum.EnumDesc conformance: white-box structure + black-box behavior.
 * @run main EnumDescTest
 */
import java.lang.Enum.EnumDesc;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

public class EnumDescTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Enum$EnumDesc");
        check(Modifier.isFinal(c.getModifiers()), "EnumDesc is final");
        check(c.getSuperclass().getName().equals("java.lang.constant.DynamicConstantDesc"),
              "extends DynamicConstantDesc");
        check(ConstantDesc.class.isAssignableFrom(c), "is a ConstantDesc");
        Method of = c.getMethod("of", ClassDesc.class, String.class);
        check(Modifier.isStatic(of.getModifiers()) && Modifier.isPublic(of.getModifiers()),
              "of(ClassDesc, String) is public static");
        check(c.getMethod("resolveConstantDesc", MethodHandles.Lookup.class) != null,
              "resolveConstantDesc(Lookup) declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static ClassDesc tu = ClassDesc.of("java.util.concurrent.TimeUnit");

    static void testOfAndResolve() throws Exception {
        EnumDesc<TimeUnit> desc = EnumDesc.of(tu, "SECONDS");
        check(desc != null, "of non-null");
        check("SECONDS".equals(desc.constantName()), "constantName is SECONDS");
        check(desc.toString().contains("SECONDS"), "toString contains constant name");
        check(desc.toString().contains("TimeUnit"), "toString contains enum type");
        check(desc.resolveConstantDesc(MethodHandles.lookup()) == TimeUnit.SECONDS,
              "resolveConstantDesc resolves the constant");
        check((Object) desc.resolveConstantDesc(MethodHandles.lookup()) == TimeUnit.SECONDS,
              "resolveConstantDesc is stable across calls");
    }

    static void testBadResolve() throws Exception {
        EnumDesc<TimeUnit> bad = EnumDesc.of(tu, "NO_SUCH_CONSTANT");
        try {
            bad.resolveConstantDesc(MethodHandles.lookup());
            throw new AssertionError("resolve missing constant: expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    static void testEquals() throws Exception {
        check(EnumDesc.of(tu, "SECONDS").equals(EnumDesc.of(tu, "SECONDS")),
              "equal descriptors are equal");
        check(!EnumDesc.of(tu, "SECONDS").equals(EnumDesc.of(tu, "MINUTES")),
              "different constants differ");
        check(EnumDesc.of(tu, "SECONDS").hashCode() == EnumDesc.of(tu, "SECONDS").hashCode(),
              "hashCode consistent with equals");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testOfAndResolve();
        testBadResolve();
        testEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
