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


    static void testDeepPrimitiveClassLabel() throws Throwable {
        // primitive class labels match their boxed values (probed on jdk-27.0.1)
        MethodType tsType = MethodType.methodType(int.class, Object.class, int.class);
        CallSite cs = SwitchBootstraps.typeSwitch(LOOKUP, "", tsType, int.class, String.class);
        MethodHandle mh = cs.dynamicInvoker();
        check((int) mh.invoke((Object) 42, 0) == 0, "boxed int matches int.class label");
        check((int) mh.invoke((Object) Integer.valueOf(42), 0) == 0, "Integer matches int.class label");
        check((int) mh.invoke((Object) "s", 0) == 1, "String matches its label");
        check((int) mh.invoke((Object) 3.5d, 0) == 2, "no match -> default index == label count");
        check((int) mh.invoke((Object) null, 0) == -1, "null selector -> -1");
        check((int) mh.invoke((Object) Long.valueOf(42L), 0) == 2, "Long does not match int.class");
    }

    static void testDeepEnumSwitchResume() throws Throwable {
        MethodType esType = MethodType.methodType(int.class, Color.class, int.class);
        CallSite es = SwitchBootstraps.enumSwitch(LOOKUP, "", esType, new Object[]{"RED", "BLUE"});
        MethodHandle eh = es.dynamicInvoker();
        check((int) eh.invoke(Color.RED, 0) == 0, "RED at start index 0");
        check((int) eh.invoke(Color.RED, 1) == 2, "resume from 1 skips RED -> default (count)");
        check((int) eh.invoke(Color.RED, 2) == 2, "resume past all labels -> default");
        check((int) eh.invoke(Color.BLUE, 1) == 1, "resume from 1 still finds BLUE");
        check((int) eh.invoke(Color.GREEN, 0) == 2, "unlisted constant -> default");
        check((int) eh.invoke(null, 0) == -1, "null enum selector -> -1");
        // labels are matched by constant name, not ordinals
        CallSite reordered = SwitchBootstraps.enumSwitch(LOOKUP, "", esType,
                new Object[]{"BLUE", "RED"});
        MethodHandle rh = reordered.dynamicInvoker();
        check((int) rh.invoke(Color.RED, 0) == 1, "label order defines the index (BLUE first)");
        check((int) rh.invoke(Color.BLUE, 0) == 0, "label order defines the index (BLUE at 0)");
    }

    // ---- contract deepening (batch lcL, 2026-11) ----

    // black-box closure: the switch machinery these bootstraps power, observed
    // through the language itself (pattern switch with null case + guarded pattern)
    static String langSwitch(Object o) {
        return switch (o) {
            case null -> "null";
            case String s -> "str:" + s;
            case Integer i -> "int:" + i;
            case Character c -> "char:" + c;
            default -> "other";
        };
    }

    static String guardedSwitch(Object o) {
        return switch (o) {
            case String s when s.isEmpty() -> "empty";
            case String s -> "str:" + s.length();
            default -> "other";
        };
    }

    static String enumLang(Color c) {
        return switch (c) { case RED -> "r"; case GREEN -> "g"; case BLUE -> "b"; };
    }

    static void testDeepLanguageSwitchClosure() {
        check(langSwitch("x").equals("str:x"), "pattern switch String binding");
        check(langSwitch(42).equals("int:42"), "pattern switch Integer binding");
        check(langSwitch('c').equals("char:c"), "pattern switch Character binding");
        check(langSwitch(3.5).equals("other"), "pattern switch default branch");
        check(langSwitch(null).equals("null"), "pattern switch null case (JDK 21+)");
        check(guardedSwitch("").equals("empty"), "guarded pattern when-guard taken");
        check(guardedSwitch("abc").equals("str:3"), "guarded pattern falls through on false guard");
        check(guardedSwitch(7).equals("other"), "guarded switch default");
        check(enumLang(Color.RED).equals("r"), "enum switch RED branch");
        check(enumLang(Color.GREEN).equals("g"), "enum switch GREEN branch");
        check(enumLang(Color.BLUE).equals("b"), "enum switch BLUE branch (all branches hit)");
    }

    static void testDeepNullLabelRejected() throws Throwable {
        // probed on jdk-27.0.1: an explicit null label is rejected at bootstrap time
        try {
            MethodType t = MethodType.methodType(int.class, Object.class, int.class);
            SwitchBootstraps.typeSwitch(LOOKUP, "", t, new Object[]{null, String.class});
            check(false, "null label must be rejected at bootstrap");
        } catch (IllegalArgumentException e) {
            check("null label found".equals(e.getMessage()), "null label message exact: " + e.getMessage());
        }
    }

    static void testDeepSupertypeLabelOrdering() throws Throwable {
        // a Class label matches subtypes; the FIRST matching label wins
        MethodType t = MethodType.methodType(int.class, Object.class, int.class);
        CallSite cs = SwitchBootstraps.typeSwitch(LOOKUP, "", t, CharSequence.class, String.class);
        MethodHandle mh = cs.dynamicInvoker();
        check((int) mh.invoke((Object) "s", 0) == 0, "String matches the CharSequence label (subtype, first)");
        check((int) mh.invoke((Object) new StringBuilder("x"), 0) == 0, "StringBuilder matches CharSequence");
        check((int) mh.invoke((Object) Integer.valueOf(1), 0) == 2, "non-CharSequence falls to default");
        CallSite cs2 = SwitchBootstraps.typeSwitch(LOOKUP, "", t, String.class, CharSequence.class);
        MethodHandle mh2 = cs2.dynamicInvoker();
        check((int) mh2.invoke((Object) "s", 0) == 0, "String label first: String still label 0");
        check((int) mh2.invoke((Object) new StringBuilder("x"), 0) == 1, "StringBuilder falls to label 1");
    }

    static void testDeepDuplicateClassLabels() throws Throwable {
        MethodType t = MethodType.methodType(int.class, Object.class, int.class);
        CallSite cs = SwitchBootstraps.typeSwitch(LOOKUP, "", t, String.class, String.class);
        MethodHandle mh = cs.dynamicInvoker();
        check((int) mh.invoke((Object) "s", 0) == 0, "duplicate labels: first occurrence wins");
        check((int) mh.invoke((Object) 5, 0) == 2, "duplicate labels: no match -> label count");
    }

    static void testDeepEnumIllegalLabelType() throws Throwable {
        // probed on jdk-27.0.1: non-String labels rejected with this exact message
        try {
            SwitchBootstraps.enumSwitch(LOOKUP, "",
                    MethodType.methodType(int.class, Color.class, int.class),
                    new Object[]{Integer.valueOf(1)});
            check(false, "enumSwitch with an Integer label must be rejected");
        } catch (IllegalArgumentException e) {
            check(("label with illegal type found: class java.lang.Integer, "
                    + "expected label of type either String or Class").equals(e.getMessage()),
                    "enumSwitch illegal label message exact: " + e.getMessage());
        }
    }

    static void testDeepEnumUnknownAndDuplicateNames() throws Throwable {
        MethodType t = MethodType.methodType(int.class, Color.class, int.class);
        CallSite unknown = SwitchBootstraps.enumSwitch(LOOKUP, "", t, new Object[]{"NOPE"});
        MethodHandle uh = unknown.dynamicInvoker();
        check((int) uh.invoke(Color.RED, 0) == 1, "unknown name label: all constants -> default");
        check((int) uh.invoke(Color.BLUE, 0) == 1, "unknown name label: default for every constant");
        CallSite dup = SwitchBootstraps.enumSwitch(LOOKUP, "", t, new Object[]{"RED", "RED"});
        MethodHandle dh = dup.dynamicInvoker();
        check((int) dh.invoke(Color.RED, 0) == 0, "duplicate names: first occurrence wins");
        check((int) dh.invoke(Color.BLUE, 0) == 2, "unlisted constant -> default index");
        // matching is by NAME, not ordinal: RED is ordinal 0 but listed last here
        CallSite byName = SwitchBootstraps.enumSwitch(LOOKUP, "", t, new Object[]{"BLUE", "GREEN", "RED"});
        MethodHandle nh = byName.dynamicInvoker();
        check((int) nh.invoke(Color.RED, 0) == 2, "name-based matching: RED listed last matches last");
    }

    static void testDeepConstantLabels() throws Throwable {
        MethodType t = MethodType.methodType(int.class, Object.class, int.class);
        // Boolean class label matches the boxed selector by type
        CallSite bool = SwitchBootstraps.typeSwitch(LOOKUP, "", t, Boolean.class, Integer.class);
        MethodHandle bh = bool.dynamicInvoker();
        check((int) bh.invoke((Object) Boolean.TRUE, 0) == 0, "Boolean selector matches Boolean label");
        check((int) bh.invoke((Object) 5, 0) == 1, "Integer selector matches Integer label");
        check((int) bh.invoke((Object) "s", 0) == 2, "String selector -> default");
        // non-Class constant labels compare by equals()
        CallSite ints = SwitchBootstraps.typeSwitch(LOOKUP, "", t, 5, 7);
        MethodHandle ih = ints.dynamicInvoker();
        check((int) ih.invoke((Object) 5, 0) == 0, "Integer constant label 5 by equals");
        check((int) ih.invoke((Object) 7, 0) == 1, "Integer constant label 7 by equals");
        check((int) ih.invoke((Object) 9, 0) == 2, "unlisted constant -> default index");
        check((int) ih.invoke((Object) null, 0) == -1, "null selector still -1 for constant labels");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepPrimitiveClassLabel();
        testDeepEnumSwitchResume();
        testDeepLanguageSwitchClosure();
        testDeepNullLabelRejected();
        testDeepSupertypeLabelOrdering();
        testDeepDuplicateClassLabels();
        testDeepEnumIllegalLabelType();
        testDeepEnumUnknownAndDuplicateNames();
        testDeepConstantLabels();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
