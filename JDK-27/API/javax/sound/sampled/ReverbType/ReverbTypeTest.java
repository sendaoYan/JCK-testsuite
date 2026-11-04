/*
 * @test
 * @summary javax.sound.sampled.ReverbType conformance: white-box structure + black-box behavior.
 * @run main ReverbTypeTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.ReverbType;

public class ReverbTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.ReverbType");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // name, earlyReflectionDelay(int), earlyReflectionIntensity(float),
        // lateReflectionDelay(int), lateReflectionIntensity(float), decayTime(int)
        ReverbType cavern = new ReverbType("Cavern", 10000, -1.4f, 40000, -2.0f, 2250000) {};

        check("Cavern".equals(cavern.getName()), "getName");
        check(cavern.getEarlyReflectionDelay() == 10000, "earlyReflectionDelay");
        check(cavern.getEarlyReflectionIntensity() == -1.4f, "earlyReflectionIntensity");
        check(cavern.getLateReflectionDelay() == 40000, "lateReflectionDelay");
        check(cavern.getLateReflectionIntensity() == -2.0f, "lateReflectionIntensity");
        check(cavern.getDecayTime() == 2250000, "decayTime");

        String s = cavern.toString();
        check(s.startsWith("Cavern"), "toString starts with name");
        check(s.contains("early reflection delay") && s.contains("decay time"), "toString has parameters");

        // equality is identity (Object.equals), not value-based
        ReverbType other = new ReverbType("Cavern", 10000, -1.4f, 40000, -2.0f, 2250000) {};
        check(!cavern.equals(other), "equal params but distinct instances not equal");
        check(cavern.equals(cavern), "reflexive");
        check(!cavern.equals(null), "not equal null");
        check(cavern.hashCode() == cavern.hashCode(), "hashCode stable");

        // distinct values are preserved independently
        ReverbType closet = new ReverbType("Closet", 600, -7.0f, 2500, -10.0f, 150000) {};
        check("Closet".equals(closet.getName()), "second name");
        check(closet.getDecayTime() == 150000, "second decayTime");
        check(closet.getLateReflectionIntensity() == -10.0f, "second lateReflectionIntensity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
