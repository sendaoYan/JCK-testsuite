/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleExportInfo conformance: behavioral.
 * @run main ModuleExportInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ModuleExportInfoTest {
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
        ModuleExportInfo i = ModuleExportInfo.of(PackageDesc.of("p"), 0, ModuleDesc.of("m"));
        check(PackageDesc.of("p").equals(i.exportedPackage().asSymbol()), "exportedPackage");
        check(i.exportsTo().size() == 1, "exportsTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
