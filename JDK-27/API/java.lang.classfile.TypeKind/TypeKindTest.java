/*
 * @test
 * @summary java.lang.classfile.TypeKind conformance: behavioral.
 * @run main TypeKindTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeKindTest {
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
        check(TypeKind.class.isEnum(), "TypeKind is an enum");
        check(TypeKind.values().length == 10, "10 constants");
        check(TypeKind.INT.slotSize() == 1, "INT slotSize");
        check(TypeKind.LONG.slotSize() == 2, "LONG slotSize");
        check(TypeKind.DOUBLE.slotSize() == 2, "DOUBLE slotSize");
        check(TypeKind.VOID.slotSize() == 0, "VOID slotSize");
        check(TypeKind.INT.newarrayCode() == 10, "INT newarrayCode");
        check(TypeKind.BOOLEAN.newarrayCode() == 4, "BOOLEAN newarrayCode");
        check(TypeKind.VOID.newarrayCode() == -1, "VOID newarrayCode");
        check(TypeKind.fromNewarrayCode(10) == TypeKind.INT, "fromNewarrayCode INT");
        check(TypeKind.fromNewarrayCode(4) == TypeKind.BOOLEAN, "fromNewarrayCode BOOLEAN");
        check(TypeKind.fromDescriptor("J") == TypeKind.LONG, "fromDescriptor J");
        check(TypeKind.fromDescriptor("[I") == TypeKind.REFERENCE, "fromDescriptor array");
        check(TypeKind.INT.upperBound().equals(ConstantDescs.CD_int), "INT upperBound");
        check(TypeKind.INT.asLoadable() == TypeKind.INT, "asLoadable INT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
