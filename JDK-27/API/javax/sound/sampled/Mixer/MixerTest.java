/*
 * @test
 * @summary javax.sound.sampled.Mixer conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main MixerTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class MixerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Mixer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("javax.sound.sampled.Line"), "extends Line");
        for (String m : new String[]{"getMixerInfo", "getSourceLineInfo", "getTargetLineInfo", "isLineSupported", "getLine", "getMaxLines", "getSourceLines", "getTargetLines", "synchronize", "unsynchronize", "isSynchronizationSupported"}) {
            check(hasMethod(c, m), "method " + m);
        }
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        check(infos != null, "getMixerInfo non-null");
        if (infos.length == 0) {
            return; // no mixers installed: enumeration paths already asserted.
        }

        Mixer mixer = AudioSystem.getMixer(infos[0]);
        check(mixer != null, "getMixer(first) non-null");

        Mixer.Info info = mixer.getMixerInfo();
        check(info != null, "mixer getMixerInfo non-null");
        check(info.getName() != null, "mixer name non-null");
        check(info.getVendor() != null, "mixer vendor non-null");
        check(info.getDescription() != null, "mixer description non-null");
        check(info.getVersion() != null, "mixer version non-null");

        Line.Info[] src = mixer.getSourceLineInfo();
        Line.Info[] tgt = mixer.getTargetLineInfo();
        check(src != null, "getSourceLineInfo non-null");
        check(tgt != null, "getTargetLineInfo non-null");

        AudioFormat fmt = new AudioFormat(8000f, 8, 1, true, false);
        DataLine.Info sdlInfo = new DataLine.Info(SourceDataLine.class, fmt);
        Line.Info[] matchingSrc = mixer.getSourceLineInfo(sdlInfo);
        check(matchingSrc != null, "getSourceLineInfo(filter) non-null");
        check(mixer.isLineSupported(sdlInfo) == (matchingSrc.length > 0), "isLineSupported consistent with source line info");

        check(mixer.getTargetLineInfo(new Line.Info(TargetDataLine.class)) != null, "getTargetLineInfo(filter) non-null");
        check(mixer.getMaxLines(new Line.Info(SourceDataLine.class)) >= -1, "getMaxLines value");

        Line[] sourceLines = mixer.getSourceLines();
        Line[] targetLines = mixer.getTargetLines();
        check(sourceLines != null, "getSourceLines non-null");
        check(targetLines != null, "getTargetLines non-null");
        if (sourceLines.length > 0) {
            mixer.isSynchronizationSupported(sourceLines, false);
            mixer.synchronize(sourceLines, false);
            mixer.unsynchronize(sourceLines);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
