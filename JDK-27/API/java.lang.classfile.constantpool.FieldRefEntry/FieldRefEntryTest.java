/*
 * @test
 * @summary java.lang.classfile.constantpool.FieldRefEntry conformance: behavioral.
 * @run main FieldRefEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class FieldRefEntryTest {
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
        FieldRefEntry e = cpb.fieldRefEntry(ClassDesc.of("java.lang.Integer"), "MAX_VALUE", ConstantDescs.CD_int);
        check("I".equals(e.typeSymbol().descriptorString()), "typeSymbol");
        check("MAX_VALUE".equals(e.name().stringValue()), "name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
