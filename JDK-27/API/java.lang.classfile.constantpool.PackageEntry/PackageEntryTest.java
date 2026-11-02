/*
 * @test
 * @summary java.lang.classfile.constantpool.PackageEntry conformance: behavioral.
 * @run main PackageEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class PackageEntryTest {
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
        PackageEntry e = cpb.packageEntry(PackageDesc.of("java.lang"));
        check(PackageDesc.of("java.lang").equals(e.asSymbol()), "asSymbol");
        check(e.matches(PackageDesc.of("java.lang")), "matches");
        check("java/lang".equals(e.name().stringValue()), "name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
