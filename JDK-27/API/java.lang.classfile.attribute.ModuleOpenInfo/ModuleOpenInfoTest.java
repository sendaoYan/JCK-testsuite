/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleOpenInfo conformance: behavioral.
 * @run main ModuleOpenInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ModuleOpenInfoTest {
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
        ModuleOpenInfo i = ModuleOpenInfo.of(PackageDesc.of("p"), 0, ModuleDesc.of("m"));
        check(PackageDesc.of("p").equals(i.openedPackage().asSymbol()), "openedPackage");
        check(i.opensTo().size() == 1, "opensTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
