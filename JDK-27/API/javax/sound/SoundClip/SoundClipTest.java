/*
 * @test
 * @key sound
 * @summary javax.sound.SoundClip conformance: white-box structure + black-box behavior.
 * @run main SoundClipTest
 */

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.sound.SoundClip;

public class SoundClipTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.SoundClip");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isFinal(mods), "final modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("createSoundClip"), "method createSoundClip");
        check(dm.contains("canPlay"), "method canPlay");
        check(dm.contains("isPlaying"), "method isPlaying");
        check(dm.contains("play"), "method play");
        check(dm.contains("loop"), "method loop");
        check(dm.contains("stop"), "method stop");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "constructor count");
        check(!Modifier.isPublic(ctors[0].getModifiers()), "constructor not public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-soundclip");
        try {
            expectThrows(NullPointerException.class, () -> SoundClip.createSoundClip(null),
                    "createSoundClip(null)");

            File missing = dir.resolve("missing.wav").toFile();
            expectThrows(IOException.class, () -> SoundClip.createSoundClip(missing),
                    "createSoundClip(missing file)");

            File garbage = dir.resolve("garbage.wav").toFile();
            Files.write(garbage.toPath(), new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
            SoundClip sc = SoundClip.createSoundClip(garbage);
            check(sc != null, "createSoundClip returns non-null for readable file");
            check(sc.canPlay() == false, "canPlay false for unsupported data");
            check(sc.isPlaying() == false, "isPlaying false before play");

            sc.play();
            check(sc.isPlaying() == false, "play is no-op when not playable");
            sc.loop();
            check(sc.isPlaying() == false, "loop is no-op when not playable");
            sc.stop();
            check(sc.isPlaying() == false, "stop is no-op when not playable");
            check(sc.canPlay() == false, "canPlay unchanged after play/loop/stop");
        } finally {
            Files.deleteIfExists(dir.resolve("missing.wav"));
            Files.deleteIfExists(dir.resolve("garbage.wav"));
            Files.deleteIfExists(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
