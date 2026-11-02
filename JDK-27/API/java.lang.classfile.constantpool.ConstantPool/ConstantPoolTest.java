/*
 * @test
 * @summary java.lang.classfile.constantpool.ConstantPool conformance: behavioral.
 * @run main ConstantPoolTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ConstantPoolTest {
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
        Utf8Entry u = cpb.utf8Entry("x");
        IntegerEntry ie = cpb.intEntry(5);
        check(cpb.size() == 3, "size");
        check(cpb.entryByIndex(1) == u, "entryByIndex(1)");
        check(cpb.entryByIndex(2) == ie, "entryByIndex(2)");
        check(cpb.entryByIndex(1, Utf8Entry.class) == u, "entryByIndex typed");
        boolean sawU = false;
        for (PoolEntry e : cpb) if (e == u) sawU = true;
        check(sawU, "iterator contains utf8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
