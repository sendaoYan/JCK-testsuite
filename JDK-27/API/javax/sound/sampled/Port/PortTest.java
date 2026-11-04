/*
 * @test
 * @summary javax.sound.sampled.Port conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main PortTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;

public class PortTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Port");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(c.getInterfaces()[0].getName().equals("javax.sound.sampled.Line"), "extends Line");
        check(c.getDeclaredMethods().length == 0, "Port declares no methods of its own");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Port adds no methods; exercise through Port.Info and line queries.
        Line.Info portInfo = new Line.Info(Port.class);
        Line.Info[] srcPorts = AudioSystem.getSourceLineInfo(portInfo);
        Line.Info[] tgtPorts = AudioSystem.getTargetLineInfo(portInfo);
        check(srcPorts != null, "getSourceLineInfo(Port) non-null");
        check(tgtPorts != null, "getTargetLineInfo(Port) non-null");
        check(AudioSystem.isLineSupported(portInfo) == (srcPorts.length > 0 || tgtPorts.length > 0), "isLineSupported(Port) consistent");

        if (srcPorts.length > 0) {
            try {
                Line l = AudioSystem.getLine(srcPorts[0]);
                l.open();
                check(l.isOpen(), "port line open");
                l.close();
                check(!l.isOpen(), "port line close");
            } catch (LineUnavailableException lue) {
                // Port line not available: acceptable.
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
