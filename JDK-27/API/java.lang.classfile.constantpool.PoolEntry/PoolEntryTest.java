/*
 * @test
 * @summary java.lang.classfile.constantpool.PoolEntry conformance: behavioral.
 * @run main PoolEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class PoolEntryTest {
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
        PoolEntry e = cpb.intEntry(5);
        check(e.index() > 0, "index");
        check(e.tag() == 3 && e.width() == 1, "tag/width");
        check(e.constantPool() != null, "constantPool");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
