/*
 * @test
 * @summary javax.sound.midi.VoiceStatus conformance: white-box structure + black-box behavior.
 * @run main VoiceStatusTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.VoiceStatus;

public class VoiceStatusTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.VoiceStatus");
        check(!c.isInterface(), "is not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "is not abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getConstructors().length == 1 && c.getConstructors()[0].getParameterCount() == 0, "public no-arg constructor");
        String[] fieldNames = {"active", "channel", "bank", "program", "note", "volume"};
        java.util.Set<String> fs = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getFields()) fs.add(f.getName());
        for (String n : fieldNames) check(fs.contains(n), "public field " + n);
    }

    static void testBlackBox() throws Exception {
        VoiceStatus v = new VoiceStatus();
        check(!v.active, "active default false");
        check(v.channel == 0, "channel default 0");
        check(v.bank == 0, "bank default 0");
        check(v.program == 0, "program default 0");
        check(v.note == 0, "note default 0");
        check(v.volume == 0, "volume default 0");
        v.active = true; v.channel = 9; v.bank = 128; v.program = 47; v.note = 60; v.volume = 127;
        check(v.active && v.channel == 9 && v.bank == 128 && v.program == 47 && v.note == 60 && v.volume == 127, "field round-trip");
        VoiceStatus w = new VoiceStatus();
        check(!w.active && w.note == 0, "fresh instance defaults isolated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
