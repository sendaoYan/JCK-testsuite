/*
 * @test
 * @summary java.security.spec.ECField conformance: full contract coverage.
 *          Interface shape (single method getFieldSize, no super-interfaces),
 *          implementor matrix (ECFieldFp/ECFieldF2m implement, unrelated spec
 *          types do not), polymorphic getFieldSize dispatch across implementations,
 *          and reflection-level modifiers.
 * @run main ECFieldTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;

public class ECFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.spec.ECField");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getFieldSize"), "declares getFieldSize");

        // ---- interface shape: exactly one declared method, no super-interfaces ----
        check(in.isEmpty(), "no super-interfaces (standalone marker-plus contract)");
        check(ms.size() == 1, "exactly one declared method, found " + ms);
        Method fs = c.getDeclaredMethod("getFieldSize");
        check(fs.getReturnType() == int.class, "getFieldSize returns int");
        check(Modifier.isPublic(fs.getModifiers()) && Modifier.isAbstract(fs.getModifiers()),
                "getFieldSize public abstract");
        check(fs.getParameterTypes().length == 0, "getFieldSize no parameters");
        check(Modifier.isPublic(c.getModifiers()), "interface public");

        // ---- implementor matrix ----
        check(ECField.class.isAssignableFrom(ECFieldFp.class), "ECFieldFp implements ECField");
        check(ECField.class.isAssignableFrom(ECFieldF2m.class), "ECFieldF2m implements ECField");
        check(!ECField.class.isAssignableFrom(EllipticCurve.class), "EllipticCurve does not implement ECField");
        check(!ECField.class.isAssignableFrom(ECParameterSpec.class), "ECParameterSpec does not implement ECField");
        check(!ECField.class.isAssignableFrom(BigInteger.class), "BigInteger unrelated");

        // ---- polymorphic dispatch ----
        ECField fp = new ECFieldFp(new BigInteger("23"));
        ECField f2 = new ECFieldF2m(113);
        check(fp.getFieldSize() == 5, "ECField reference -> Fp.getFieldSize");
        check(f2.getFieldSize() == 113, "ECField reference -> F2m.getFieldSize");

        // ---- arrays of interface type accept both implementations ----
        ECField[] fields = {fp, f2, new ECFieldF2m(163, new int[]{7, 2, 1})};
        int sum = 0;
        for (ECField f : fields) sum += f.getFieldSize();
        check(sum == 5 + 113 + 163, "polymorphic sum over ECField[]");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
