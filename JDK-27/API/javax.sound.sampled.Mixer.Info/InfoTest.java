/*
 * @test
 * @summary javax.sound.sampled.Mixer.Info conformance: white-box structure + black-box behavior.
 * @run main InfoTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.Mixer;

public class InfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.Mixer$Info");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor(String.class, String.class, String.class, String.class);
        check(Modifier.isProtected(ctor.getModifiers()), "protected constructor");
        check(c.getMethod("getName") != null, "method getName");
        check(c.getMethod("getVendor") != null, "method getVendor");
        check(c.getMethod("getDescription") != null, "method getDescription");
        check(c.getMethod("getVersion") != null, "method getVersion");
        check(c.getMethod("toString") != null, "method toString");
        check(c.getMethod("equals", Object.class) != null, "method equals");
        check(c.getMethod("hashCode") != null, "method hashCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Mixer.Info i1 = new MixInfo("n", "v", "d", "1.0");
        check("n".equals(i1.getName()), "getName");
        check("v".equals(i1.getVendor()), "getVendor");
        check("d".equals(i1.getDescription()), "getDescription");
        check("1.0".equals(i1.getVersion()), "getVersion");
        check(i1.toString() != null && i1.toString().length() > 0, "toString non-empty");

        check(i1.equals(i1), "equals self");
        check(!i1.equals(null), "equals null false");
        check(!i1.equals("n"), "equals foreign type false");
        Mixer.Info same = new MixInfo("n", "v", "d", "1.0");
        check(!i1.equals(same), "identity equals: distinct instances not equal");
        check(i1.hashCode() == i1.hashCode(), "hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class MixInfo extends Mixer.Info {
    MixInfo(String a, String b, String c, String d) { super(a, b, c, d); }
}
