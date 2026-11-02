/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleAttribute conformance: behavioral.
 * @run main ModuleAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ModuleAttributeTest {
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
        ModuleAttribute a = ModuleAttribute.of(ModuleDesc.of("m"), mb -> mb.moduleVersion("1.0"));
        check(ModuleDesc.of("m").equals(a.moduleName().asSymbol()), "moduleName");
        check("1.0".equals(a.moduleVersion().get().stringValue()), "moduleVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
