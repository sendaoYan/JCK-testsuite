/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleProvideInfo conformance: behavioral.
 * @run main ModuleProvideInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ModuleProvideInfoTest {
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
        ModuleProvideInfo i = ModuleProvideInfo.of(ClassDesc.of("pkg.S"), ClassDesc.of("pkg.I"));
        check("pkg/S".equals(i.provides().asInternalName()), "provides");
        check(i.providesWith().size() == 1, "providesWith");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
