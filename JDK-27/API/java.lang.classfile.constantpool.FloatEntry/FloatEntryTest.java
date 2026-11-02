/*
 * @test
 * @summary java.lang.classfile.constantpool.FloatEntry conformance: behavioral.
 * @run main FloatEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class FloatEntryTest {
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
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        FloatEntry e = cpb.floatEntry(2.5f);
        check(Float.floatToIntBits(e.floatValue()) == Float.floatToIntBits(2.5f), "floatValue");
        check(e.typeKind() == TypeKind.FLOAT, "typeKind");
        check(e.tag() == 4 && e.width() == 1, "tag/width");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
