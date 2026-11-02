/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleRequireInfo conformance: behavioral.
 * @run main ModuleRequireInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ModuleRequireInfoTest {
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
        ModuleRequireInfo i = ModuleRequireInfo.of(ModuleDesc.of("m"), 0, "1.0");
        check(ModuleDesc.of("m").equals(i.requires().asSymbol()), "requires");
        check("1.0".equals(i.requiresVersion().get().stringValue()), "requiresVersion");
        check(i.requiresFlagsMask() == 0, "requiresFlagsMask");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
