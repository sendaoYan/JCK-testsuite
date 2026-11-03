/*
 * @test
 * @summary javax.sound.sampled.AudioPermission conformance: white-box structure + black-box behavior.
 * @run main AudioPermissionTest
 */

import java.lang.reflect.Modifier;
import javax.sound.sampled.AudioPermission;

public class AudioPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.sampled.AudioPermission");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.BasicPermission"), "superclass BasicPermission");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AudioPermission play = new AudioPermission("play");
        AudioPermission record = new AudioPermission("record");

        check("play".equals(play.getName()), "name play");
        check("record".equals(record.getName()), "name record");
        check("".equals(play.getActions()), "actions empty");

        // name round-trip through the two-arg constructor
        AudioPermission play2 = new AudioPermission("play", null);
        check("play".equals(play2.getName()), "two-arg name play");

        // equals / hashCode
        check(play.equals(new AudioPermission("play")), "equal by name");
        check(!play.equals(record), "different name not equal");
        check(play.hashCode() == new AudioPermission("play").hashCode(), "hashCode by name");
        check(!play.equals(null), "not equal null");

        // implies uses BasicPermission wildcard semantics
        AudioPermission all = new AudioPermission("*");
        check(all.implies(play), "* implies play");
        check(all.implies(record), "* implies record");
        check(play.implies(play), "play implies play");
        check(!play.implies(record), "play does not imply record");
        check(new AudioPermission("play.*").implies(new AudioPermission("play.record")), "play.* implies play.record");
        check(!new AudioPermission("play.*").implies(new AudioPermission("play")), "play.* does not imply play");

        // toString carries the name
        check(play.toString().contains("play"), "toString has name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
