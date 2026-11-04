/*
 * @test
 * @summary com.sun.media.sound.ModelPatch conformance: Patch subclass with percussion flag.
 * @run main ModelPatchTest
 */

import com.sun.media.sound.ModelPatch;
import javax.sound.midi.Patch;

public class ModelPatchTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        ModelPatch mp = new ModelPatch(0, 1, true);
        check(mp instanceof Patch, "extends Patch");
        check(mp.getBank() == 0, "getBank");
        check(mp.getProgram() == 1, "getProgram");
        check(mp.isPercussion(), "isPercussion");

        ModelPatch mp2 = new ModelPatch(2, 3);
        check(mp2.getBank() == 2, "two-arg getBank");
        check(mp2.getProgram() == 3, "two-arg getProgram");
        check(!mp2.isPercussion(), "two-arg isPercussion false");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
