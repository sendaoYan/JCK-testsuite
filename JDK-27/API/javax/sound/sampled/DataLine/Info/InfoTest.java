/*
 * @test
 * @summary javax.sound.sampled.DataLine.Info conformance: white-box structure + black-box behavior.
 * @run main InfoTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;

public class InfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.DataLine$Info");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Line$Info"), "extends Line.Info");
        check(c.getConstructor(Class.class, AudioFormat[].class, int.class, int.class) != null, "constructor(Class,AudioFormat[],int,int)");
        check(c.getConstructor(Class.class, AudioFormat.class, int.class) != null, "constructor(Class,AudioFormat,int)");
        check(c.getConstructor(Class.class, AudioFormat.class) != null, "constructor(Class,AudioFormat)");
        check(c.getMethod("getFormats") != null, "method getFormats");
        check(c.getMethod("isFormatSupported", AudioFormat.class) != null, "method isFormatSupported");
        check(c.getMethod("getMinBufferSize") != null, "method getMinBufferSize");
        check(c.getMethod("getMaxBufferSize") != null, "method getMaxBufferSize");
        check(c.getMethod("matches", Line.Info.class) != null, "method matches");
        check(c.getMethod("toString") != null, "method toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);

        // 4-arg constructor: formats + buffer range
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, new AudioFormat[]{fmt}, 1024, 4096);
        check(info.getLineClass() == SourceDataLine.class, "lineClass");
        check(info.getFormats().length == 1, "single format");
        check(info.getFormats()[0].matches(fmt), "stored format matches");
        check(info.isFormatSupported(fmt), "isFormatSupported true");
        check(!info.isFormatSupported(new AudioFormat(22050f, 16, 2, true, false)), "isFormatSupported false for different format");
        check(info.getMinBufferSize() == 1024, "minBufferSize");
        check(info.getMaxBufferSize() == 4096, "maxBufferSize");
        check(info.matches(info), "matches self");
        check(!info.matches(new Line.Info(SourceDataLine.class)), "does not match plain Line.Info");
        check(!info.matches(new DataLine.Info(Clip.class, fmt)), "does not match different line class");
        check(info.toString() != null && info.toString().length() > 0, "toString non-empty");

        // defensive copy of formats
        AudioFormat[] fs = info.getFormats();
        fs[0] = null;
        check(info.getFormats()[0] != null, "getFormats returns defensive copy");

        // 2-arg constructor: single format, NOT_SPECIFIED buffer
        DataLine.Info info2 = new DataLine.Info(SourceDataLine.class, fmt);
        check(info2.getFormats().length == 1, "single format (2-arg)");
        check(info2.getMinBufferSize() == AudioSystem.NOT_SPECIFIED, "min NOT_SPECIFIED");
        check(info2.getMaxBufferSize() == AudioSystem.NOT_SPECIFIED, "max NOT_SPECIFIED");

        // 3-arg constructor: single format + fixed buffer size
        DataLine.Info info3 = new DataLine.Info(SourceDataLine.class, fmt, 2048);
        check(info3.getMinBufferSize() == 2048, "3-arg min buffer");
        check(info3.getMaxBufferSize() == 2048, "3-arg max buffer");
        check(info3.isFormatSupported(fmt), "3-arg isFormatSupported");

        // null format constructor produces empty formats
        DataLine.Info info4 = new DataLine.Info(SourceDataLine.class, (AudioFormat) null);
        check(info4.getFormats().length == 0, "null format -> empty formats");
        check(!info4.isFormatSupported(fmt), "empty formats -> isFormatSupported false");
        check(info4.getMinBufferSize() == AudioSystem.NOT_SPECIFIED, "null format min NOT_SPECIFIED");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
