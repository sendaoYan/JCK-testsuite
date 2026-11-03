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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
