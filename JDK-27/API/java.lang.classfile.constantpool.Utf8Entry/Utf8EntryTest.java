/*
 * @test
 * @summary java.lang.classfile.constantpool.Utf8Entry conformance: behavioral.
 * @run main Utf8EntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class Utf8EntryTest {
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
        Utf8Entry u = cpb.utf8Entry("abc");
        check("abc".equals(u.stringValue()), "stringValue");
        check(u.equalsString("abc"), "equalsString");
        check(u.length() == 3 && u.charAt(0) == 97, "CharSequence");
        Utf8Entry fd = cpb.utf8Entry("Ljava/lang/String;");
        check(fd.isFieldType(ClassDesc.of("java.lang.String")), "isFieldType");
        check(cpb.utf8Entry("I").isFieldType(ConstantDescs.CD_int), "isFieldType primitive");
        check(cpb.utf8Entry("()V").isMethodType(MethodTypeDesc.of(ConstantDescs.CD_void)), "isMethodType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
