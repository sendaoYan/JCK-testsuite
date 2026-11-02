/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleHashInfo conformance: behavioral.
 * @run main ModuleHashInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ModuleHashInfoTest {
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
        ModuleHashInfo i = ModuleHashInfo.of(ModuleDesc.of("m"), new byte[]{1, 2});
        check(ModuleDesc.of("m").equals(i.moduleName().asSymbol()), "moduleName");
        check(java.util.Arrays.equals(new byte[]{1, 2}, i.hash()), "hash");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
