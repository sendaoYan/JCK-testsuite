/*
 * @test
 * @summary java.lang.classfile.constantpool.DoubleEntry conformance: behavioral.
 * @run main DoubleEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class DoubleEntryTest {
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
        DoubleEntry e = cpb.doubleEntry(3.5);
        check(e.doubleValue() == 3.5, "doubleValue");
        check(e.typeKind() == TypeKind.DOUBLE, "typeKind");
        check(e.tag() == 6 && e.width() == 2, "tag/width");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
