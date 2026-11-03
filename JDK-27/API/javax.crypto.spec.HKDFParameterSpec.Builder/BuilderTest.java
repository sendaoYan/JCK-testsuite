/*
 * @test
 * @summary javax.crypto.spec.HKDFParameterSpec.Builder conformance: white-box structure + black-box behavior.
 * @run main BuilderTest
 */

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;

public class BuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // RFC 5869 Test Case 1 (HKDF-SHA256)
    static final byte[] IKM = filled(0x0b, 22);
    static final byte[] SALT = bytes("000102030405060708090a0b0c");
    static final byte[] INFO = bytes("f0f1f2f3f4f5f6f7f8f9");
    static final String OKM1 = "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf"
            + "34007208d5b887185865";
    static final String PRK1 = "077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5";

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.HKDFParameterSpec$Builder");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getEnclosingClass().getName().equals("javax.crypto.spec.HKDFParameterSpec"),
                "nested in HKDFParameterSpec");
        checkMethods(c, new String[]{"addIKM", "addSalt", "extractOnly", "thenExpand"});
        check(c.getConstructors().length == 0, "no public constructor (created via ofExtract)");
        // addIKM/addSalt each overloaded (byte[]) and (SecretKey)
        int ikmOverloads = 0, saltOverloads = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("addIKM") && m.getParameterTypes()[0] == byte[].class) ikmOverloads++;
            if (m.getName().equals("addIKM") && m.getParameterTypes()[0] == SecretKey.class) ikmOverloads++;
            if (m.getName().equals("addSalt") && m.getParameterTypes()[0] == byte[].class) saltOverloads++;
            if (m.getName().equals("addSalt") && m.getParameterTypes()[0] == SecretKey.class) saltOverloads++;
        }
        check(ikmOverloads == 2, "addIKM overloads (byte[])/(SecretKey)");
        check(saltOverloads == 2, "addSalt overloads (byte[])/(SecretKey)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (preserved) basic chain
        SecretKeySpec ikm = new SecretKeySpec(new byte[]{1, 2, 3}, "Generic");
        HKDFParameterSpec.Builder b = HKDFParameterSpec.ofExtract();
        b.addIKM(ikm).addSalt(new byte[]{9, 8});
        HKDFParameterSpec.ExtractThenExpand ete = b.thenExpand(new byte[]{0}, 32);
        check(ete.ikms().size() == 1, "ikms size");
        check(ete.salts().size() == 1, "salts size");
        check(ete.length() == 32, "expand length");
        HKDFParameterSpec.Extract ex = HKDFParameterSpec.ofExtract().extractOnly();
        check(ex.ikms().isEmpty() && ex.salts().isEmpty(), "extractOnly empty");

        // add* return the same builder instance for chaining
        HKDFParameterSpec.Builder bb = HKDFParameterSpec.ofExtract();
        check(bb.addIKM(new byte[]{1}) == bb, "addIKM(byte[]) returns this");
        check(bb.addIKM(new SecretKeySpec(new byte[]{2}, "Generic")) == bb, "addIKM(SecretKey) returns this");
        check(bb.addSalt(new byte[]{3}) == bb, "addSalt(byte[]) returns this");
        check(bb.addSalt(new SecretKeySpec(new byte[]{4}, "Generic")) == bb, "addSalt(SecretKey) returns this");

        // multiple adds preserve insertion order (byte[] entries become SecretKeySpec)
        HKDFParameterSpec.Extract multi = bb.extractOnly();
        List<SecretKey> ikms = multi.ikms();
        List<SecretKey> salts = multi.salts();
        check(ikms.size() == 2 && salts.size() == 2, "multi add sizes");
        check(Arrays.equals(ikms.get(0).getEncoded(), new byte[]{1}), "ikms order [0]");
        check(Arrays.equals(ikms.get(1).getEncoded(), new byte[]{2}), "ikms order [1]");
        check(Arrays.equals(salts.get(0).getEncoded(), new byte[]{3}), "salts order [0]");
        check(Arrays.equals(salts.get(1).getEncoded(), new byte[]{4}), "salts order [1]");
        // SecretKey arguments keep identity; byte[] arguments are cloned
        SecretKeySpec userKey = new SecretKeySpec(new byte[]{7, 7}, "Generic");
        HKDFParameterSpec.Extract id = HKDFParameterSpec.ofExtract().addIKM(userKey).extractOnly();
        check(id.ikms().get(0) == userKey, "SecretKey ikm identity");
        byte[] raw = new byte[]{9};
        HKDFParameterSpec.Extract def = HKDFParameterSpec.ofExtract().addIKM(raw).addSalt(raw).extractOnly();
        raw[0] = 1;
        check(def.ikms().get(0).getEncoded()[0] == 9, "addIKM(byte[]) defensive copy");
        check(def.salts().get(0).getEncoded()[0] == 9, "addSalt(byte[]) defensive copy");

        // zero-length byte[] additions are discarded
        HKDFParameterSpec.Builder eb = HKDFParameterSpec.ofExtract().addIKM(new byte[0]).addSalt(new byte[0]);
        check(eb.extractOnly().ikms().isEmpty() && eb.extractOnly().salts().isEmpty(),
                "empty arrays discarded");

        // built objects are immutable snapshots of builder state
        HKDFParameterSpec.Builder reuse = HKDFParameterSpec.ofExtract().addIKM(new byte[]{1});
        HKDFParameterSpec.Extract early = reuse.extractOnly();
        reuse.addIKM(new byte[]{2});
        check(early.ikms().size() == 1, "built object immune to later builder adds");
        check(reuse.extractOnly().ikms().size() == 2, "builder keeps accumulating");

        // null arguments -> NPE for all four add methods
        try { HKDFParameterSpec.ofExtract().addIKM((byte[]) null); check(false, "addIKM null must throw"); }
        catch (NullPointerException e) { check(true, "addIKM(byte[]) NPE"); }
        try { HKDFParameterSpec.ofExtract().addIKM((SecretKey) null); check(false, "addIKM null key must throw"); }
        catch (NullPointerException e) { check(true, "addIKM(SecretKey) NPE"); }
        try { HKDFParameterSpec.ofExtract().addSalt((byte[]) null); check(false, "addSalt null must throw"); }
        catch (NullPointerException e) { check(true, "addSalt(byte[]) NPE"); }
        try { HKDFParameterSpec.ofExtract().addSalt((SecretKey) null); check(false, "addSalt null key must throw"); }
        catch (NullPointerException e) { check(true, "addSalt(SecretKey) NPE"); }

        // thenExpand length validation: 0 and negative -> IllegalArgumentException
        try { HKDFParameterSpec.ofExtract().thenExpand(new byte[0], 0); check(false, "length 0 must throw"); }
        catch (IllegalArgumentException e) { check(true, "thenExpand length 0 IAE"); }
        try { HKDFParameterSpec.ofExtract().thenExpand(null, -1); check(false, "negative length must throw"); }
        catch (IllegalArgumentException e) { check(true, "thenExpand negative length IAE"); }
        // null info is accepted and reported as null
        check(HKDFParameterSpec.ofExtract().thenExpand(null, 10).info() == null, "null info accepted");

        // strongest chain: builder output feeds KDF("HKDF-SHA256") with RFC 5869 exact results
        KDF kdf = KDF.getInstance("HKDF-SHA256");
        HKDFParameterSpec.Extract extract =
                HKDFParameterSpec.ofExtract().addIKM(IKM).addSalt(SALT).extractOnly();
        check(java.util.HexFormat.of().formatHex(kdf.deriveKey("PRK", extract).getEncoded()).equals(PRK1),
                "builder extractOnly exact PRK");
        byte[] okm = kdf.deriveData(
                HKDFParameterSpec.ofExtract().addIKM(IKM).addSalt(SALT).thenExpand(INFO, 42));
        check(java.util.HexFormat.of().formatHex(okm).equals(OKM1), "builder thenExpand exact OKM");
        // piecewise adds (split IKM/salt) concatenate to the same result as single adds
        byte[] okmSplit = kdf.deriveData(HKDFParameterSpec.ofExtract()
                .addIKM(Arrays.copyOfRange(IKM, 0, 11)).addIKM(Arrays.copyOfRange(IKM, 11, 22))
                .addSalt(Arrays.copyOfRange(SALT, 0, 5)).addSalt(Arrays.copyOfRange(SALT, 5, 13))
                .thenExpand(INFO, 42));
        check(Arrays.equals(okm, okmSplit), "split adds equal single adds");
    }

    static byte[] bytes(String hex) { return java.util.HexFormat.of().parseHex(hex); }
    static byte[] filled(int b, int len) { byte[] r = new byte[len]; Arrays.fill(r, (byte) b); return r; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
}
