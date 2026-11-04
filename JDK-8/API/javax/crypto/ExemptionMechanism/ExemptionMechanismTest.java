/*
 * @test
 * @summary javax.crypto.ExemptionMechanism conformance: white-box structure + black-box behavior.
 * @run main ExemptionMechanismTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.ExemptionMechanism;
import javax.crypto.ExemptionMechanismException;
import javax.crypto.ExemptionMechanismSpi;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

public class ExemptionMechanismTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface Thunk { void run() throws Exception; }
    static void expect(Class<? extends Throwable> t, Thunk r) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError("expected " + t.getName() + " but got " + e, e);
        }
        throw new AssertionError("expected " + t.getName() + " but nothing was thrown");
    }

    // deterministic SPI used to drive the ExemptionMechanism wrapper state machine
    static class MySpi extends ExemptionMechanismSpi {
        Key recKey; AlgorithmParameterSpec recSpec; AlgorithmParameters recParams; int outputCalls;
        static final byte[] BLOB = {1, 2, 3, 4};

        @Override protected int engineGetOutputSize(int inputLen) { outputCalls++; return inputLen + 7; }
        @Override protected void engineInit(Key key) throws ExemptionMechanismException { recKey = key; recSpec = null; recParams = null; }
        @Override protected void engineInit(Key key, AlgorithmParameterSpec params)
                throws ExemptionMechanismException { recKey = key; recSpec = params; recParams = null; }
        @Override protected void engineInit(Key key, AlgorithmParameters params)
                throws ExemptionMechanismException { recKey = key; recSpec = null; recParams = params; }
        @Override protected byte[] engineGenExemptionBlob() throws ExemptionMechanismException { return BLOB.clone(); }
        @Override protected int engineGenExemptionBlob(byte[] output, int outputOffset) throws ShortBufferException {
            if (output.length - outputOffset < BLOB.length) throw new ShortBufferException("spi: need 4 bytes");
            System.arraycopy(BLOB, 0, output, outputOffset, BLOB.length);
            return BLOB.length;
        }
    }

    // concrete subclass exposing the protected ExemptionMechanism constructor
    static class MyMech extends ExemptionMechanism {
        MyMech(ExemptionMechanismSpi spi, Provider p, String name) { super(spi, p, name); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.ExemptionMechanism");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface() && !Modifier.isAbstract(c.getModifiers()),
                "public concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "direct superclass java.lang.Object");
        // legacy assertion: no public constructor
        check(c.getConstructors().length == 0, "no public constructor");
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            Class<?>[] ps = k.getParameterTypes();
            if (Modifier.isProtected(k.getModifiers()) && ps.length == 3
                    && ps[0].getName().equals("javax.crypto.ExemptionMechanismSpi")
                    && ps[1].getName().equals("java.security.Provider")
                    && ps[2] == String.class) protCtor = true;
        }
        check(protCtor, "protected (ExemptionMechanismSpi, Provider, String) constructor");
        String[] finals = {"getName", "getProvider", "isCryptoAllowed", "getOutputSize", "init", "genExemptionBlob"};
        for (String n : finals) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods())
                if (m.getName().equals(n)) { found = Modifier.isFinal(m.getModifiers()); }
            check(found, "final method " + n);
        }
        boolean statInst = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("getInstance") && Modifier.isStatic(m.getModifiers())
                    && Modifier.isFinal(m.getModifiers()) && m.getParameterCount() >= 1) statInst = true;
        check(statInst, "static final getInstance factories");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        factoryErrors();
        wrapperStateMachine();
    }

    // Temurin JDK-27 ships no ExemptionMechanism implementation: every known
    // mechanism name must fail with NoSuchAlgorithmException (type-only assertions).
    static void factoryErrors() {
        expect(java.security.NoSuchAlgorithmException.class,
                () -> ExemptionMechanism.getInstance("NoSuchMechanism"));
        expect(java.security.NoSuchAlgorithmException.class,
                () -> ExemptionMechanism.getInstance("KeyRecovery"));
        expect(java.security.NoSuchAlgorithmException.class,
                () -> ExemptionMechanism.getInstance("KeyEscrow"));
        expect(java.security.NoSuchAlgorithmException.class,
                () -> ExemptionMechanism.getInstance("KeyWeakening"));
        expect(java.security.NoSuchAlgorithmException.class,
                () -> ExemptionMechanism.getInstance("KeyRecovery", "SunJCE"));
        expect(java.security.NoSuchAlgorithmException.class,
                () -> ExemptionMechanism.getInstance("KeyRecovery", java.security.Security.getProvider("SunJCE")));
        expect(java.security.NoSuchProviderException.class,
                () -> ExemptionMechanism.getInstance("KeyRecovery", "NoSuchProviderXYZ"));
        expect(NullPointerException.class, () -> ExemptionMechanism.getInstance(null));
    }

    // full wrapper state machine driven through a deterministic SPI
    static void wrapperStateMachine() throws Exception {
        Provider prov = java.security.Security.getProvider("SunJCE");
        MySpi spi = new MySpi();
        MyMech em = new MyMech(spi, prov, "DummyMech");

        // attributes come straight from the protected constructor
        check("DummyMech".equals(em.getName()), "getName returns constructor name");
        check(em.getProvider() == prov, "getProvider returns constructor provider");

        // before init: size and blob generation are rejected, isCryptoAllowed stays false
        expect(IllegalStateException.class, () -> em.getOutputSize(10));
        expect(IllegalStateException.class, () -> em.genExemptionBlob());
        expect(IllegalStateException.class, () -> em.genExemptionBlob(new byte[16]));
        expect(IllegalStateException.class, () -> em.genExemptionBlob(new byte[16], 0));

        Key key = new SecretKeySpec(new byte[16], "AES");
        check(!em.isCryptoAllowed(key), "isCryptoAllowed false before any blob");
        em.init(key);
        check(spi.recKey == key, "init(Key) delegates to engineInit(Key)");
        check(!em.isCryptoAllowed(key), "isCryptoAllowed false right after init (no blob yet)");
        check(em.getOutputSize(10) == 17, "getOutputSize delegates to engineGetOutputSize");
        check(spi.outputCalls >= 1, "engineGetOutputSize actually invoked");
        expect(IllegalArgumentException.class, () -> em.getOutputSize(-1));

        // three init overloads each reach the matching engine method
        AlgorithmParameterSpec spec = new javax.crypto.spec.GCMParameterSpec(128, new byte[12]);
        em.init(key, spec);
        check(spi.recSpec == spec, "init(Key,AlgorithmParameterSpec) forwards spec");
        AlgorithmParameters ap = AlgorithmParameters.getInstance("AES");
        ap.init(new javax.crypto.spec.IvParameterSpec(new byte[16]));
        em.init(key, ap);
        check(spi.recParams == ap, "init(Key,AlgorithmParameters) forwards params");
        check(em.getOutputSize(0) == 7, "getOutputSize still works after parameter inits");
        em.init(key);

        // blob generation via all three overloads; done flag drives isCryptoAllowed
        byte[] blob = em.genExemptionBlob();
        check(Arrays.equals(blob, MySpi.BLOB), "genExemptionBlob() returns engine blob");
        check(em.isCryptoAllowed(key), "isCryptoAllowed true for the init key after blob");
        Key otherKey = new SecretKeySpec(new byte[]{9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9}, "AES");
        check(!em.isCryptoAllowed(otherKey), "isCryptoAllowed false for a different key");
        check(!em.isCryptoAllowed(null), "isCryptoAllowed false for null key");

        byte[] buf = new byte[8];
        int n = em.genExemptionBlob(buf, 2);
        check(n == 4, "genExemptionBlob(byte[],int) returns blob length");
        check(buf[0] == 0 && buf[1] == 0 && buf[2] == 1 && buf[5] == 4 && buf[6] == 0, "blob written at offset 2");
        check(em.genExemptionBlob(new byte[4]) == 4, "genExemptionBlob(byte[]) fills from index 0");
        expect(ShortBufferException.class, () -> em.genExemptionBlob(new byte[3], 0));

        // a fresh init clears the done flag: blob must be regenerated before crypto is allowed
        em.init(key);
        check(!em.isCryptoAllowed(key), "init clears the generated-blob flag");
        em.genExemptionBlob();
        check(em.isCryptoAllowed(key), "regenerating the blob re-enables the key");

        // ExemptionMechanismException from the engine propagates through the wrapper
        MySpi failing = new MySpi() {
            @Override public byte[] engineGenExemptionBlob() throws ExemptionMechanismException {
                throw new ExemptionMechanismException("spi blob failure");
            }
        };
        MyMech emFail = new MyMech(failing, prov, "DummyMech");
        emFail.init(key);
        expect(ExemptionMechanismException.class, () -> emFail.genExemptionBlob());
        check(!emFail.isCryptoAllowed(key), "failed blob generation keeps crypto disallowed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
