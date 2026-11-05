/*
 * @test
 * @summary java.lang.classfile.attribute.LocalVariableTypeInfo conformance: white-box structure + black-box behavior.
 * @run main LocalVariableTypeInfoTest
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

public class LocalVariableTypeInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.LocalVariableTypeInfo");
        check(c != null, "java.lang.classfile.attribute.LocalVariableTypeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.LocalVariableTypeInfo"), "binary name java.lang.classfile.attribute.LocalVariableTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.LocalVariableTypeInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.LocalVariableTypeInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.LocalVariableTypeInfo has no superclass");
        check(declares(c, "startPc"), "declares startPc");
        check(declares(c, "length"), "declares length");
        check(declares(c, "name"), "declares name");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_void), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            cob.localVariableType(2, "x", Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"), s, e).labelBinding(s).labelBinding(e).return_();
        }));
        LocalVariableTypeInfo i = ClassFile.of().parse(b).methods().get(0).code().get().findAttribute(Attributes.localVariableTypeTable()).get().localVariableTypes().get(0);
        check(i.name().stringValue().equals("x"), "name");
        check(i.signature().stringValue().equals("Ljava/util/List<Ljava/lang/String;>;"), "signature");
        check(i.slot() == 2, "slot");
        check(i.startPc() >= 0 && i.length() >= 0, "startPc/length");

    }

    // ---- Part 3: 发射 localVariableType → re-parse 全访问器回读 ----
    static void testEmissionReadback() {
        ClassFile cf = ClassFile.of();
        Signature sig = Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;");
        byte[] b = cf.build(ClassDesc.of("LVTTI1"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariableType(1, "x", sig, s, e);
                    c.aconst_null();
                    c.pop();
                    c.return_();
                }));
        CodeModel code = cf.parse(b).methods().get(0).code().get();
        var lvtt = code.findAttribute(Attributes.localVariableTypeTable());
        check(lvtt.isPresent(), "localVariableType emission produces LVTT");
        check(lvtt.get().localVariableTypes().size() == 1, "1 entry");
        LocalVariableTypeInfo t = lvtt.get().localVariableTypes().get(0);
        check(t.slot() == 1, "slot");
        check(t.name().stringValue().equals("x"), "name");
        check(t.signature().stringValue().equals("Ljava/util/List<Ljava/lang/String;>;"), "signature exact");
        check(t.startPc() == 0 && t.length() == 3, "pc/length covers whole 3-byte body");
        // LocalVariableType 伪指令一致性
        int pseudos = 0;
        for (CodeElement el : code.elementList()) if (el instanceof java.lang.classfile.instruction.LocalVariableType) pseudos++;
        check(pseudos == 1, "1 LocalVariableType pseudo");
        // 泛型映射表（typeParameter）
        byte[] b2 = cf.build(ClassDesc.of("LVTTI2"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariableType(2, "m", Signature.parseFrom("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;"), s, e);
                    c.aconst_null();
                    c.pop();
                    c.return_();
                }));
        LocalVariableTypeInfo t2 = cf.parse(b2).methods().get(0).code().get()
                .findAttribute(Attributes.localVariableTypeTable()).get().localVariableTypes().get(0);
        check(t2.slot() == 2 && t2.name().stringValue().equals("m"), "second var slot/name");
        check(t2.signature().stringValue().equals("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;"), "map signature exact");
        // Utf8Entry 通道重载
        byte[] b3 = cf.build(ClassDesc.of("LVTTI3"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariableType(1, c.constantPool().utf8Entry("k"), c.constantPool().utf8Entry("TK;"), s, e);
                    c.aconst_null();
                    c.pop();
                    c.return_();
                }));
        LocalVariableTypeInfo t3 = cf.parse(b3).methods().get(0).code().get()
                .findAttribute(Attributes.localVariableTypeTable()).get().localVariableTypes().get(0);
        check(t3.name().stringValue().equals("k") && t3.signature().stringValue().equals("TK;"), "Utf8Entry overload readback");
    }

    // ---- Part 4: 选项交叉 + javac 真类 ----
    static void testOptionsAndReal() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("LVTTI4"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariableType(1, "x", Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"), s, e);
                    c.aconst_null();
                    c.pop();
                    c.return_();
                }));
        // DROP_DEBUG parse 侧：剥伪指令保留属性
        CodeModel dropped = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).parse(b).methods().get(0).code().get();
        int pseudos = 0;
        for (CodeElement el : dropped.elementList()) if (el instanceof java.lang.classfile.instruction.LocalVariableType) pseudos++;
        check(pseudos == 0, "DROP_DEBUG strips LocalVariableType pseudo");
        check(dropped.findAttribute(Attributes.localVariableTypeTable()).isPresent(), "LVTT attribute kept");
        // build 侧 DROP_DEBUG：属性不生成
        byte[] b2 = cf.withOptions(ClassFile.DebugElementsOption.DROP_DEBUG).build(ClassDesc.of("LVTTI5"), cb -> cb
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_STATIC, c -> {
                    Label s = c.startLabel(), e = c.endLabel();
                    c.localVariableType(1, "x", Signature.parseFrom("Ljava/util/List<Ljava/lang/String;>;"), s, e);
                    c.aconst_null();
                    c.pop();
                    c.return_();
                }));
        check(cf.parse(b2).methods().get(0).code().get().findAttribute(Attributes.localVariableTypeTable()).isEmpty(), "build DROP_DEBUG omits LVTT");
        // javac 默认不产 LVTT
        byte[] self = LocalVariableTypeInfoTest.class.getResourceAsStream("LocalVariableTypeInfoTest.class").readAllBytes();
        boolean any = false;
        for (MethodModel mm : cf.parse(self).methods())
            if (mm.code().isPresent()) any |= mm.code().get().findAttribute(Attributes.localVariableTypeTable()).isPresent();
        check(!any, "javac without -g emits no LocalVariableTypeTable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEmissionReadback();
        testOptionsAndReal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
