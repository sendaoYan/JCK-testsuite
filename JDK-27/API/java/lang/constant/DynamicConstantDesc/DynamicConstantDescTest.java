/*
 * @test
 * @summary java.lang.constant.DynamicConstantDesc conformance: white-box + black-box behavior.
 * @run main DynamicConstantDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.List;

public class DynamicConstantDescTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** real bootstrap used by resolveConstantDesc: name + "/" + simple type name */
    public static Object dcBoot(MethodHandles.Lookup lookup, String name, Class<?> type) {
        return name + "/" + type.getSimpleName();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.constant.DynamicConstantDesc");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Class.forName("java.lang.constant.ConstantDesc").isAssignableFrom(c),
              "implements ConstantDesc");
        int protCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isProtected(ct.getModifiers())) protCtors++;
        }
        check(protCtors >= 1, "protected constructor for subclasses");
        for (String m : new String[]{"constantName", "constantType", "bootstrapMethod",
                                      "bootstrapArgs", "bootstrapArgsList", "resolveConstantDesc",
                                      "ofCanonical", "ofNamed", "of"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // factory + accessors
        DirectMethodHandleDesc bsm = ConstantDescs.BSM_NULL_CONSTANT;
        DynamicConstantDesc<?> d = DynamicConstantDesc.ofNamed(
                bsm, "nothing", ConstantDescs.CD_Object);
        check(d.constantName().equals("nothing"), "constantName");
        check(d.constantType().descriptorString().equals("Ljava/lang/Object;"), "constantType");
        check(d.bootstrapMethod().equals(bsm), "bootstrapMethod");
        check(d.bootstrapArgs().length == 0, "no bootstrap args");
        check(d.bootstrapArgsList().equals(List.of()), "bootstrapArgsList empty");

        // of(bsm) and of(bsm, args...) default the name to "_" and type to CD_Object
        DynamicConstantDesc<?> anon = DynamicConstantDesc.of(bsm);
        check(anon.constantName().equals("_"), "default name _");
        check(anon.constantType().equals(ConstantDescs.CD_Object), "default type Object");
        DynamicConstantDesc<?> withArg = DynamicConstantDesc.of(bsm, "a1");
        check(withArg.bootstrapArgs().length == 1 && withArg.bootstrapArgs()[0].equals("a1"),
              "of(bsm, args...) keeps args");
        check(withArg.bootstrapArgsList().size() == 1, "bootstrapArgsList view");

        // resolveConstantDesc: NULL bootstrap resolves to null
        check(anon.resolveConstantDesc(lookup) == null, "nullConstant resolves to null");

        // resolveConstantDesc with a user bootstrap: name and type reach the bootstrap
        DirectMethodHandleDesc myBsm = MethodHandleDesc.ofMethod(
                DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("DynamicConstantDescTest"),
                "dcBoot", MethodTypeDesc.ofDescriptor(
                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
        DynamicConstantDesc<String> mine = DynamicConstantDesc.ofNamed(
                myBsm, "abc", ConstantDescs.CD_String);
        check(mine.resolveConstantDesc(lookup).equals("abc/String"),
              "user bootstrap receives name and type");

        // well-known canonical constants
        check(ConstantDescs.TRUE.resolveConstantDesc(lookup) == Boolean.TRUE, "TRUE resolves");
        check(ConstantDescs.FALSE.resolveConstantDesc(lookup) == Boolean.FALSE, "FALSE resolves");
        // ofCanonical maps the NULL bootstrap back onto the shared NULL constant
        ConstantDesc canonical = DynamicConstantDesc.ofCanonical(bsm, ConstantDescs.DEFAULT_NAME,
                ConstantDescs.CD_Object, new ConstantDesc[0]);
        check(canonical.equals(ConstantDescs.NULL), "ofCanonical NULL -> shared NULL");

        // value semantics
        DynamicConstantDesc<?> d2 = DynamicConstantDesc.ofNamed(
                bsm, "nothing", ConstantDescs.CD_Object);
        check(d.equals(d2), "equals by value");
        check(d.hashCode() == d2.hashCode(), "hashCode consistent");
        check(!d.equals(DynamicConstantDesc.ofNamed(bsm, "other", ConstantDescs.CD_Object)),
              "name participates in equals");
        check(!d.equals(DynamicConstantDesc.ofNamed(bsm, "nothing", ConstantDescs.CD_String)),
              "type participates in equals");
        check(!d.equals(null), "!equals null");

        // toString names bootstrap, name and type
        String ts = d.toString();
        check(ts.contains("nullConstant") && ts.contains("nothing"), "toString content: " + ts);
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
