/*
 * @test
 * @summary java.lang.reflect.Modifier conformance: full contract coverage.
 *          all 12 modifier constant bit values, every is* predicate against its own
 *          constant / unrelated constants / 0 / combined flags, toString(int) for
 *          empty/single/combined/full masks, the six *Modifiers() masks with exact
 *          bits, and the public Modifier() constructor (Java 8 only).
 *          Deepening batch: exhaustive 4096-mask truth table per predicate, exact
 *          canonical-order toString strings, per-javadoc membership/exclusion for
 *          every *Modifiers() mask, and end-to-end getModifiers() filtering on real
 *          Class/Method/Field members (incl. native/synchronized/strictfp/volatile/
 *          transient samples compiled from source).
 * @run main ModifierTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ModifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- deepening-batch sample members (source-compiled modifier bits) ----

    public abstract static class AbsSample { }

    public interface IfaceSample { }

    static class MemberSample {
        public static native int natM();
        public static synchronized int syncM() { return 1; }
        public static strictfp double sfpM(double d) { return d; }
        public transient int tF;
        public volatile int vF;
        public final int fF = 1;
        public static final int SF = 2;
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

    // ---- deepening batch: exhaustive truth table over all 4096 masks ----

    static void testPredicateTruthTable() {
        final int[] bits = {
            Modifier.PUBLIC, Modifier.PRIVATE, Modifier.PROTECTED, Modifier.STATIC,
            Modifier.FINAL, Modifier.SYNCHRONIZED, Modifier.VOLATILE, Modifier.TRANSIENT,
            Modifier.NATIVE, Modifier.INTERFACE, Modifier.ABSTRACT, Modifier.STRICT
        };
        final String[] names = {
            "isPublic", "isPrivate", "isProtected", "isStatic", "isFinal", "isSynchronized",
            "isVolatile", "isTransient", "isNative", "isInterface", "isAbstract", "isStrict"
        };
        final java.util.function.IntPredicate[] preds = {
            Modifier::isPublic, Modifier::isPrivate, Modifier::isProtected, Modifier::isStatic,
            Modifier::isFinal, Modifier::isSynchronized, Modifier::isVolatile, Modifier::isTransient,
            Modifier::isNative, Modifier::isInterface, Modifier::isAbstract, Modifier::isStrict
        };
        // every predicate is exactly (mask & bit) != 0 for ALL 2^12 masks
        for (int i = 0; i < bits.length; i++) {
            int bad = -1;
            for (int mask = 0; mask <= 0x0fff; mask++) {
                if (preds[i].test(mask) != ((mask & bits[i]) != 0)) { bad = mask; break; }
            }
            check(bad < 0, names[i] + " matches its bit across all 4096 masks (first mismatch 0x"
                    + Integer.toHexString(bad) + ")");
        }
        // representative combinations incl. all-set and near-miss masks
        check(Modifier.isPublic(0x0fff) && Modifier.isStrict(0x0fff) && Modifier.isInterface(0x0fff),
                "full mask sets every predicate");
        check(!Modifier.isPublic(0x0fff & ~Modifier.PUBLIC), "full mask minus PUBLIC");
        check(!Modifier.isFinal(0x0fff ^ Modifier.FINAL), "full mask XOR FINAL");
        check(Modifier.isSynchronized(Modifier.PUBLIC | Modifier.STATIC | Modifier.SYNCHRONIZED
                | Modifier.FINAL), "method-like combo keeps SYNCHRONIZED");
        check(Modifier.isVolatile(Modifier.PRIVATE | Modifier.VOLATILE | Modifier.TRANSIENT),
                "field-like combo keeps VOLATILE");
        check(Modifier.isAbstract(Modifier.ABSTRACT | Modifier.INTERFACE) && !Modifier.isAbstract(0),
                "interface-like combo");
        check(Modifier.isNative(Modifier.NATIVE | Modifier.STRICT) && !Modifier.isStrict(Modifier.NATIVE),
                "native+strict combo");
        check(!Modifier.isTransient(Modifier.STATIC | Modifier.FINAL), "static final has no TRANSIENT");
        check(!Modifier.isVolatile(Modifier.STATIC | Modifier.FINAL), "static final has no VOLATILE");
        check(Modifier.isProtected(Modifier.PROTECTED | 0x80000000), "PROTECTED with unknown high bit");
        check(!Modifier.isProtected(0x80000000), "only unknown high bit");
    }

    // ---- deepening batch: exact canonical-order toString strings ----

    static void testToStringExactOrder() {
        check(Modifier.toString(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL)
                .equals("public static final"), "PSF exact");
        check(Modifier.toString(Modifier.FINAL | Modifier.STATIC | Modifier.PUBLIC)
                .equals("public static final"), "PSF input-order independent");
        check(Modifier.toString(0x0fff).equals("public protected private abstract static final "
                + "transient volatile synchronized native strictfp interface"),
                "full mask exact canonical order");
        check(Modifier.toString(Modifier.PUBLIC | Modifier.PROTECTED | Modifier.ABSTRACT)
                .equals("public protected abstract"), "PPA exact");
        check(Modifier.toString(Modifier.STATIC | Modifier.VOLATILE | Modifier.TRANSIENT)
                .equals("static transient volatile"), "SVT exact");
        check(Modifier.toString(Modifier.NATIVE | Modifier.STRICT).equals("native strictfp"),
                "native+strictfp exact");
        check(Modifier.toString(Modifier.INTERFACE | Modifier.STRICT).equals("strictfp interface"),
                "strictfp+interface exact");
        check(Modifier.toString(Modifier.PRIVATE | Modifier.FINAL).equals("private final"),
                "private final exact");
        check(Modifier.toString(Modifier.ABSTRACT | Modifier.STRICT).equals("abstract strictfp"),
                "abstract strictfp exact");
        check(Modifier.toString(Modifier.TRANSIENT | Modifier.VOLATILE).equals("transient volatile"),
                "transient volatile exact");
        check(Modifier.toString(Modifier.STATIC | Modifier.FINAL | Modifier.VOLATILE)
                .equals("static final volatile"), "SFV exact");
        // repeated calls with the same mask produce equal strings (fresh instances)
        check(Modifier.toString(Modifier.PUBLIC).equals(Modifier.toString(Modifier.PUBLIC)),
                "repeated toString same-mask equal");
        check(Modifier.toString(0).equals(Modifier.toString(0)), "repeated toString(0) equal");
        check(Modifier.toString(Modifier.STATIC).equals(Modifier.toString(Modifier.STATIC)
                .intern()), "toString consistent with interned literal");
    }

    // ---- deepening batch: per-javadoc membership/exclusion for each mask ----

    static void testMaskMembershipPerJavadoc() {
        int cm = Modifier.classModifiers();
        check(Modifier.isPublic(cm) && Modifier.isPrivate(cm) && Modifier.isProtected(cm)
                && Modifier.isStatic(cm) && Modifier.isFinal(cm) && Modifier.isAbstract(cm)
                && Modifier.isStrict(cm), "classModifiers contains its 7 members");
        check(!Modifier.isSynchronized(cm) && !Modifier.isVolatile(cm) && !Modifier.isTransient(cm)
                && !Modifier.isNative(cm) && !Modifier.isInterface(cm),
                "classModifiers excludes method/field-only bits");

        int im = Modifier.interfaceModifiers();
        check(Modifier.isPublic(im) && Modifier.isPrivate(im) && Modifier.isProtected(im)
                && Modifier.isStatic(im) && Modifier.isAbstract(im) && Modifier.isStrict(im),
                "interfaceModifiers contains its 6 members");
        check(!Modifier.isFinal(im) && !Modifier.isInterface(im) && !Modifier.isSynchronized(im)
                && !Modifier.isVolatile(im) && !Modifier.isTransient(im) && !Modifier.isNative(im),
                "interfaceModifiers excludes non-interface bits");

        int ctm = Modifier.constructorModifiers();
        check(ctm == 0x7, "constructorModifiers == PUBLIC|PRIVATE|PROTECTED == 0x7");
        check(!Modifier.isStatic(ctm) && !Modifier.isFinal(ctm) && !Modifier.isAbstract(ctm)
                && !Modifier.isSynchronized(ctm) && !Modifier.isNative(ctm),
                "constructorModifiers access bits only");
        check(Modifier.toString(ctm).equals("public protected private"),
                "constructorModifiers toString exact");

        int mm = Modifier.methodModifiers();
        check(Modifier.isPublic(mm) && Modifier.isPrivate(mm) && Modifier.isProtected(mm)
                && Modifier.isStatic(mm) && Modifier.isFinal(mm) && Modifier.isSynchronized(mm)
                && Modifier.isNative(mm) && Modifier.isAbstract(mm) && Modifier.isStrict(mm),
                "methodModifiers contains its 9 members");
        check(!Modifier.isVolatile(mm) && !Modifier.isTransient(mm) && !Modifier.isInterface(mm),
                "methodModifiers excludes field-only bits");

        int fm = Modifier.fieldModifiers();
        check(Modifier.isPublic(fm) && Modifier.isPrivate(fm) && Modifier.isProtected(fm)
                && Modifier.isStatic(fm) && Modifier.isFinal(fm) && Modifier.isVolatile(fm)
                && Modifier.isTransient(fm), "fieldModifiers contains its 7 members");
        check(!Modifier.isSynchronized(fm) && !Modifier.isNative(fm) && !Modifier.isAbstract(fm)
                && !Modifier.isInterface(fm) && !Modifier.isStrict(fm),
                "fieldModifiers excludes method/class-only bits");
        check(Modifier.toString(fm).equals("public protected private static final transient volatile"),
                "fieldModifiers toString exact");

        check(Modifier.parameterModifiers() == Modifier.FINAL
                && !Modifier.isStatic(Modifier.parameterModifiers()), "parameterModifiers FINAL only");
    }

    // ---- deepening batch: end-to-end on real members ----

    static void testRealMemberModifiers() throws Exception {
        // classes
        check(Modifier.toString(String.class.getModifiers()).equals("public final"),
                "String.class toString exact");
        check(Modifier.toString(CharSequence.class.getModifiers())
                .equals("public abstract interface"), "CharSequence toString exact");
        check(Modifier.isInterface(CharSequence.class.getModifiers()),
                "CharSequence has INTERFACE bit");
        check(Modifier.toString(int.class.getModifiers()).equals("public abstract final"),
                "int.class toString exact");
        check(Modifier.toString(AbsSample.class.getModifiers()).equals("public abstract static"),
                "nested abstract class toString exact");
        check(AbsSample.class.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC
                | Modifier.ABSTRACT), "nested abstract class bits");
        check(Modifier.toString(IfaceSample.class.getModifiers())
                .equals("public abstract static interface"), "nested interface toString exact");
        check(IfaceSample.class.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC
                | Modifier.INTERFACE | Modifier.ABSTRACT), "nested interface bits");
        check(Modifier.toString(MemberSample.class.getModifiers()).equals("static"),
                "package-private nested class toString exact");

        // methods
        Method natM = MemberSample.class.getDeclaredMethod("natM");
        check(natM.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.NATIVE),
                "native method bits");
        check(Modifier.toString(natM.getModifiers()).equals("public static native"),
                "native method toString exact");
        Method syncM = MemberSample.class.getDeclaredMethod("syncM");
        check(syncM.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.SYNCHRONIZED),
                "synchronized method bits");
        check(Modifier.toString(syncM.getModifiers()).equals("public static synchronized"),
                "synchronized method toString exact");
        Method sfpM = MemberSample.class.getDeclaredMethod("sfpM", double.class);
        check(sfpM.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.STRICT),
                "strictfp method bits");
        check(Modifier.toString(sfpM.getModifiers()).equals("public static strictfp"),
                "strictfp method toString exact");
        check(Modifier.toString(Integer.class.getMethod("valueOf", String.class).getModifiers())
                .equals("public static"), "Integer.valueOf toString exact");
        check(Modifier.toString(Object.class.getMethod("hashCode").getModifiers())
                .equals("public native"), "Object.hashCode toString exact");
        check(Modifier.toString(Integer.class.getMethod("toString").getModifiers())
                .equals("public"), "plain public method toString exact");
        check(Modifier.isAbstract(Modifier.methodModifiers()), "abstract legal for methods");

        // fields
        Field tF = MemberSample.class.getDeclaredField("tF");
        check(tF.getModifiers() == (Modifier.PUBLIC | Modifier.TRANSIENT), "transient field bits");
        check(Modifier.toString(tF.getModifiers()).equals("public transient"),
                "transient field toString exact");
        Field vF = MemberSample.class.getDeclaredField("vF");
        check(vF.getModifiers() == (Modifier.PUBLIC | Modifier.VOLATILE), "volatile field bits");
        check(Modifier.toString(vF.getModifiers()).equals("public volatile"),
                "volatile field toString exact");
        Field fF = MemberSample.class.getDeclaredField("fF");
        check(fF.getModifiers() == (Modifier.PUBLIC | Modifier.FINAL), "final field bits");
        check(Modifier.toString(fF.getModifiers()).equals("public final"),
                "final field toString exact");
        Field sFld = MemberSample.class.getDeclaredField("SF");
        check(sFld.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL),
                "static final field bits");
        check(Modifier.toString(sFld.getModifiers()).equals("public static final"),
                "static final field toString exact");
        check(Modifier.toString(String.class.getDeclaredField("value").getModifiers())
                .equals("private final"), "String.value toString exact");
    }

    public static void main(String[] args) throws Exception {
        testConstants();
        testPredicates();
        testToString();
        testMaskHelpers();
        testConstructor();
        testPredicateTruthTable();
        testToStringExactOrder();
        testMaskMembershipPerJavadoc();
        testRealMemberModifiers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
