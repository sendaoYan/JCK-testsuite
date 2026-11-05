/*
 * @test
 * @summary javax.sound.sampled.AudioPermission conformance: white-box structure + black-box behavior.
 * @run main AudioPermissionTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioPermission;

public class AudioPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.AudioPermission");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.BasicPermission"), "superclass BasicPermission");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioPermission play = new AudioPermission("play");
        AudioPermission record = new AudioPermission("record");

        check("play".equals(play.getName()), "name play");
        check("record".equals(record.getName()), "name record");
        check("".equals(play.getActions()), "actions empty");

        // name round-trip through the two-arg constructor
        AudioPermission play2 = new AudioPermission("play", null);
        check("play".equals(play2.getName()), "two-arg name play");

        // equals / hashCode
        check(play.equals(new AudioPermission("play")), "equal by name");
        check(!play.equals(record), "different name not equal");
        check(play.hashCode() == new AudioPermission("play").hashCode(), "hashCode by name");
        check(!play.equals(null), "not equal null");

        // implies uses BasicPermission wildcard semantics
        AudioPermission all = new AudioPermission("*");
        check(all.implies(play), "* implies play");
        check(all.implies(record), "* implies record");
        check(play.implies(play), "play implies play");
        check(!play.implies(record), "play does not imply record");
        check(new AudioPermission("play.*").implies(new AudioPermission("play.record")), "play.* implies play.record");
        check(!new AudioPermission("play.*").implies(new AudioPermission("play")), "play.* does not imply play");

        // toString carries the name
        check(play.toString().contains("play"), "toString has name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: names / actions / null contract ----
    static void testContractDeep() throws Exception {
        javax.sound.sampled.AudioPermission play = new javax.sound.sampled.AudioPermission("play");
        check("play".equals(play.getName()), "play name");
        check("".equals(play.getActions()), "empty actions");
        javax.sound.sampled.AudioPermission rec = new javax.sound.sampled.AudioPermission("record");
        check("record".equals(rec.getName()), "record name");
        javax.sound.sampled.AudioPermission both = new javax.sound.sampled.AudioPermission("play,record");
        check("play,record".equals(both.getName()), "multi name kept verbatim");
        check(javax.sound.sampled.AudioPermission.class.getSuperclass() == java.security.BasicPermission.class, "extends BasicPermission");
        check(play.implies(rec) == false, "play does not imply record");
        check(play.implies(play), "self implies");
        try { new javax.sound.sampled.AudioPermission(null); check(false, "null name must NPE"); }
        catch (NullPointerException e) { passed++; }
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
