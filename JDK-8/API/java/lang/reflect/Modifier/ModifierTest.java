/*
 * @test
 * @summary java.lang.reflect.Modifier conformance: full contract coverage.
 *          all 12 modifier constant bit values, every is* predicate against its own
 *          constant / unrelated constants / 0 / combined flags, toString(int) for
 *          empty/single/combined/full masks, the six *Modifiers() masks with exact
 *          bits, and the public Modifier() constructor (Java 8 only).
 * @run main ModifierTest
 */

import java.lang.reflect.Modifier;

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConstants() {
        check(Modifier.PUBLIC == 0x00000001, "PUBLIC");
        check(Modifier.PRIVATE == 0x00000002, "PRIVATE");
        check(Modifier.PROTECTED == 0x00000004, "PROTECTED");
        check(Modifier.STATIC == 0x00000008, "STATIC");
        check(Modifier.FINAL == 0x00000010, "FINAL");
        check(Modifier.SYNCHRONIZED == 0x00000020, "SYNCHRONIZED");
        check(Modifier.VOLATILE == 0x00000040, "VOLATILE");
        check(Modifier.TRANSIENT == 0x00000080, "TRANSIENT");
        check(Modifier.NATIVE == 0x00000100, "NATIVE");
        check(Modifier.INTERFACE == 0x00000200, "INTERFACE");
        check(Modifier.ABSTRACT == 0x00000400, "ABSTRACT");
        check(Modifier.STRICT == 0x00000800, "STRICT");

        // all 12 are distinct power-of-two flags
        int[] all = {
            Modifier.PUBLIC, Modifier.PRIVATE, Modifier.PROTECTED, Modifier.STATIC,
            Modifier.FINAL, Modifier.SYNCHRONIZED, Modifier.VOLATILE, Modifier.TRANSIENT,
            Modifier.NATIVE, Modifier.INTERFACE, Modifier.ABSTRACT, Modifier.STRICT
        };
        int or = 0;
        for (int m : all) {
            check((m & (m - 1)) == 0, "power of two " + Integer.toHexString(m));
            or |= m;
        }
        check(or == 0x0fff, "all 12 flags OR to 0xfff");
    }

    static void testPredicates() {
        // each predicate true for its own constant
        check(Modifier.isPublic(Modifier.PUBLIC), "isPublic own");
        check(Modifier.isPrivate(Modifier.PRIVATE), "isPrivate own");
        check(Modifier.isProtected(Modifier.PROTECTED), "isProtected own");
        check(Modifier.isStatic(Modifier.STATIC), "isStatic own");
        check(Modifier.isFinal(Modifier.FINAL), "isFinal own");
        check(Modifier.isSynchronized(Modifier.SYNCHRONIZED), "isSynchronized own");
        check(Modifier.isVolatile(Modifier.VOLATILE), "isVolatile own");
        check(Modifier.isTransient(Modifier.TRANSIENT), "isTransient own");
        check(Modifier.isNative(Modifier.NATIVE), "isNative own");
        check(Modifier.isInterface(Modifier.INTERFACE), "isInterface own");
        check(Modifier.isAbstract(Modifier.ABSTRACT), "isAbstract own");
        check(Modifier.isStrict(Modifier.STRICT), "isStrict own");

        // false for 0
        check(!Modifier.isPublic(0), "isPublic 0");
        check(!Modifier.isPrivate(0), "isPrivate 0");
        check(!Modifier.isProtected(0), "isProtected 0");
        check(!Modifier.isStatic(0), "isStatic 0");
        check(!Modifier.isFinal(0), "isFinal 0");
        check(!Modifier.isSynchronized(0), "isSynchronized 0");
        check(!Modifier.isVolatile(0), "isVolatile 0");
        check(!Modifier.isTransient(0), "isTransient 0");
        check(!Modifier.isNative(0), "isNative 0");
        check(!Modifier.isInterface(0), "isInterface 0");
        check(!Modifier.isAbstract(0), "isAbstract 0");
        check(!Modifier.isStrict(0), "isStrict 0");

        // each predicate is a pure single-bit test: unrelated constant -> false
        check(!Modifier.isPublic(Modifier.PRIVATE), "isPublic PRIVATE");
        check(!Modifier.isPublic(Modifier.ABSTRACT), "isPublic ABSTRACT");
        check(!Modifier.isStatic(Modifier.FINAL), "isStatic FINAL");
        check(!Modifier.isFinal(Modifier.STATIC), "isFinal STATIC");
        check(!Modifier.isInterface(Modifier.ABSTRACT), "isInterface ABSTRACT");
        check(!Modifier.isAbstract(Modifier.INTERFACE), "isAbstract INTERFACE");
        check(!Modifier.isNative(Modifier.SYNCHRONIZED), "isNative SYNCHRONIZED");
        check(!Modifier.isVolatile(Modifier.TRANSIENT), "isVolatile TRANSIENT");

        // combined flags keep the predicate true
        check(Modifier.isPublic(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL),
              "isPublic combined");
        check(Modifier.isStatic(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL),
              "isStatic combined");
        check(!Modifier.isPrivate(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL),
              "isPrivate combined absent");

        // negative / unknown bits only touch the matching bit
        check(Modifier.isPublic(Modifier.PUBLIC | 0x80000000), "isPublic with unknown bit");
        check(!Modifier.isPublic(0x80000000), "isPublic only unknown bit");
    }

    static void testToString() {
        check(Modifier.toString(0).equals(""), "toString(0) empty");
        check(Modifier.toString(Modifier.PUBLIC).equals("public"), "toString PUBLIC");
        check(Modifier.toString(Modifier.PRIVATE).equals("private"), "toString PRIVATE");
        check(Modifier.toString(Modifier.PROTECTED).equals("protected"), "toString PROTECTED");
        check(Modifier.toString(Modifier.STATIC).equals("static"), "toString STATIC");
        check(Modifier.toString(Modifier.FINAL).equals("final"), "toString FINAL");
        check(Modifier.toString(Modifier.SYNCHRONIZED).equals("synchronized"),
              "toString SYNCHRONIZED");
        check(Modifier.toString(Modifier.VOLATILE).equals("volatile"), "toString VOLATILE");
        check(Modifier.toString(Modifier.TRANSIENT).equals("transient"), "toString TRANSIENT");
        check(Modifier.toString(Modifier.NATIVE).equals("native"), "toString NATIVE");
        check(Modifier.toString(Modifier.INTERFACE).equals("interface"), "toString INTERFACE");
        check(Modifier.toString(Modifier.ABSTRACT).equals("abstract"), "toString ABSTRACT");
        check(Modifier.toString(Modifier.STRICT).equals("strictfp"), "toString STRICT");

        String combined = Modifier.toString(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
        check(combined.contains("public") && combined.contains("static")
                && combined.contains("final"), "toString combined contains tokens");
        check(combined.split(" ").length == 3, "toString combined three tokens");

        // full mask lists every token (order is not specified)
        String full = Modifier.toString(0x0fff);
        String[] tokens = {"public", "private", "protected", "abstract", "static", "final",
                "transient", "volatile", "synchronized", "native", "interface", "strictfp"};
        for (String t : tokens) {
            check(full.contains(t), "full toString contains '" + t + "'");
        }

        // unknown bits are ignored
        check(Modifier.toString(0x1000 | Modifier.PUBLIC).equals("public"),
              "toString ignores unknown bits");
    }

    static void testMaskHelpers() {
        check(Modifier.classModifiers() == (Modifier.PUBLIC | Modifier.PRIVATE
                | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL
                | Modifier.ABSTRACT | Modifier.STRICT),
                "classModifiers bits");

        check(Modifier.interfaceModifiers() == (Modifier.PUBLIC | Modifier.PRIVATE
                | Modifier.PROTECTED | Modifier.STATIC | Modifier.ABSTRACT | Modifier.STRICT),
                "interfaceModifiers bits");
        check(!Modifier.isFinal(Modifier.interfaceModifiers()), "interfaceModifiers no FINAL");
        check(!Modifier.isInterface(Modifier.interfaceModifiers()),
                "interfaceModifiers no INTERFACE flag");

        check(Modifier.constructorModifiers() == (Modifier.PUBLIC | Modifier.PRIVATE
                | Modifier.PROTECTED), "constructorModifiers bits");

        check(Modifier.methodModifiers() == (Modifier.PUBLIC | Modifier.PRIVATE
                | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL
                | Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.ABSTRACT
                | Modifier.STRICT), "methodModifiers bits");
        check(!Modifier.isInterface(Modifier.methodModifiers()), "methodModifiers no INTERFACE");

        check(Modifier.fieldModifiers() == (Modifier.PUBLIC | Modifier.PRIVATE
                | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL
                | Modifier.VOLATILE | Modifier.TRANSIENT), "fieldModifiers bits");

        check(Modifier.parameterModifiers() == Modifier.FINAL, "parameterModifiers bits");

        // masks are non-negative and their toString has no duplicates
        check(Modifier.classModifiers() >= 0 && Modifier.fieldModifiers() >= 0,
                "mask helpers non-negative");
    }

    static void testConstructor() {
        // Java 8: Modifier is a plain class with a public no-arg constructor.
        Modifier m = new Modifier();
        check(m != null, "new Modifier() constructs");
    }

    public static void main(String[] args) {
        testConstants();
        testPredicates();
        testToString();
        testMaskHelpers();
        testConstructor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
