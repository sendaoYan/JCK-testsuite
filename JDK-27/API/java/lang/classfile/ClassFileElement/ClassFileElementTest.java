/*
 * @test
 * @summary java.lang.classfile.ClassFileElement conformance: white-box structure + black-box behavior.
 * @run main ClassFileElementTest
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

public class ClassFileElementTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFileElement");
        check(c != null, "java.lang.classfile.ClassFileElement loads");
        check(c.getName().equals("java.lang.classfile.ClassFileElement"), "binary name java.lang.classfile.ClassFileElement");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFileElement is public");
        check(c.isInterface(), "java.lang.classfile.ClassFileElement is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassFileElement has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ClassFileElement e = ClassFileVersion.of(52, 0);
        check(e != null, "ClassFileVersion is ClassFileElement");
        ClassFileElement f = DeprecatedAttribute.of();
        check(f instanceof ClassFileElement, "DeprecatedAttribute is ClassFileElement");

    }


    // ---- Part 3: 全家族 instanceof ClassFileElement 矩阵（编译期+运行期） ----
    static class CAsup extends CustomAttribute<CAsup> { CAsup(AttributeMapper<CAsup> m) { super(m); } }
    static final AttributeMapper<CAsup> SUPMAP = new AttributeMapper<CAsup>() {
        public String name() { return "SupAttr"; }
        public CAsup readAttribute(AttributedElement e, ClassReader cr, int pos) { return new CAsup(SUPMAP); }
        public void writeAttribute(BufWriter bw, CAsup a) { bw.writeIndex(bw.constantPool().utf8Entry(name())); bw.writeInt(0); }
        public AttributeStability stability() { return AttributeStability.STATELESS; }
    };
    static void testFamilyMatrix() throws Exception {
        // 类级元素
        check(ClassFileVersion.of(52, 0) instanceof ClassFileElement, "ClassFileVersion is ClassFileElement");
        check(Superclass.of(ConstantPoolBuilder.of().classEntry(ClassDesc.of("java.lang.Object"))) instanceof ClassFileElement, "Superclass is CFE");
        check(Interfaces.ofSymbols() instanceof ClassFileElement, "Interfaces is CFE");
        check(DeprecatedAttribute.of() instanceof ClassFileElement, "DeprecatedAttribute is CFE");
        check(SourceFileAttribute.of("X.java") instanceof ClassFileElement, "SourceFileAttribute is CFE");
        check(new CAsup(SUPMAP) instanceof ClassFileElement, "CustomAttribute is CFE");
        // 成员模型（含其元素层）
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.FX"), cb -> cb
                .withField("f", ConstantDescs.CD_int, fb -> fb.withFlags(0).with(ConstantValueAttribute.of(1)))
                .withMethodBody("m", ConstantDescs.MTD_void, ClassFile.ACC_STATIC, c -> c.nop().return_()));
        ClassModel cm = cf.parse(b);
        check(cm instanceof ClassFileElement, "ClassModel is CFE");
        FieldModel fm = cm.fields().get(0);
        MethodModel mm = cm.methods().get(0);
        CodeModel code = mm.code().get();
        check(fm instanceof ClassFileElement, "FieldModel is CFE");
        check(mm instanceof ClassFileElement, "MethodModel is CFE");
        check(code instanceof ClassFileElement, "CodeModel is CFE");
        check(fm.flags() instanceof ClassFileElement, "field AccessFlags is CFE");
        check(mm.flags() instanceof ClassFileElement, "method AccessFlags is CFE");
        check(cm.flags() instanceof ClassFileElement, "class AccessFlags is CFE");
        check(fm.findAttribute(Attributes.constantValue()).get() instanceof ClassFileElement, "ConstantValueAttribute is CFE");
        check(NopInstruction.of() instanceof ClassFileElement, "Instruction is CFE");
        check(LineNumber.of(3) instanceof ClassFileElement, "PseudoInstruction is CFE");
        // elementList 全体 instanceof
        for (ClassElement e : cm.elementList()) check(e instanceof ClassFileElement, "class element is CFE");
        for (FieldElement e : fm.elementList()) check(e instanceof ClassFileElement, "field element is CFE");
        for (MethodElement e : mm.elementList()) check(e instanceof ClassFileElement, "method element is CFE");
        for (CodeElement e : code.elementList()) check(e instanceof ClassFileElement, "code element is CFE");
        // 子接口全 extends ClassFileElement
        check(ClassFileElement.class.isAssignableFrom(ClassElement.class), "ClassElement <- CFE");
        check(ClassFileElement.class.isAssignableFrom(MethodElement.class), "MethodElement <- CFE");
        check(ClassFileElement.class.isAssignableFrom(FieldElement.class), "FieldElement <- CFE");
        check(ClassFileElement.class.isAssignableFrom(CodeElement.class), "CodeElement <- CFE");
        // 负例：非元素类型（Label 实现类实测同时是 LabelTarget→CFE，故仅用纯外部类型做负例）
        check(!ClassFileElement.class.isInstance(Opcode.NOP), "Opcode NOT CFE");
        check(!ClassFileElement.class.isInstance(ClassDesc.of("java.lang.Object")), "ClassDesc NOT CFE");
        check(!ClassFileElement.class.isInstance("str"), "String NOT CFE");
        // 空标记接口：无自身抽象方法
        check(ClassFileElement.class.getDeclaredMethods().length == 0, "ClassFileElement declares no methods");
    }

    // ---- Part 4: build→parse 元素全量经 ClassFileElement 通道再写入（transform 三段） ----
    static void testElementPassThrough() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.PT"), cb -> cb
                .withField("x", ConstantDescs.CD_int, 0)
                .withMethodBody("m", ConstantDescs.MTD_void, 0, c -> c.return_()));
        ClassModel src = cf.parse(b);
        List<ClassFileElement> collected = new ArrayList<>();
        src.forEach(collected::add);                        // CompoundElement 元素以 ClassFileElement 通道收齐
        check(collected.size() == src.elementList().size(), "CFE channel collects all elements");
        byte[] out = cf.transformClass(src, ClassTransform.ACCEPT_ALL);
        ClassModel dst = cf.parse(out);
        check(dst.elementList().size() == src.elementList().size(), "ACCEPT_ALL element count preserved");
        check(cf.verify(dst).isEmpty(), "passthrough verify clean");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFamilyMatrix();
        testElementPassThrough();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
