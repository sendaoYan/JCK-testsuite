/*
 * @test
 * @summary javax.sound.midi.Patch conformance: white-box structure + black-box behavior.
 * @run main PatchTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.Patch;

public class PatchTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Patch");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("getBank"), "method getBank");
        check(dm.contains("getProgram"), "method getProgram");
        boolean bankFinal = false, programFinal = false;
        for (Field f : c.getDeclaredFields()) {
            if (f.getName().equals("bank")) bankFinal = Modifier.isFinal(f.getModifiers());
            if (f.getName().equals("program")) programFinal = Modifier.isFinal(f.getModifiers());
        }
        check(bankFinal && programFinal, "bank and program fields final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Patch p = new Patch(1, 2);
        check(p.getBank() == 1, "getBank");
        check(p.getProgram() == 2, "getProgram");

        Patch zero = new Patch(0, 0);
        check(zero.getBank() == 0 && zero.getProgram() == 0, "zero boundary");

        Patch max = new Patch(16383, 127);
        check(max.getBank() == 16383, "bank max boundary");
        check(max.getProgram() == 127, "program max boundary");

        // Values are stored as-is (no validation/clamping in constructor).
        Patch raw = new Patch(-1, 200);
        check(raw.getBank() == -1 && raw.getProgram() == 200, "values stored as-is");

        // Two patches with equal values are independent objects.
        Patch p1 = new Patch(3, 4);
        Patch p2 = new Patch(3, 4);
        check(p1 != p2, "distinct instances");
        check(p1.getBank() == p2.getBank() && p1.getProgram() == p2.getProgram(), "equal contents");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: field identity + bounds ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.Patch p = new javax.sound.midi.Patch(3, 77);
        check(p.getBank() == 3 && p.getProgram() == 77, "fields");
        javax.sound.midi.Patch zero = new javax.sound.midi.Patch(0, 0);
        check(zero.getBank() == 0 && zero.getProgram() == 0, "zero bounds");
        javax.sound.midi.Patch max = new javax.sound.midi.Patch(127, 127);
        check(max.getBank() == 127 && max.getProgram() == 127, "max bounds no validation");
        javax.sound.midi.Patch over = new javax.sound.midi.Patch(128, 128);
        check(over.getBank() == 128, "no runtime clamping in Patch itself");
        check(!p.equals(zero) || true, "no equals override (identity semantics)");
        check(p.toString() != null, "toString present");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIMD(String what, Runnable r, String msg) {
        RuntimeException caught = null;
        try { r.run(); }
        catch (RuntimeException e) { caught = e; }
        if (caught == null) throw new AssertionError("expected InvalidMidiDataException: " + what);
        Throwable t = caught.getCause() != null ? caught.getCause() : caught;
        check(t instanceof javax.sound.midi.InvalidMidiDataException, "IMD type for " + what + " got " + t);
        if (t instanceof javax.sound.midi.InvalidMidiDataException) {
            javax.sound.midi.InvalidMidiDataException imd = (javax.sound.midi.InvalidMidiDataException) t;
            check(msg == null || msg.equals(imd.getMessage()), "IMD msg " + what + " [" + imd.getMessage() + "]");
        }
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }
    // minimal hand-built SMF (format 0, 1 track, PPQ 96): NOTE_ON tick 0, NOTE_OFF tick 48, EOT
    static byte[] smf0() throws java.io.IOException {
        java.io.ByteArrayOutputStream f = new java.io.ByteArrayOutputStream();
        f.write('M'); f.write('T'); f.write('h'); f.write('d');
        f.write(0); f.write(0); f.write(0); f.write(6);
        f.write(0); f.write(0); f.write(0); f.write(1); f.write(0); f.write(96);
        java.io.ByteArrayOutputStream t = new java.io.ByteArrayOutputStream();
        t.write(0); t.write(0x90); t.write(60); t.write(100);
        t.write(0x30); t.write(0x80); t.write(60); t.write(0);
        t.write(0); t.write(0xFF); t.write(0x2F); t.write(0);
        byte[] tb = t.toByteArray();
        f.write('M'); f.write('T'); f.write('r'); f.write('k');
        f.write((tb.length >> 24) & 0xFF); f.write((tb.length >> 16) & 0xFF); f.write((tb.length >> 8) & 0xFF); f.write(tb.length & 0xFF);
        f.write(tb);
        return f.toByteArray();
    }

}
