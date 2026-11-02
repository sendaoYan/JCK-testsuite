/*
 * @test
 * @summary java.lang.classfile.constantpool.NameAndTypeEntry conformance: behavioral.
 * @run main NameAndTypeEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class NameAndTypeEntryTest {
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
        NameAndTypeEntry e = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_int));
        check("m".equals(e.name().stringValue()), "name");
        check("()I".equals(e.type().stringValue()), "type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
