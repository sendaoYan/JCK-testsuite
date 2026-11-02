/*
 * @test
 * @summary java.lang.classfile.constantpool.LongEntry conformance: behavioral.
 * @run main LongEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class LongEntryTest {
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
        LongEntry e = cpb.longEntry(999L);
        check(e.longValue() == 999L, "longValue");
        check(e.typeKind() == TypeKind.LONG, "typeKind");
        check(e.tag() == 5 && e.width() == 2, "tag/width");
        check(Long.valueOf(999L).equals(e.constantValue()), "constantValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
