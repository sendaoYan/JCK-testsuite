/*
 * @test
 * @summary javax.sound.sampled.AudioFileFormat.Type conformance: white-box structure + black-box behavior.
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFileFormat;

public class TypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.AudioFileFormat$Type");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods) && Modifier.isStatic(mods), "public static");
        check(c.getEnclosingClass() == AudioFileFormat.class, "enclosing class");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioFileFormat.Type wave = AudioFileFormat.Type.WAVE;
        AudioFileFormat.Type au   = AudioFileFormat.Type.AU;
        AudioFileFormat.Type aiff = AudioFileFormat.Type.AIFF;
        AudioFileFormat.Type aifc = AudioFileFormat.Type.AIFC;
        AudioFileFormat.Type snd  = AudioFileFormat.Type.SND;

        check(wave != null && au != null && aiff != null && aifc != null && snd != null, "constants non-null");
        check(wave != au && au != aiff && aiff != aifc && aifc != snd && snd != wave, "constants distinct");

        // names
        check("WAVE".equals(wave.toString()), "WAVE name");
        check("AU".equals(au.toString()), "AU name");
        check("AIFF".equals(aiff.toString()), "AIFF name");
        check("AIFF-C".equals(aifc.toString()), "AIFF-C name");
        check("SND".equals(snd.toString()), "SND name");

        // extensions (no leading dot)
        check("wav".equals(wave.getExtension()), "wav extension");
        check("au".equals(au.getExtension()), "au extension");
        check("aif".equals(aiff.getExtension()), "aif extension");
        check("aifc".equals(aifc.getExtension()), "aifc extension");
        check("snd".equals(snd.getExtension()), "snd extension");
        check(wave.getExtension().charAt(0) != '.', "extension no leading dot");

        // equals / hashCode compare by name, not identity
        AudioFileFormat.Type same = new AudioFileFormat.Type("WAVE", "xyz");
        check(wave.equals(same), "equals by name");
        check(same.equals(wave), "equals symmetric");
        check(wave.hashCode() == same.hashCode(), "hashCode by name");
        check(!wave.equals(au), "different type not equal");
        check(!wave.equals(null), "not equal null");
        check(!wave.equals(new Object()), "not equal Object");
        check("xyz".equals(same.getExtension()), "custom extension preserved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
