/*
 * @test
 * @summary javax.sound.sampled.AudioFormat.Encoding conformance: white-box structure + black-box behavior.
 * @run main EncodingTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;

public class EncodingTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.AudioFormat$Encoding");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods) && Modifier.isStatic(mods), "public static");
        check(c.getEnclosingClass() == AudioFormat.class, "enclosing class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioFormat.Encoding signed   = AudioFormat.Encoding.PCM_SIGNED;
        AudioFormat.Encoding unsigned = AudioFormat.Encoding.PCM_UNSIGNED;
        AudioFormat.Encoding flt      = AudioFormat.Encoding.PCM_FLOAT;
        AudioFormat.Encoding ulaw     = AudioFormat.Encoding.ULAW;
        AudioFormat.Encoding alaw     = AudioFormat.Encoding.ALAW;

        check(signed != null && unsigned != null && flt != null && ulaw != null && alaw != null, "constants non-null");
        check(signed != unsigned && unsigned != flt && flt != ulaw && ulaw != alaw, "constants distinct");

        check("PCM_SIGNED".equals(signed.toString()), "PCM_SIGNED name");
        check("PCM_UNSIGNED".equals(unsigned.toString()), "PCM_UNSIGNED name");
        check("PCM_FLOAT".equals(flt.toString()), "PCM_FLOAT name");
        check("ULAW".equals(ulaw.toString()), "ULAW name");
        check("ALAW".equals(alaw.toString()), "ALAW name");

        // equals/hashCode compare by name (EncodingEquals bug 4925483 behavior)
        AudioFormat.Encoding same = new AudioFormat.Encoding("PCM_SIGNED");
        check(signed.equals(same), "equals by name");
        check(same.equals(signed), "equals symmetric");
        check(signed.hashCode() == same.hashCode(), "hashCode by name");
        check(!signed.equals(unsigned), "different encoding not equal");
        check(!signed.equals(null), "not equal null");
        check(!signed.equals(new Object()), "not equal Object");

        // custom name round-trips through constructor/toString
        AudioFormat.Encoding custom = new AudioFormat.Encoding("MY_ENC");
        check("MY_ENC".equals(custom.toString()), "custom encoding name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
