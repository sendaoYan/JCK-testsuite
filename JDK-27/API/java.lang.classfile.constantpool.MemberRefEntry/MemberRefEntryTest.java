/*
 * @test
 * @summary java.lang.classfile.constantpool.MemberRefEntry conformance: behavioral.
 * @run main MemberRefEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class MemberRefEntryTest {
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
        MemberRefEntry e = cpb.methodRefEntry(ClassDesc.of("java.lang.String"), "length", MethodTypeDesc.of(ConstantDescs.CD_int));
        check("java/lang/String".equals(e.owner().asInternalName()), "owner");
        check("length".equals(e.name().stringValue()), "name");
        check("()I".equals(e.type().stringValue()), "type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
