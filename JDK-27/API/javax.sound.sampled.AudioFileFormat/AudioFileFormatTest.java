/*
 * @test
 * @summary javax.sound.sampled.AudioFileFormat conformance: white-box structure + black-box behavior.
 * @run main AudioFileFormatTest
 */

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

public class AudioFileFormatTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.AudioFileFormat");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getDeclaredConstructors().length == 3, "constructor count 3");

        Class<?> t = Class.forName("javax.sound.sampled.AudioFileFormat$Type");
        check(t != null, "nested Type loads");
        check(t.getEnclosingClass() == c, "Type enclosed by AudioFileFormat");
        check(Modifier.isPublic(t.getModifiers()) && Modifier.isStatic(t.getModifiers()), "Type public static");
    }

    // concrete subclass to exercise the protected (byteLength) constructor
    static class CustomFileFormat extends AudioFileFormat {
        CustomFileFormat(AudioFileFormat.Type type, int byteLength, AudioFormat format, int frameLength) {
            super(type, byteLength, format, frameLength);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false);

        // 3-arg public constructor
        AudioFileFormat aff = new AudioFileFormat(AudioFileFormat.Type.WAVE, fmt, 100);
        check(aff.getType() == AudioFileFormat.Type.WAVE, "getType WAVE");
        check(aff.getFormat() == fmt, "getFormat identity");
        check(aff.getFrameLength() == 100, "getFrameLength 100");
        check(aff.getByteLength() == AudioSystem.NOT_SPECIFIED, "3-arg byteLength NOT_SPECIFIED");
        check(aff.properties().isEmpty(), "3-arg properties empty");
        check(aff.getProperty("author") == null, "absent property null");

        // 4-arg public constructor with properties map
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("author", "me");
        props.put("duration", Long.valueOf(5000L));
        AudioFileFormat aff2 = new AudioFileFormat(AudioFileFormat.Type.AU, fmt, 50, props);
        check("me".equals(aff2.getProperty("author")), "property author");
        check(Long.valueOf(5000L).equals(aff2.getProperty("duration")), "property duration");
        check(aff2.properties().size() == 2, "properties size 2");
        check(aff2.getFrameLength() == 50, "4-arg frameLength");

        // protected constructor -> byteLength round-trip
        CustomFileFormat cff = new CustomFileFormat(AudioFileFormat.Type.AIFF, 1234, fmt, 60);
        check(cff.getByteLength() == 1234, "byteLength round-trip");
        check(cff.getType() == AudioFileFormat.Type.AIFF, "AIFF type");
        check(cff.getFrameLength() == 60, "frameLength 60");

        // toString includes type and extension
        String s = aff.toString();
        check(s.contains("WAVE") && s.contains(".wav"), "toString has WAVE + .wav");
        check(aff2.toString().contains(".au"), "toString has .au");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
