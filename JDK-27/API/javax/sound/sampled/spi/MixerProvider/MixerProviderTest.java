/*
 * @test
 * @summary javax.sound.sampled.spi.MixerProvider conformance: white-box structure + black-box behavior.
 * @run main MixerProviderTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

public class MixerProviderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.spi.MixerProvider");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected constructor");
        check(Modifier.isAbstract(c.getMethod("getMixerInfo").getModifiers()), "getMixerInfo abstract");
        check(Modifier.isAbstract(c.getMethod("getMixer", Mixer.Info.class).getModifiers()), "getMixer abstract");
        check(!Modifier.isAbstract(c.getMethod("isMixerSupported", Mixer.Info.class).getModifiers()), "isMixerSupported concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Concrete convenience method on an anonymous subclass with no mixers.
        MixerProviderImpl p = new MixerProviderImpl();
        check(!p.isMixerSupported(new MixInfo("n", "v", "d", "1")), "empty provider: mixer unsupported");
        check(p.getMixerInfo().length == 0, "empty provider: no mixer info");

        // Real providers via AudioSystem public API.
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        check(infos != null, "getMixerInfo non-null");
        if (infos.length > 0) {
            Mixer m = AudioSystem.getMixer(infos[0]);
            check(m != null, "getMixer(first) non-null");
            check(m.getMixerInfo() != null, "mixer info non-null");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class MixerProviderImpl extends javax.sound.sampled.spi.MixerProvider {
    public Mixer.Info[] getMixerInfo() { return new Mixer.Info[0]; }
    public Mixer getMixer(Mixer.Info info) { return null; }
}

class MixInfo extends Mixer.Info {
    MixInfo(String a, String b, String c, String d) { super(a, b, c, d); }
}
