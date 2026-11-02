/*
 * @test
 * @summary java.lang.classfile.constantpool.ModuleEntry conformance: behavioral.
 * @run main ModuleEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ModuleEntryTest {
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
        ModuleEntry e = cpb.moduleEntry(ModuleDesc.of("java.base"));
        check(ModuleDesc.of("java.base").equals(e.asSymbol()), "asSymbol");
        check(e.matches(ModuleDesc.of("java.base")), "matches");
        check("java.base".equals(e.name().stringValue()), "name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
