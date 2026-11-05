/*
 * @test
 * @summary java.lang.classfile.attribute.BootstrapMethodsAttribute conformance: white-box structure + black-box behavior.
 * @run main BootstrapMethodsAttributeTest
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

public class BootstrapMethodsAttributeTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.BootstrapMethodsAttribute");
        check(c != null, "java.lang.classfile.attribute.BootstrapMethodsAttribute loads");
        check(c.getName().equals("java.lang.classfile.attribute.BootstrapMethodsAttribute"), "binary name java.lang.classfile.attribute.BootstrapMethodsAttribute");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.BootstrapMethodsAttribute is public");
        check(c.isInterface(), "java.lang.classfile.attribute.BootstrapMethodsAttribute is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.BootstrapMethodsAttribute has no superclass");
        check(declares(c, "bootstrapMethods"), "declares bootstrapMethods");
        check(declares(c, "bootstrapMethodsSize"), "declares bootstrapMethodsSize");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> {
            ConstantPoolBuilder cpb = cb.constantPool();
            BootstrapMethodEntry bsm = cpb.bsmEntry(MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)), java.util.List.of());
            NameAndTypeEntry nat = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_int));
            InvokeDynamicEntry ide = cpb.invokeDynamicEntry(bsm, nat);
            cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.invokedynamic(ide).ireturn());
        });
        BootstrapMethodsAttribute a = ClassFile.of().parse(b).findAttribute(Attributes.bootstrapMethods()).get();
        check(a.bootstrapMethodsSize() == 1, "bootstrapMethodsSize");
        check(a.bootstrapMethods().size() == 1, "bootstrapMethods size");
        check(a.bootstrapMethods().get(0).bootstrapMethod().reference().name().stringValue().equals("valueOf"), "bsm ref name");

    }

    // ---- Part 3: build 注入 → re-parse 逐项回读 ----
    static void testBuildMatrix() {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("BSMT1"), cb -> {
            ConstantPoolBuilder cpb = cb.constantPool();
            BootstrapMethodEntry bsm0 = cpb.bsmEntry(
                    MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Integer"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Integer"), ConstantDescs.CD_int)),
                    java.util.List.of());
            BootstrapMethodEntry bsm1 = cpb.bsmEntry(
                    MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, ClassDesc.of("java.lang.Long"), "valueOf", MethodTypeDesc.of(ClassDesc.of("java.lang.Long"), ConstantDescs.CD_long)),
                    java.util.List.of("arg0", 7));
            NameAndTypeEntry nat = cpb.nameAndTypeEntry("m", MethodTypeDesc.of(ConstantDescs.CD_int));
            NameAndTypeEntry nat2 = cpb.nameAndTypeEntry("m2", MethodTypeDesc.of(ConstantDescs.CD_long));
            InvokeDynamicEntry ide = cpb.invokeDynamicEntry(bsm0, nat);
            InvokeDynamicEntry ide2 = cpb.invokeDynamicEntry(bsm1, nat2);
            cb.withMethodBody("m", MethodTypeDesc.of(ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.invokedynamic(ide).ireturn());
            cb.withMethodBody("m2", MethodTypeDesc.of(ConstantDescs.CD_long), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.invokedynamic(ide2).lreturn());
        });
        ClassModel parsed = cf.parse(b);
        BootstrapMethodsAttribute a = parsed.findAttribute(Attributes.bootstrapMethods()).get();
        check(a.bootstrapMethodsSize() == 2, "2 bsm entries");
        check(a.bootstrapMethods().size() == a.bootstrapMethodsSize(), "list==size");
        BootstrapMethodEntry e0 = a.bootstrapMethods().get(0);
        BootstrapMethodEntry e1 = a.bootstrapMethods().get(1);
        check(e0.bsmIndex() == 0 && e1.bsmIndex() == 1, "bsmIndex==list position");
        check(e0.bootstrapMethod().kind() == 6, "kind==6 REF_invokeStatic");
        check(e0.bootstrapMethod().reference().owner().asInternalName().equals("java/lang/Integer"), "owner Integer");
        check(e0.bootstrapMethod().reference().name().stringValue().equals("valueOf"), "name valueOf");
        check(e0.arguments().isEmpty(), "bsm0 0 args");
        check(e1.arguments().size() == 2, "bsm1 2 args");
        check(((StringEntry) e1.arguments().get(0)).stringValue().equals("arg0"), "arg0 string");
        check(((IntegerEntry) e1.arguments().get(1)).intValue() == 7, "arg1 int 7");
        check(e0.constantPool() != null && e0.constantPool() == parsed.constantPool(), "entry constantPool bound");
        check(e0.bootstrapMethod().asSymbol().methodName().equals("valueOf"), "asSymbol methodName");
        // attribute 元数据
        check(a.attributeName().stringValue().equals(Attributes.NAME_BOOTSTRAP_METHODS), "attributeName==NAME_BOOTSTRAP_METHODS");
        check(a.attributeName().stringValue().equals("BootstrapMethods"), "attributeName literal");
        check(a.attributeMapper() == Attributes.bootstrapMethods(), "attributeMapper==Attributes.bootstrapMethods()");
        check(a.attributeMapper().stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        check(!a.attributeMapper().allowMultiple(), "allowMultiple false");
        // 与 invokedynamic 指令交叉：指令的 bsm 指向列表中的条目
        MethodModel mm = parsed.methods().get(0);
        InvokeDynamicInstruction indy = null;
        for (CodeElement el : mm.code().get().elementList())
            if (el instanceof InvokeDynamicInstruction x) indy = x;
        check(indy != null, "invokedynamic instruction found");
        check(indy.name().stringValue().equals("m"), "indy name");
        check(indy.bootstrapMethod().methodName().equals("valueOf"), "indy bsm methodName");
        check(indy.bootstrapMethod().owner().descriptorString().equals("Ljava/lang/Integer;"), "indy bsm owner");
        check(indy.bootstrapArgs().isEmpty(), "indy static args 0");
    }

    // ---- Part 4: javac 真类产物（lambda / string concat）----
    static Runnable mkLambda(int x) { return () -> System.identityHashCode(x); }
    static String concat(String a, int b) { return a + b; }

    static void testRealClass() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = BootstrapMethodsAttributeTest.class
                .getResourceAsStream("BootstrapMethodsAttributeTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        var found = cm.findAttribute(Attributes.bootstrapMethods());
        check(found.isPresent(), "lambda class carries BootstrapMethods");
        BootstrapMethodsAttribute a = found.get();
        check(a.bootstrapMethodsSize() >= 2, "at least metafactory + StringConcatFactory");
        check(a.bootstrapMethods().size() == a.bootstrapMethodsSize(), "list==size real");
        boolean hasMetafactory = false, hasConcat = false;
        for (BootstrapMethodEntry e : a.bootstrapMethods()) {
            String owner = e.bootstrapMethod().reference().owner().asInternalName();
            String name = e.bootstrapMethod().reference().name().stringValue();
            if (owner.equals("java/lang/invoke/LambdaMetafactory") && name.equals("metafactory")) {
                hasMetafactory = true;
                check(e.bootstrapMethod().kind() == 6, "metafactory kind 6");
                check(e.arguments().size() == 3, "metafactory 3 static args");
            }
            if (owner.equals("java/lang/invoke/StringConcatFactory")) hasConcat = true;
        }
        check(hasMetafactory, "LambdaMetafactory.metafactory entry");
        check(hasConcat, "StringConcatFactory entry from concat");
        // bsmIndex 与位置一致 + 所有条目 kind/ref/args 结构完整
        for (int i = 0; i < a.bootstrapMethods().size(); i++) {
            BootstrapMethodEntry e = a.bootstrapMethods().get(i);
            check(e.bsmIndex() == i, "real bsmIndex==pos " + i);
            check(e.bootstrapMethod().reference().tag() == 10, "ref is MethodRef tag 10 @" + i);
        }
        check(cf.verify(cm).isEmpty(), "real class verifies");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testBuildMatrix();
        testRealClass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
