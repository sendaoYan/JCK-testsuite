/*
 * @test
 * @summary java.lang.classfile.attribute.LocalVariableInfo conformance: white-box structure + black-box behavior.
 * @run main LocalVariableInfoTest
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

public class LocalVariableInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.LocalVariableInfo");
        check(c != null, "java.lang.classfile.attribute.LocalVariableInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.LocalVariableInfo"), "binary name java.lang.classfile.attribute.LocalVariableInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.LocalVariableInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.LocalVariableInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.LocalVariableInfo has no superclass");
        check(declares(c, "startPc"), "declares startPc");
        check(declares(c, "length"), "declares length");
        check(declares(c, "name"), "declares name");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            cob.localVariable(1, "y", ConstantDescs.CD_long, s, e).labelBinding(s).labelBinding(e).return_();
        }));
        LocalVariableInfo i = ClassFile.of().parse(b).methods().get(0).code().get().findAttribute(Attributes.localVariableTable()).get().localVariables().get(0);
        check(i.name().stringValue().equals("y"), "name");
        check(i.type().stringValue().equals("J"), "type");
        check(i.typeSymbol().equals(ConstantDescs.CD_long), "typeSymbol");
        check(i.slot() == 1, "slot");
        check(i.startPc() >= 0 && i.length() >= 0, "startPc/length");

    }

    // ---- Part 3: 发射 → re-parse 全访问器回读（LocalVariableInfo 无公开工厂，经 LVT 属性取得实例）----
    static void testEmissionReadback() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("LVIT1"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(IJ)V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariable(0, "a", ConstantDescs.CD_int, s, e);
                    c.localVariable(1, "b", ConstantDescs.CD_long, s, e);
                    c.return_();
                }));
        CodeModel code = cf.parse(b).methods().get(0).code().get();
        var lvt = code.findAttribute(Attributes.localVariableTable());
        check(lvt.isPresent(), "localVariable emission produces LVT");
        List<LocalVariableInfo> vs = lvt.get().localVariables();
        check(vs.size() == 2, "2 vars: " + vs.size());
        LocalVariableInfo va = vs.get(0), vb = vs.get(1);
        check(va.slot() == 0 && va.name().stringValue().equals("a") && va.type().stringValue().equals("I"), "var a slot/name/type");
        check(va.typeSymbol().descriptorString().equals("I"), "var a typeSymbol");
        check(va.startPc() == 0 && va.length() == 1, "var a startPc=0 length=codeLength(1)");
        check(vb.slot() == 1 && vb.name().stringValue().equals("b") && vb.type().stringValue().equals("J"), "var b long");
        check(vb.typeSymbol().descriptorString().equals("J"), "var b typeSymbol");
        // 引用类型描述符
        byte[] b2 = cf.build(ClassDesc.of("LVIT2"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariable(1, "s", ClassDesc.of("java.lang.String"), s, e);
                    c.return_();
                }));
        LocalVariableInfo vr = cf.parse(b2).methods().get(0).code().get()
                .findAttribute(Attributes.localVariableTable()).get().localVariables().get(0);
        check(vr.type().stringValue().equals("Ljava/lang/String;"), "ref descriptor");
        check(vr.typeSymbol().equals(ClassDesc.of("java.lang.String")), "typeSymbol ClassDesc");
        // Utf8Entry 通道发射（localVariable(int, Utf8Entry, Utf8Entry, Label, Label)）
        byte[] b3 = cf.build(ClassDesc.of("LVIT3"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariable(1, c.constantPool().utf8Entry("u"), c.constantPool().utf8Entry("D"), s, e);
                    c.dconst_0().pop().return_();
                }));
        LocalVariableInfo vu = cf.parse(b3).methods().get(0).code().get()
                .findAttribute(Attributes.localVariableTable()).get().localVariables().get(0);
        check(vu.name().stringValue().equals("u") && vu.type().stringValue().equals("D"), "Utf8Entry overload readback");
        // LocalVariable 伪指令与属性表条目一致
        int pseudos = 0;
        for (CodeElement el : code.elementList()) if (el instanceof java.lang.classfile.instruction.LocalVariable) pseudos++;
        check(pseudos == 2, "2 LocalVariable pseudos");
    }

    // ---- Part 4: 选项交叉 ----
    static void testOptionCross() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("LVIT4"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariable(1, "lv", ConstantDescs.CD_int, s, e).return_();
                }));
        // parse 侧 DROP_DEBUG：剥伪指令、保留属性
        CodeModel dropped = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).parse(b).methods().get(0).code().get();
        int pseudos = 0;
        for (CodeElement el : dropped.elementList()) if (el instanceof java.lang.classfile.instruction.LocalVariable) pseudos++;
        check(pseudos == 0, "DROP_DEBUG strips pseudos");
        check(dropped.findAttribute(Attributes.localVariableTable()).isPresent(), "DROP_DEBUG keeps LVT attribute");
        check(!dropped.findAttribute(Attributes.localVariableTable()).get().localVariables().isEmpty(), "entries still readable");
        // build 侧 DROP_DEBUG：不产 LVT
        byte[] b2 = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).build(ClassDesc.of("LVIT5"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("(I)V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariable(1, "lv", ConstantDescs.CD_int, s, e).return_();
                }));
        check(cf.parse(b2).methods().get(0).code().get().findAttribute(Attributes.localVariableTable()).isEmpty(), "build DROP_DEBUG omits LVT");
    }

    // ---- Part 5: javac 默认（无 -g）不产 LVT；-g:vars 才产 ----
    static void testRealClass() throws Exception {
        byte[] self = LocalVariableInfoTest.class.getResourceAsStream("LocalVariableInfoTest.class").readAllBytes();
        boolean any = false;
        for (MethodModel mm : ClassFile.of().parse(self).methods())
            if (mm.code().isPresent()) any |= mm.code().get().findAttribute(Attributes.localVariableTable()).isPresent();
        check(!any, "javac without -g emits no LocalVariableTable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmissionReadback();
        testOptionCross();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
