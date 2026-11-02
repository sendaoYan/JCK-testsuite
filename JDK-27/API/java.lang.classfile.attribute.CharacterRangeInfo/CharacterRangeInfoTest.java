/*
 * @test
 * @summary java.lang.classfile.attribute.CharacterRangeInfo conformance: behavioral.
 * @run main CharacterRangeInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class CharacterRangeInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        CharacterRangeInfo i = CharacterRangeInfo.of(1, 2, 3, 4, 5);
        check(i.startPc() == 1 && i.endPc() == 2 && i.characterRangeStart() == 3 && i.characterRangeEnd() == 4 && i.flags() == 5, "fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
