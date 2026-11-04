/*
 * @test
 * @summary javax.sound.sampled.AudioFormat conformance: white-box structure + black-box behavior.
 * @run main AudioFormatTest
 */

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

public class AudioFormatTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.AudioFormat");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getDeclaredConstructors().length == 3, "constructor count 3");

        Class<?> e = Class.forName("javax.sound.sampled.AudioFormat$Encoding");
        check(e != null && e.getEnclosingClass() == c, "Encoding nested");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 7-arg explicit constructor
        AudioFormat f = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false);
        check(f.getEncoding() == AudioFormat.Encoding.PCM_SIGNED, "encoding");
        check(f.getSampleRate() == 44100f, "sampleRate");
        check(f.getSampleSizeInBits() == 16, "sampleSizeInBits");
        check(f.getChannels() == 2, "channels");
        check(f.getFrameSize() == 4, "frameSize");
        check(f.getFrameRate() == 44100f, "frameRate");
        check(!f.isBigEndian(), "bigEndian false");
        check(f.properties().isEmpty(), "default properties empty");
        check(f.getProperty("bitrate") == null, "default property null");

        // PCM convenience constructor derives frameSize/frameRate/encoding
        AudioFormat pcm = new AudioFormat(22050f, 16, 1, true, true);
        check(pcm.getEncoding() == AudioFormat.Encoding.PCM_SIGNED, "PCM signed encoding");
        check(pcm.getSampleRate() == 22050f, "PCM sampleRate");
        check(pcm.getChannels() == 1, "PCM channels");
        check(pcm.getFrameSize() == 2, "PCM frameSize = ceil(16/8)*1");
        check(pcm.getFrameRate() == 22050f, "PCM frameRate == sampleRate");
        check(pcm.isBigEndian(), "PCM bigEndian true");

        AudioFormat upcm = new AudioFormat(8000f, 8, 1, false, false);
        check(upcm.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED, "PCM unsigned encoding");
        check(upcm.getFrameSize() == 1, "8-bit frameSize 1");

        // 8-arg constructor with properties
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("bitrate", Integer.valueOf(128000));
        AudioFormat fp = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false, props);
        check(Integer.valueOf(128000).equals(fp.getProperty("bitrate")), "property bitrate");
        check(fp.properties().size() == 1, "properties size 1");

        // matches: equal formats match, properties are ignored
        AudioFormat same = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false);
        check(f.matches(same), "identical formats match");
        AudioFormat sameProps = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false, props);
        check(f.matches(sameProps), "properties ignored in matches");

        // NOT_SPECIFIED wildcards match concrete values
        AudioFormat wild = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, false);
        check(f.matches(wild), "NOT_SPECIFIED wildcard matches");

        // differing concrete values do not match
        AudioFormat otherRate = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 22050f, 16, 2, 4, 44100f, false);
        check(!f.matches(otherRate), "different sampleRate not match");
        AudioFormat otherEnc = new AudioFormat(AudioFormat.Encoding.ULAW, 44100f, 16, 2, 4, 44100f, false);
        check(!f.matches(otherEnc), "different encoding not match");
        // big-endian differs and sample size > 8 -> no match
        AudioFormat otherEndian = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, true);
        check(!f.matches(otherEndian), "byte order differs (16-bit) not match");

        // toString is non-empty and carries the encoding name
        String s = f.toString();
        check(s != null && s.contains("PCM_SIGNED"), "toString has encoding");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
