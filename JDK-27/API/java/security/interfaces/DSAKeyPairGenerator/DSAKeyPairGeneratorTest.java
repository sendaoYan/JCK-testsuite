/*
 * @test
 * @summary java.security.interfaces.DSAKeyPairGenerator conformance: white-box structure + black-box behavior.
 * @run main DSAKeyPairGeneratorTest
 */

import java.lang.reflect.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.*;
import javax.crypto.KeyAgreement;


public class DSAKeyPairGeneratorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAKeyPairGenerator");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"initialize"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // The interface predates provider SPI refactoring: an implementation supplies the two
        // DSA-specific initialize methods; the JDK's returned KeyPairGenerator does NOT implement it
        class Impl implements DSAKeyPairGenerator {
            DSAParams recordedParams; SecureRandom recordedRandom;
            int recordedModulus; boolean recordedForce;
            public void initialize(DSAParams params, SecureRandom random) {
                recordedParams = params; recordedRandom = random;
            }
            public void initialize(int modlen, boolean genParams, SecureRandom random) {
                recordedModulus = modlen; recordedForce = genParams; recordedRandom = random;
            }
        }
        Impl impl = new Impl();
        SecureRandom sr = new SecureRandom();
        // params + random form
        DSAParams params = KeyPairGenerator.getInstance("DSA").generateKeyPair() instanceof Object ? null : null;
        KeyPairGenerator probe = KeyPairGenerator.getInstance("DSA");
        probe.initialize(2048);
        params = ((DSAKey) probe.generateKeyPair().getPublic()).getParams();
        impl.initialize(params, sr);
        check(impl.recordedParams == params, "params recorded");
        check(impl.recordedRandom == sr, "random recorded");
        // modulus + forceNewParams + random form
        impl.initialize(1024, true, sr);
        check(impl.recordedModulus == 1024, "modulus recorded");
        check(impl.recordedForce, "genParams recorded");
        // default state before any call
        Impl fresh = new Impl();
        check(fresh.recordedParams == null && fresh.recordedRandom == null, "no recorded state initially");
        // modern JDK: the generator itself is a delegation wrapper, not a DSAKeyPairGenerator
        KeyPairGenerator modern = KeyPairGenerator.getInstance("DSA");
        check(!(modern instanceof DSAKeyPairGenerator), "modern generator does not implement it");
        // the modern equivalent path: initialize(AlgorithmParameterSpec)
        modern.initialize(new DSAParameterSpec(params.getP(), params.getQ(), params.getG()));
        KeyPair kp = modern.generateKeyPair();
        check(kp.getPublic() instanceof DSAPublicKey, "spec-based init yields DSA keys");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract — exact member surface and lattice ----
    static void testContract() throws Exception {
        // exact member surface: exactly two void initializes; JDK-27 declares no throws
        // clause (JDK-8 declared the unchecked InvalidParameterException there)
        Method[] decl = DSAKeyPairGenerator.class.getDeclaredMethods();
        int count = 0;
        for (Method m : decl) {
            if ("initialize".equals(m.getName())) {
                count++;
                check(m.getExceptionTypes().length == 0,
                        "initialize declares no exceptions (JDK-27 dropped the throws clause)");
                check(m.getReturnType() == void.class, "initialize returns void");
            }
        }
        check(count == 2, "exactly two initialize overloads");
        check(decl.length == 2, "interface declares exactly two methods in total");
        for (Method m : decl) {
            Class<?>[] ps = m.getParameterTypes();
            if (ps.length == 2) {
                check(ps[0] == DSAParams.class && ps[1] == SecureRandom.class,
                        "two-arg overload is (DSAParams, SecureRandom)");
            }
            if (ps.length == 3) {
                check(ps[0] == int.class && ps[1] == boolean.class && ps[2] == SecureRandom.class,
                        "three-arg overload is (int, boolean, SecureRandom)");
            }
        }

        // lattice
        check(DSAKeyPairGenerator.class.getInterfaces().length == 0, "no superinterfaces");
        check(!Key.class.isAssignableFrom(DSAKeyPairGenerator.class), "not a Key");
        check(!Serializable.class.isAssignableFrom(DSAKeyPairGenerator.class), "not Serializable");
        try {
            DSAKeyPairGenerator.class.getDeclaredField("serialVersionUID");
            check(false, "no serialVersionUID declared");
        } catch (NoSuchFieldException e) {
            check(true, "no serialVersionUID declared");
        }
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
