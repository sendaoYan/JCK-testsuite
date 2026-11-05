/*
 * @test
 * @summary java.applet.AudioClip conformance: interface face + real clip behavior.
 * @run main AudioClipTest
 */

import java.applet.Applet;
import java.applet.AudioClip;
import java.lang.reflect.*;
import java.net.*;

public class AudioClipTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    /** recording implementation exercising the contract */
    static class RecClip implements AudioClip {
        int playCalls, loopCalls, stopCalls;
        public void play() { playCalls++; }
        public void loop() { loopCalls++; }
        public void stop() { stopCalls++; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.applet.AudioClip");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(hasMethod(c, "play"), "method play");
        check(hasMethod(c, "loop"), "method loop");
        check(hasMethod(c, "stop"), "method stop");
        check(c.getDeclaredMethods().length == 3, "exactly 3 declared methods (got " + c.getDeclaredMethods().length + ")");
        check(c.getDeclaredMethod("play").getReturnType() == void.class, "play() returns void");
        check(c.getDeclaredMethod("loop").getReturnType() == void.class, "loop() returns void");
        check(c.getDeclaredMethod("stop").getReturnType() == void.class, "stop() returns void");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    "method " + m.getName() + " is public abstract");
            check(m.getParameterTypes().length == 0, "method " + m.getName() + " takes no arguments");
        }
    }

    // ---- Part 2: black-box ----
    static void testBlackBox() throws Exception {
        // contract exercised via the recording implementation
        RecClip clip = new RecClip();
        clip.play();
        clip.loop();
        clip.stop();
        check(clip.playCalls == 1 && clip.loopCalls == 1 && clip.stopCalls == 1,
                "play/loop/stop each invoked exactly once");
        clip.stop();
        check(clip.stopCalls == 2, "stop is callable repeatedly");
        check(clip instanceof AudioClip, "implementation is an AudioClip");

        // the static factory is the only JDK-supplied entry point without a browser
        try {
            Applet.newAudioClip(null);
            check(false, "expected NPE for newAudioClip(null)");
        } catch (NullPointerException expected) {
            check(true, "newAudioClip(null) -> NullPointerException");
        }
        // missing resource: a clip is still produced and stays quiet on all operations
        AudioClip missing = Applet.newAudioClip(new URL("file:/definitely-missing-test.au"));
        check(missing != null && missing instanceof AudioClip, "newAudioClip(missing file) returns an AudioClip");
        missing.play();
        missing.loop();
        missing.stop();
        check(true, "missing clip: play/loop/stop do not throw");
        // malformed URL: rejected by URL itself, never reaching the factory
        try {
            new URL("not a url");
            check(false, "expected MalformedURLException");
        } catch (MalformedURLException expected) {
            check(true, "malformed URL rejected before the factory");
        }
        // deterministic type: repeated creation of a missing clip yields the JDK clip type
        AudioClip again = Applet.newAudioClip(new URL("file:/definitely-missing-test.au"));
        check(missing.getClass() == again.getClass(), "newAudioClip is deterministic in the produced type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
