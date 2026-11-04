/*
 * @test
 * @summary javax.sound.sampled.Port.Info conformance: white-box structure + black-box behavior.
 * @run main InfoTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.Line;
import javax.sound.sampled.Port;

public class InfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Port$Info");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.sampled.Line$Info"), "extends Line.Info");
        for (String f : new String[]{"MICROPHONE", "LINE_IN", "COMPACT_DISC", "SPEAKER", "HEADPHONE", "LINE_OUT"}) {
            check(c.getField(f) != null, "constant " + f);
        }
        check(c.getConstructor(Class.class, String.class, boolean.class) != null, "constructor(Class,String,boolean)");
        check(c.getMethod("getName") != null, "method getName");
        check(c.getMethod("isSource") != null, "method isSource");
        check(c.getMethod("matches", Line.Info.class) != null, "method matches");
        check(c.getMethod("equals", Object.class) != null, "method equals");
        check(c.getMethod("hashCode") != null, "method hashCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Port.Info mic = Port.Info.MICROPHONE;
        Port.Info speaker = Port.Info.SPEAKER;
        check(mic != null, "MICROPHONE non-null");
        check(speaker != null, "SPEAKER non-null");
        check(Port.Info.LINE_IN != null, "LINE_IN non-null");
        check(Port.Info.COMPACT_DISC != null, "COMPACT_DISC non-null");
        check(Port.Info.HEADPHONE != null, "HEADPHONE non-null");
        check(Port.Info.LINE_OUT != null, "LINE_OUT non-null");

        check(mic.isSource(), "MICROPHONE is source");
        check(!speaker.isSource(), "SPEAKER is target (not source)");
        check("MICROPHONE".equals(mic.getName()), "MICROPHONE name");
        check("SPEAKER".equals(speaker.getName()), "SPEAKER name");
        check(mic.getLineClass() == Port.class, "MICROPHONE line class Port");
        check(speaker.getLineClass() == Port.class, "SPEAKER line class Port");

        check(mic.equals(mic), "MICROPHONE equals self");
        check(!mic.equals(null), "equals null false");
        check(!mic.equals(speaker), "MICROPHONE != SPEAKER");
        check(mic.hashCode() == mic.hashCode(), "hashCode stable");
        check(mic.matches(mic), "matches self");
        check(mic.toString() != null && mic.toString().length() > 0, "toString non-empty");

        Port.Info custom = new Port.Info(Port.class, "Custom", true);
        check(custom.isSource(), "custom isSource");
        check("Custom".equals(custom.getName()), "custom name");
        check(custom.getLineClass() == Port.class, "custom line class");
        Port.Info custom2 = new Port.Info(Port.class, "Custom", true);
        check(!custom.equals(custom2), "identity equals: distinct custom instances not equal");
        check(custom.matches(custom2), "matches same name/isSource/lineClass");
        check(custom2.matches(custom), "matches symmetric");
        check(custom.hashCode() == custom.hashCode(), "custom hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
