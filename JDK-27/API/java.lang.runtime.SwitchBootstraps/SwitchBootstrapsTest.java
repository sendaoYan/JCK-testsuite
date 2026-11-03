/*
 * @test
 * @summary java.lang.runtime.SwitchBootstraps conformance: white-box structure + black-box behavior.
 * @run main SwitchBootstrapsTest
 */

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.lang.runtime.SwitchBootstraps;

public class SwitchBootstrapsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    enum Color {
        RED, GREEN, BLUE
    }

    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.runtime.SwitchBootstraps");
        check(Modifier.isFinal(c.getModifiers()), "SwitchBootstraps is final");
        check(Modifier.isPrivate(c.getDeclaredConstructor().getModifiers()),
              "private constructor");
        int publics = 0;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) publics++;
        }
        check(publics == 2, "two public static bootstrap methods");
        check(c.getDeclaredMethod("typeSwitch", MethodHandles.Lookup.class, String.class,
                                  MethodType.class, Object[].class).getReturnType() == CallSite.class,
              "typeSwitch returns CallSite");
        check(c.getDeclaredMethod("enumSwitch", MethodHandles.Lookup.class, String.class,
                                  MethodType.class, Object[].class).getReturnType() == CallSite.class,
              "enumSwitch returns CallSite");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        // typeSwitch: labels are Class objects or constants; result is label index,
        // or -1 for null, or labels.length (i.e. "default") for no match
        MethodType tsType = MethodType.methodType(int.class, Object.class, int.class);
        CallSite byClass = SwitchBootstraps.typeSwitch(LOOKUP, "", tsType,
                                                       String.class, Integer.class);
        MethodHandle ts = byClass.dynamicInvoker();
        check((int) ts.invoke((Object) "hi", 0) == 0, "String matches label 0");
        check((int) ts.invoke((Object) 42, 0) == 1, "Integer matches label 1");
        check((int) ts.invoke((Object) 3.5d, 0) == 2, "no match -> default index");
        check((int) ts.invoke((Object) null, 0) == -1, "null selector -> -1");
        check((int) ts.invoke((Object) "hi", 2) == 2, "start index beyond match returns default");

        // subtype semantics: Integer matches before Number if ordered so
        CallSite poly = SwitchBootstraps.typeSwitch(LOOKUP, "", tsType,
                                                    Number.class, Integer.class);
        MethodHandle ph = poly.dynamicInvoker();
        check((int) ph.invoke((Object) 7, 0) == 0, "Integer is a Number: first label wins");
        check((int) ph.invoke((Object) 3.5f, 0) == 0, "Float matches Number label too");

        // constant (non-Class) labels compare by equals
        CallSite byConst = SwitchBootstraps.typeSwitch(LOOKUP, "", tsType, "a", "b");
        MethodHandle bc = byConst.dynamicInvoker();
        check((int) bc.invoke((Object) "a", 0) == 0, "constant a matches");
        check((int) bc.invoke((Object) "b", 0) == 1, "constant b matches");
        check((int) bc.invoke((Object) "c", 0) == 2, "constant no match -> default");

        // enumSwitch: labels are the enum constant names
        MethodType esType = MethodType.methodType(int.class, Color.class, int.class);
        CallSite es = SwitchBootstraps.enumSwitch(LOOKUP, "", esType, new Object[]{"RED", "BLUE"});
        MethodHandle eh = es.dynamicInvoker();
        check((int) eh.invoke(Color.RED, 0) == 0, "RED matches");
        check((int) eh.invoke(Color.BLUE, 0) == 1, "BLUE matches");
        check((int) eh.invoke(Color.GREEN, 0) == 2, "GREEN -> default");
        check((int) eh.invoke(null, 0) == -1, "null enum -> -1");

        // wrong selector type in enumSwitch MethodType is rejected at bootstrap
        try {
            SwitchBootstraps.typeSwitch(LOOKUP, "", MethodType.methodType(int.class, Color.class, int.class),
                                        (Object[]) null);
            check(false, "typeSwitch with enum selector and null labels should throw");
        } catch (IllegalArgumentException | NullPointerException expected) {
            check(true, "typeSwitch null labels rejected");
        }

        // int selector is preserved across the start index: resume semantics
        CallSite multi = SwitchBootstraps.typeSwitch(LOOKUP, "", tsType,
                                                     String.class, String.class, Integer.class);
        MethodHandle mh = multi.dynamicInvoker();
        check((int) mh.invoke((Object) "x", 1) == 1, "resume from index 1 still matches String");
        check((int) mh.invoke((Object) "x", 3) == 3, "resume past all -> default");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
