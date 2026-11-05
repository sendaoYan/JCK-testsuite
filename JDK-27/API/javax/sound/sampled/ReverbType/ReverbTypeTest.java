/*
 * @test
 * @summary javax.sound.sampled.ReverbType conformance: white-box structure + black-box behavior.
 * @run main ReverbTypeTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.ReverbType;

public class ReverbTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.ReverbType");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // name, earlyReflectionDelay(int), earlyReflectionIntensity(float),
        // lateReflectionDelay(int), lateReflectionIntensity(float), decayTime(int)
        ReverbType cavern = new ReverbType("Cavern", 10000, -1.4f, 40000, -2.0f, 2250000) {};

        check("Cavern".equals(cavern.getName()), "getName");
        check(cavern.getEarlyReflectionDelay() == 10000, "earlyReflectionDelay");
        check(cavern.getEarlyReflectionIntensity() == -1.4f, "earlyReflectionIntensity");
        check(cavern.getLateReflectionDelay() == 40000, "lateReflectionDelay");
        check(cavern.getLateReflectionIntensity() == -2.0f, "lateReflectionIntensity");
        check(cavern.getDecayTime() == 2250000, "decayTime");

        String s = cavern.toString();
        check(s.startsWith("Cavern"), "toString starts with name");
        check(s.contains("early reflection delay") && s.contains("decay time"), "toString has parameters");

        // equality is identity (Object.equals), not value-based
        ReverbType other = new ReverbType("Cavern", 10000, -1.4f, 40000, -2.0f, 2250000) {};
        check(!cavern.equals(other), "equal params but distinct instances not equal");
        check(cavern.equals(cavern), "reflexive");
        check(!cavern.equals(null), "not equal null");
        check(cavern.hashCode() == cavern.hashCode(), "hashCode stable");

        // distinct values are preserved independently
        ReverbType closet = new ReverbType("Closet", 600, -7.0f, 2500, -10.0f, 150000) {};
        check("Closet".equals(closet.getName()), "second name");
        check(closet.getDecayTime() == 150000, "second decayTime");
        check(closet.getLateReflectionIntensity() == -10.0f, "second lateReflectionIntensity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: subclass fields + no equals + toString ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.ReverbType rt = new javax.sound.sampled.ReverbType("Deep", 100, 0.2f, 5000, 0.5f, 2000) {};
        check("Deep".equals(rt.getName()), "name");
        check(rt.getEarlyReflectionDelay() == 100 && rt.getEarlyReflectionIntensity() == 0.2f, "early fields");
        check(rt.getLateReflectionDelay() == 5000 && rt.getLateReflectionIntensity() == 0.5f, "late fields");
        check(rt.getDecayTime() == 2000, "decay time");
        javax.sound.sampled.ReverbType twin = new javax.sound.sampled.ReverbType("Deep", 100, 0.2f, 5000, 0.5f, 2000) {};
        check(!rt.equals(twin), "no value-equals (identity only)");
        check(rt.toString().contains("Deep"), "toString contains name");
        check(rt.toString().contains("100"), "toString contains early delay");
        check(java.lang.reflect.Modifier.isProtected(javax.sound.sampled.ReverbType.class.getDeclaredConstructors()[0].getModifiers()), "ctor protected");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    // hand-built WAV: RIFF/WAVE, PCM 8000Hz 8-bit mono, samples {16,-16}, 44-byte header
    static byte[] wav8() throws java.io.IOException {
        java.io.ByteArrayOutputStream w = new java.io.ByteArrayOutputStream();
        byte[] pcm = { 0x10, (byte) 0xF0 };
        w.write('R'); w.write('I'); w.write('F'); w.write('F');
        int riffLen = 36 + pcm.length;
        w.write(riffLen & 0xFF); w.write((riffLen >> 8) & 0xFF); w.write((riffLen >> 16) & 0xFF); w.write((riffLen >> 24) & 0xFF);
        w.write('W'); w.write('A'); w.write('V'); w.write('E');
        w.write('f'); w.write('m'); w.write('t'); w.write(' ');
        w.write(16); w.write(0); w.write(0); w.write(0);
        w.write(1); w.write(0); w.write(1); w.write(0);
        int rate = 8000; w.write(rate & 0xFF); w.write((rate >> 8) & 0xFF); w.write((rate >> 16) & 0xFF); w.write((rate >> 24) & 0xFF);
        w.write(rate & 0xFF); w.write((rate >> 8) & 0xFF); w.write((rate >> 16) & 0xFF); w.write((rate >> 24) & 0xFF);
        w.write(1); w.write(0); w.write(8); w.write(0);
        w.write('d'); w.write('a'); w.write('t'); w.write('a');
        w.write(pcm.length & 0xFF); w.write(0); w.write(0); w.write(0);
        w.write(pcm);
        return w.toByteArray();
    }
    // hand-built AU (.snd, big-endian header): encoding 1 = 8-bit mu-law, 8000Hz mono, 3 data bytes
    static byte[] au(int sizeField, int enc, int rate, int ch, byte[] data) throws java.io.IOException {
        java.io.ByteArrayOutputStream u = new java.io.ByteArrayOutputStream();
        u.write('.'); u.write('s'); u.write('n'); u.write('d');
        u.write((sizeField >> 24) & 0xFF); u.write((sizeField >> 16) & 0xFF); u.write((sizeField >> 8) & 0xFF); u.write(sizeField & 0xFF);
        u.write(0); u.write(0); u.write(0); u.write(24);
        u.write((enc >> 24) & 0xFF); u.write((enc >> 16) & 0xFF); u.write((enc >> 8) & 0xFF); u.write(enc & 0xFF);
        u.write((rate >> 24) & 0xFF); u.write((rate >> 16) & 0xFF); u.write((rate >> 8) & 0xFF); u.write(rate & 0xFF);
        u.write((ch >> 24) & 0xFF); u.write((ch >> 16) & 0xFF); u.write((ch >> 8) & 0xFF); u.write(ch & 0xFF);
        u.write(data);
        return u.toByteArray();
    }
    static byte[] auUlaw() throws java.io.IOException { return au(27, 1, 8000, 1, new byte[] { 0, (byte) 0xFF, 0x7F }); }

}
