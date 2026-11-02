/*
 * @test
 * @summary java.lang.classfile.constantpool.ClassEntry conformance: behavioral.
 * @run main ClassEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ClassEntryTest {
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
        ClassEntry ce = cpb.classEntry(ClassDesc.of("java.lang.String"));
        check("java/lang/String".equals(ce.asInternalName()), "asInternalName");
        check(ClassDesc.of("java.lang.String").equals(ce.asSymbol()), "asSymbol");
        check(ce.matches(ClassDesc.of("java.lang.String")), "matches");
        check("java/lang/String".equals(ce.name().stringValue()), "name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
