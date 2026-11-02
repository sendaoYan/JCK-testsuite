/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleAttribute.ModuleAttributeBuilder conformance: behavioral.
 * @run main ModuleAttributeBuilderTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ModuleAttributeBuilderTest {
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
        ModuleAttribute a = ModuleAttribute.of(ModuleDesc.of("m"), mb -> { mb.moduleVersion("1.0"); mb.moduleFlags(AccessFlag.OPEN); });
        check("1.0".equals(a.moduleVersion().get().stringValue()), "moduleVersion");
        check(a.moduleFlags().contains(AccessFlag.OPEN), "moduleFlags");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
