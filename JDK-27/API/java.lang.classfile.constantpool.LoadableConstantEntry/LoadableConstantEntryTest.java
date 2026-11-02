/*
 * @test
 * @summary java.lang.classfile.constantpool.LoadableConstantEntry conformance: behavioral.
 * @run main LoadableConstantEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class LoadableConstantEntryTest {
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
        LoadableConstantEntry e = cpb.loadableConstantEntry(ClassDesc.of("java.lang.String"));
        check(e.typeKind() == TypeKind.REFERENCE, "typeKind");
        check(ClassDesc.of("java.lang.String").equals(e.constantValue()), "constantValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
