/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleHashesAttribute conformance: behavioral.
 * @run main ModuleHashesAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ModuleHashesAttributeTest {
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
        ModuleHashesAttribute a = ModuleHashesAttribute.of("SHA-256", ModuleHashInfo.of(ModuleDesc.of("m"), new byte[]{1, 2}));
        check("SHA-256".equals(a.algorithm().stringValue()), "algorithm");
        check(a.hashes().size() == 1, "hashes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
