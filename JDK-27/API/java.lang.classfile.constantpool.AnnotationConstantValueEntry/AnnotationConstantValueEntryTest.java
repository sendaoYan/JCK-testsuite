/*
 * @test
 * @summary java.lang.classfile.constantpool.AnnotationConstantValueEntry conformance: behavioral.
 * @run main AnnotationConstantValueEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class AnnotationConstantValueEntryTest {
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
        AnnotationConstantValueEntry e = cpb.intEntry(7);
        check(Integer.valueOf(7).equals(e.constantValue()), "constantValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
