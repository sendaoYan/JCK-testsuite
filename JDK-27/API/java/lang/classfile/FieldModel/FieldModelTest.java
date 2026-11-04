/*
 * @test
 * @summary java.lang.classfile.FieldModel conformance: white-box structure + black-box behavior.
 * @run main FieldModelTest
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

public class FieldModelTest {
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
        Class<?> c = Class.forName("java.lang.classfile.FieldModel");
        check(c != null, "java.lang.classfile.FieldModel loads");
        check(c.getName().equals("java.lang.classfile.FieldModel"), "binary name java.lang.classfile.FieldModel");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.FieldModel is public");
        check(c.isInterface(), "java.lang.classfile.FieldModel is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.FieldModel has no superclass");
        check(declares(c, "flags"), "declares flags");
        check(declares(c, "parent"), "declares parent");
        check(declares(c, "fieldName"), "declares fieldName");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withField("count", ConstantDescs.CD_long, ClassFile.ACC_PRIVATE | ClassFile.ACC_STATIC | ClassFile.ACC_FINAL));
        ClassModel cm = ClassFile.of().parse(b);
        FieldModel f = cm.fields().get(0);
        check(f.fieldName().stringValue().equals("count"), "name");
        check(f.fieldType().stringValue().equals("J"), "descriptor");
        check(f.fieldTypeSymbol().equals(ConstantDescs.CD_long), "type symbol");
        check(f.flags().has(AccessFlag.PRIVATE) && f.flags().has(AccessFlag.STATIC) && f.flags().has(AccessFlag.FINAL), "flags");
        check(f.parent().isPresent(), "parent");
        check(f.elementList().size() >= 1, "elements");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
