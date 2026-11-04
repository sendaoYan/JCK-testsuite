/*
 * @test
 * @summary java.lang.classfile.ClassModel conformance: white-box structure + black-box behavior.
 * @run main ClassModelTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.nio.file.*;

public class ClassModelTest {
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
    static final ClassReader[] cap = new ClassReader[1];
    static final BufWriter[] cap2 = new BufWriter[1];
    static final BranchInstruction[] br = new BranchInstruction[1];
    static final SwitchCase[] sc = new SwitchCase[1];
    static final TableSwitchInstruction[] ts = new TableSwitchInstruction[1];
    static final LookupSwitchInstruction[] ls = new LookupSwitchInstruction[1];
    static final ExceptionCatch[] ec = new ExceptionCatch[1];
    static final TypeAnnotation.TypeArgumentTarget[] ta = new TypeAnnotation.TypeArgumentTarget[1];
    static final TypeAnnotation.LocalVarTarget[] lvt = new TypeAnnotation.LocalVarTarget[1];
    static final TypeAnnotation.LocalVarTargetInfo[] lvi = new TypeAnnotation.LocalVarTargetInfo[1];
    static final StackMapFrameInfo.UninitializedVerificationTypeInfo[] uv = new StackMapFrameInfo.UninitializedVerificationTypeInfo[1];
    static final TypeAnnotation.OffsetTarget[] ot = new TypeAnnotation.OffsetTarget[1];

    // ---- custom attribute support for black-box above ----
    static class CA extends CustomAttribute<CA> { CA(AttributeMapper<CA> m) { super(m); } }
    static class CA2 extends CustomAttribute<CA2> { CA2(AttributeMapper<CA2> m) { super(m); } }
    static class CA3 extends CustomAttribute<CA3> { CA3(AttributeMapper<CA3> m) { super(m); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.ClassModel");
        check(c != null, "java.lang.classfile.ClassModel loads");
        check(c.getName().equals("java.lang.classfile.ClassModel"), "binary name java.lang.classfile.ClassModel");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassModel is public");
        check(c.isInterface(), "java.lang.classfile.ClassModel is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassModel has no superclass");
        check(declares(c, "constantPool"), "declares constantPool");
        check(declares(c, "flags"), "declares flags");
        check(declares(c, "thisClass"), "declares thisClass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.Sample"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")).withInterfaceSymbols(ClassDesc.of("java.lang.Runnable")).withField("x", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE).withMethodBody("add", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int, ConstantDescs.CD_int), ClassFile.ACC_PUBLIC, cob -> cob.iload(0).iload(1).iadd().ireturn()));
        ClassModel cm = cf.parse(b);
        check("pkg/Sample".equals(cm.thisClass().asInternalName()), "thisClass");
        check(cm.majorVersion() == 71 && cm.minorVersion() == 0, "version");
        check(cm.superclass().get().asInternalName().equals("java/lang/Object"), "superclass");
        check(cm.interfaces().size() == 1 && cm.interfaces().get(0).asInternalName().equals("java/lang/Runnable"), "interfaces");
        check(!cm.isModuleInfo(), "not module-info");
        check(cm.flags().has(AccessFlag.PUBLIC), "flags PUBLIC");
        check(cm.constantPool().size() > 0, "constantPool nonempty");
        check(cm.fields().size() == 1 && cm.methods().size() == 1, "fields+methods");
        check(cm.fields().get(0).fieldName().stringValue().equals("x"), "field name");
        check(cm.methods().get(0).methodName().stringValue().equals("add"), "method name");
        check(cm.elementList().size() >= 3, "elementList");
        check(cm.toDebugString().contains("pkg"), "toDebugString");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
