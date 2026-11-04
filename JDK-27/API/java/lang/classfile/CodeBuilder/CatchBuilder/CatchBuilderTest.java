/*
 * @test
 * @summary java.lang.classfile.CodeBuilder.CatchBuilder conformance: white-box structure + black-box behavior.
 * @run main CatchBuilderTest
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

public class CatchBuilderTest {
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
        Class<?> c = Class.forName("java.lang.classfile.CodeBuilder$CatchBuilder");
        check(c != null, "java.lang.classfile.CodeBuilder.CatchBuilder loads");
        check(c.getName().equals("java.lang.classfile.CodeBuilder$CatchBuilder"), "binary name java.lang.classfile.CodeBuilder$CatchBuilder");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.CodeBuilder.CatchBuilder is public");
        check(c.isInterface(), "java.lang.classfile.CodeBuilder.CatchBuilder is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.CodeBuilder.CatchBuilder has no superclass");
        check(declares(c, "catching"), "declares catching");
        check(declares(c, "catchingMulti"), "declares catchingMulti");
        check(declares(c, "catchingAll"), "declares catchingAll");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.trying(blk -> blk.return_(), cat -> cat.catching(ClassDesc.of("java.lang.Exception"), hb -> hb.athrow())).return_()));
        ClassModel cm = ClassFile.of().parse(b);
        CodeModel code = cm.methods().get(0).code().get();
        check(code.exceptionHandlers().size() == 1, "one exception handler");
        check(code.exceptionHandlers().get(0).catchType().get().asSymbol().equals(ClassDesc.of("java.lang.Exception")), "handler catch type");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
