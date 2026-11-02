/*
 * @test
 * @summary java.lang.classfile.constantpool.InterfaceMethodRefEntry conformance: behavioral.
 * @run main InterfaceMethodRefEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class InterfaceMethodRefEntryTest {
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
        InterfaceMethodRefEntry e = cpb.interfaceMethodRefEntry(ClassDesc.of("java.lang.Runnable"), "run", MethodTypeDesc.of(ConstantDescs.CD_void));
        check("()V".equals(e.typeSymbol().descriptorString()), "typeSymbol");
        check("run".equals(e.name().stringValue()), "name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
