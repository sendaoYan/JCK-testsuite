/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.LocalVarTarget conformance: white-box structure + black-box behavior.
 * @run main LocalVarTargetTest
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

public class LocalVarTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$LocalVarTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.LocalVarTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$LocalVarTarget"), "binary name java.lang.classfile.TypeAnnotation$LocalVarTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.LocalVarTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.LocalVarTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.LocalVarTarget has no superclass");
        check(declares(c, "table"), "declares table");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            java.util.List<TypeAnnotation.LocalVarTargetInfo> tbl = java.util.List.of(TypeAnnotation.LocalVarTargetInfo.of(s, e, 0));
            lvt[0] = TypeAnnotation.TargetInfo.ofLocalVariable(tbl);
            cob.labelBinding(s).labelBinding(e).return_();
        }));
        TypeAnnotation.LocalVarTarget t = lvt[0];
        check(t != null, "ofLocalVariable created");
        check(t.table().size() == 1, "table size 1");
        check(t.table().get(0).index() == 0, "index 0");
        check(t.targetType() == TypeAnnotation.TargetType.LOCAL_VARIABLE, "targetType");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
