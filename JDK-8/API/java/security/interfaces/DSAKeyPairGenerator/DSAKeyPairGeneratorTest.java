/*
 * @test
 * @summary java.security.interfaces.DSAKeyPairGenerator conformance: interface hierarchy,
 *          exact member surface (exactly two initialize overloads, both declaring the
 *          unchecked InvalidParameterException in the throws clause),
 *          record-style implementation state capture (params/modlen/genParams/random passed
 *          through verbatim, nulls accepted), lattice (not a Key, not Serializable,
 *          platform KeyPairGenerator does not implement it).
 * @run main DSAKeyPairGeneratorTest
 */

import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.DSAKeyPairGenerator;
import java.security.interfaces.DSAParams;
import java.security.spec.DSAParameterSpec;

public class DSAKeyPairGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAKeyPairGenerator");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("initialize"), "declares initialize");

        testContract();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: contract — member surface + record-style state capture ----
    static void testContract() throws Exception {
        // exact member surface: exactly two initialize overloads, both void, both declare
        // the unchecked InvalidParameterException in their throws clause
        Method[] decl = DSAKeyPairGenerator.class.getDeclaredMethods();
        int count = 0;
        for (Method m : decl) {
            if ("initialize".equals(m.getName())) {
                count++;
                Class<?>[] ex = m.getExceptionTypes();
                check(ex.length == 1 && ex[0] == java.security.InvalidParameterException.class,
                        "initialize declares throws InvalidParameterException only");
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

        // lattice: bare interface, not a Key, not Serializable; platform KPG does not implement it
        check(DSAKeyPairGenerator.class.getInterfaces().length == 0, "no superinterfaces");
        check(!Key.class.isAssignableFrom(DSAKeyPairGenerator.class), "not a Key");
        check(!Serializable.class.isAssignableFrom(DSAKeyPairGenerator.class), "not Serializable");
        check(!DSAKeyPairGenerator.class.isAssignableFrom(KeyPairGenerator.class),
                "platform KeyPairGenerator does not implement DSAKeyPairGenerator");
        check(!(KeyPairGenerator.getInstance("DSA") instanceof DSAKeyPairGenerator),
                "a real DSA KeyPairGenerator instance is not a DSAKeyPairGenerator");
        try {
            DSAKeyPairGenerator.class.getDeclaredField("serialVersionUID");
            check(false, "no serialVersionUID declared");
        } catch (NoSuchFieldException e) {
            check(true, "no serialVersionUID declared");
        }

        // record-style implementation captures arguments verbatim
        RecGen rec = new RecGen();
        DSAParams params = new DSAParameterSpec(
                java.math.BigInteger.valueOf(23), java.math.BigInteger.valueOf(11),
                java.math.BigInteger.valueOf(2));
        SecureRandom sr = new SecureRandom();
        rec.initialize(params, sr);
        check(rec.lastParams == params, "record (DSAParams, SecureRandom) passthrough");
        check(rec.lastRandom == sr, "record random passthrough");
        rec.initialize(null, null);
        check(rec.lastParams == null && rec.lastRandom == null, "record nulls accepted");
        rec.initialize(512, true, null);
        check(rec.lastModLen == 512 && rec.lastGenParams && rec.lastRandom == null,
                "record (int, boolean, SecureRandom) passthrough");
        rec.initialize(-1, false, sr);
        check(rec.lastModLen == -1 && !rec.lastGenParams && rec.lastRandom == sr,
                "record captures negative modlen unchanged (impl validates)");
    }

    static class RecGen implements DSAKeyPairGenerator {
        DSAParams lastParams;
        SecureRandom lastRandom;
        int lastModLen = Integer.MIN_VALUE;
        boolean lastGenParams;
        public void initialize(DSAParams params, SecureRandom random) {
            lastParams = params;
            lastRandom = random;
        }
        public void initialize(int modlen, boolean genParams, SecureRandom random) {
            lastModLen = modlen;
            lastGenParams = genParams;
            lastRandom = random;
        }
    }
}
