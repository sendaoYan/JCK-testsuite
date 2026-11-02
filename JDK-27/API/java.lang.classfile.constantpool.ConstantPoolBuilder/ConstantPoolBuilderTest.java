/*
 * @test
 * @summary java.lang.classfile.constantpool.ConstantPoolBuilder conformance: behavioral.
 * @run main ConstantPoolBuilderTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ConstantPoolBuilderTest {
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
        Utf8Entry u = cpb.utf8Entry("hello");
        check("hello".equals(u.stringValue()), "utf8Entry");
        ClassEntry ce = cpb.classEntry(ClassDesc.of("java.lang.String"));
        check("java/lang/String".equals(ce.asInternalName()), "classEntry");
        check(ce.matches(ClassDesc.of("java.lang.String")), "classEntry matches");
        IntegerEntry ie = cpb.intEntry(42);
        check(ie.intValue() == 42 && ie.tag() == 3 && ie.width() == 1, "intEntry");
        LongEntry le = cpb.longEntry(123L);
        check(le.longValue() == 123L && le.width() == 2, "longEntry");
        FloatEntry fe = cpb.floatEntry(1.5f);
        check(Float.floatToIntBits(fe.floatValue()) == Float.floatToIntBits(1.5f), "floatEntry");
        DoubleEntry de = cpb.doubleEntry(2.5);
        check(de.doubleValue() == 2.5, "doubleEntry");
        StringEntry se = cpb.stringEntry("hi");
        check("hi".equals(se.stringValue()) && se.equalsString("hi"), "stringEntry");
        NameAndTypeEntry nat = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_int));
        check("m".equals(nat.name().stringValue()) && "()I".equals(nat.type().stringValue()), "nameAndTypeEntry");
        MethodRefEntry mr = cpb.methodRefEntry(ClassDesc.of("java.lang.String"), "length", MethodTypeDesc.of(ConstantDescs.CD_int));
        check("java/lang/String".equals(mr.owner().asInternalName()), "methodRef owner");
        check("length".equals(mr.name().stringValue()), "methodRef name");
        check("()I".equals(mr.typeSymbol().descriptorString()), "methodRef typeSymbol");
        FieldRefEntry fr = cpb.fieldRefEntry(ClassDesc.of("java.lang.Integer"), "MAX_VALUE", ConstantDescs.CD_int);
        check("I".equals(fr.typeSymbol().descriptorString()), "fieldRef typeSymbol");
        InterfaceMethodRefEntry imr = cpb.interfaceMethodRefEntry(ClassDesc.of("java.lang.Runnable"), "run", MethodTypeDesc.of(ConstantDescs.CD_void));
        check("run".equals(imr.name().stringValue()), "interfaceMethodRef name");
        MethodTypeEntry mte = cpb.methodTypeEntry(MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_int));
        check(mte.matches(MethodTypeDesc.of(ConstantDescs.CD_void, ConstantDescs.CD_int)), "methodType matches");
        PackageEntry pe = cpb.packageEntry(PackageDesc.of("java.lang"));
        check(PackageDesc.of("java.lang").equals(pe.asSymbol()), "packageEntry");
        ModuleEntry me = cpb.moduleEntry(ModuleDesc.of("java.base"));
        check(ModuleDesc.of("java.base").equals(me.asSymbol()), "moduleEntry");
        LoadableConstantEntry lce = cpb.loadableConstantEntry(ClassDesc.of("java.lang.String"));
        check(lce.typeKind() == TypeKind.REFERENCE, "loadableConstantEntry typeKind");
        ConstantValueEntry cve = cpb.constantValueEntry(Integer.valueOf(5));
        check(Integer.valueOf(5).equals(cve.constantValue()), "constantValueEntry");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
