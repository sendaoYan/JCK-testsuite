/*
 * @test
 * @summary java.security.DrbgParameters.Instantiation conformance: white-box structure + black-box behavior.
 * @run main InstantiationTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.DrbgParameters;
import java.security.DrbgParameters.Instantiation;
import java.security.SecureRandomParameters;
import java.util.Arrays;

public class InstantiationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.DrbgParameters$Instantiation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface() && !c.isEnum(), "class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkInterfaces(c, new String[]{"java.security.SecureRandomParameters"});
        checkMethods(c, new String[]{"getStrength", "getCapability", "getPersonalizationString", "toString"});
    }

    static void testBlackBox() throws Exception {
        byte[] ps = new byte[]{1, 2, 3};
        Instantiation inst = DrbgParameters.instantiation(192, DrbgParameters.Capability.RESEED_ONLY, ps);
        check(inst.getStrength() == 192, "getStrength");
        check(inst.getCapability() == DrbgParameters.Capability.RESEED_ONLY, "getCapability");
        check(Arrays.equals(ps, inst.getPersonalizationString()), "getPersonalizationString content");
        byte[] first = inst.getPersonalizationString();
        first[0] = 99;
        check(Arrays.equals(inst.getPersonalizationString(), new byte[]{1, 2, 3}), "getPersonalizationString defensive copy");
        Instantiation none = DrbgParameters.instantiation(-1, DrbgParameters.Capability.NONE, null);
        check(none.getStrength() == -1, "getStrength -1");
        check(none.getPersonalizationString() == null, "null personalization");
        checkThrows(IllegalArgumentException.class, () -> DrbgParameters.instantiation(-2, DrbgParameters.Capability.NONE, null));
        checkThrows(NullPointerException.class, () -> DrbgParameters.instantiation(-1, null, null));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: toString matrix, identity equality, engine round-trip ----
    static void testContractDeep() throws Exception {
        // toString is "strength,capability,Arrays.toString(personalization)"
        Instantiation v = DrbgParameters.instantiation(256, DrbgParameters.Capability.RESEED_ONLY, new byte[]{1, 2, 3});
        check("256,reseed_only,[1, 2, 3]".equals(v.toString()), "toString exact with personalization");
        check("128,none,null".equals(
                DrbgParameters.instantiation(128, DrbgParameters.Capability.NONE, null).toString()),
                "toString null personalization");
        check("192,none,[]".equals(
                DrbgParameters.instantiation(192, DrbgParameters.Capability.NONE, new byte[0]).toString()),
                "toString empty personalization differs from null");
        check("112,pr_and_reseed,null".equals(
                DrbgParameters.instantiation(112, DrbgParameters.Capability.PR_AND_RESEED, null).toString()),
                "toString pr_and_reseed form");

        // constructor-side defensive copy: later mutation of the source is invisible
        byte[] src = {4, 5};
        Instantiation d = DrbgParameters.instantiation(128, DrbgParameters.Capability.NONE, src);
        src[0] = 77;
        check(Arrays.equals(d.getPersonalizationString(), new byte[]{4, 5}),
                "personalization copied at construction");
        check(d.getPersonalizationString() != d.getPersonalizationString(),
                "getter returns a fresh copy each call");

        // equals is identity; same-value instances are not equal
        Instantiation e1 = DrbgParameters.instantiation(128, DrbgParameters.Capability.NONE, null);
        Instantiation e2 = DrbgParameters.instantiation(128, DrbgParameters.Capability.NONE, null);
        check(!e1.equals(e2), "identity equals: same value not equal");
        check(e1.equals(e1), "identity equals reflexive");
        check(e1.hashCode() == e1.hashCode(), "hashCode self-consistent");
        check(!e1.equals(null), "equals null false");
        check(!e1.equals(new Object()), "equals Object false");

        // strength boundaries: -1 wildcard legal, 0 legal, -2 rejected
        check(DrbgParameters.instantiation(-1, DrbgParameters.Capability.NONE, null).getStrength() == -1,
                "strength -1 wildcard");
        check(DrbgParameters.instantiation(0, DrbgParameters.Capability.NONE, null).getStrength() == 0,
                "strength 0 legal");
        checkThrows(IllegalArgumentException.class,
                () -> DrbgParameters.instantiation(-2, DrbgParameters.Capability.NONE, null));

        // all three capabilities round-trip
        for (DrbgParameters.Capability cap : DrbgParameters.Capability.values()) {
            check(DrbgParameters.instantiation(128, cap, null).getCapability() == cap,
                    "capability round-trip " + cap);
        }

        // engine round-trip: the params handed to getInstance come back from getParameters
        Instantiation inst = DrbgParameters.instantiation(256, DrbgParameters.Capability.RESEED_ONLY,
                new byte[]{9, 9, 9});
        java.security.SecureRandom drbg = java.security.SecureRandom.getInstance("DRBG", inst);
        check(drbg.getParameters() instanceof Instantiation, "engine returns Instantiation");
        Instantiation back = (Instantiation) drbg.getParameters();
        check(back.getStrength() == 256, "engine round-trip strength");
        check(back.getCapability() == DrbgParameters.Capability.RESEED_ONLY, "engine round-trip capability");
        check(Arrays.equals(back.getPersonalizationString(), new byte[]{9, 9, 9}),
                "engine round-trip personalization");
        drbg.nextBytes(new byte[8]);
        check(true, "instantiated DRBG produces output");
    }

    static void checkThrows(Class<? extends Throwable> type, Runnable r) {
        try { r.run(); } catch (Throwable t) { check(type.isInstance(t), "throws " + type.getSimpleName()); return; }
        throw new AssertionError("expected " + type.getSimpleName());
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
