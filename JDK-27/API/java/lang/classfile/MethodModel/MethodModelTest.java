/*
 * @test
 * @summary java.lang.classfile.MethodModel conformance: white-box structure + black-box behavior.
 * @run main MethodModelTest
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

public class MethodModelTest {
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
        Class<?> c = Class.forName("java.lang.classfile.MethodModel");
        check(c != null, "java.lang.classfile.MethodModel loads");
        check(c.getName().equals("java.lang.classfile.MethodModel"), "binary name java.lang.classfile.MethodModel");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.MethodModel is public");
        check(c.isInterface(), "java.lang.classfile.MethodModel is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.MethodModel has no superclass");
        check(declares(c, "flags"), "declares flags");
        check(declares(c, "parent"), "declares parent");
        check(declares(c, "methodName"), "declares methodName");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] b = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withMethodBody("add", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int, ConstantDescs.CD_int), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, cob -> cob.iload(0).iload(1).iadd().ireturn()));
        ClassModel cm = ClassFile.of().parse(b);
        MethodModel m = cm.methods().get(0);
        check(m.methodName().stringValue().equals("add"), "name");
        check(m.methodType().stringValue().equals("(II)I"), "descriptor");
        check(m.methodTypeSymbol().equals(MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_int, ConstantDescs.CD_int)), "type symbol");
        check(m.flags().has(AccessFlag.PUBLIC) && m.flags().has(AccessFlag.STATIC), "flags");
        check(m.parent().isPresent() && m.parent().get().thisClass().asInternalName().equals("pkg/T"), "parent");
        check(m.code().isPresent(), "code present");
        check(m.code().get() instanceof CodeAttribute, "code model is CodeAttribute");

    }


    // ---- real-class members (javac 产物属性源) ----
    @Deprecated
    static void depMethod() {}
    static void throwsM() throws IllegalStateException, InterruptedException {}
    static <T extends java.lang.Object> T genericM(T t, java.util.List<java.lang.String> ls) { return t; }
    static int sw(int x) { switch (x) { case 1: return 1; case 5: return 5; case 9: return 9; default: return -1; } }

    static MethodModel find(ClassModel cm, String name) {
        for (MethodModel m : cm.methods()) if (m.methodName().stringValue().equals(name)) return m;
        return null;
    }

    // ---- Part 3: 真类 main 方法定位 + 符号/flags/code/parent 全断言 ----
    static void testSelfMainMethod() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] self = MethodModelTest.class.getResourceAsStream("MethodModelTest.class").readAllBytes();
        ClassModel cm = cf.parse(self);
        MethodModel main = find(cm, "main");
        check(main != null, "self main located");
        check(main.methodName().stringValue().equals("main"), "main methodName");
        check(main.methodName().equalsString("main"), "main methodName equalsString");
        check(main.methodType().stringValue().equals("([Ljava/lang/String;)V"), "main descriptor exact");
        check(main.methodTypeSymbol().descriptorString().equals("([Ljava/lang/String;)V"), "main methodTypeSymbol descriptor");
        check(main.methodTypeSymbol().parameterCount() == 1, "main parameterCount 1");
        check(main.methodTypeSymbol().parameterType(0).descriptorString().equals("[Ljava/lang/String;"), "main param 0 descriptor");
        check(main.methodTypeSymbol().returnType().descriptorString().equals("V"), "main return V");
        check(main.flags().has(AccessFlag.PUBLIC) && main.flags().has(AccessFlag.STATIC), "main flags PUBLIC+STATIC");
        check(main.flags().flagsMask() == (ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC), "main flagsMask 0x0009");
        check(main.flags().location() == AccessFlag.Location.METHOD, "main flags location METHOD");
        check(main.code().isPresent(), "main code present");
        check(main.code().get() instanceof CodeAttribute, "main code is CodeAttribute");
        check(main.code().get().parent().isPresent() && main.code().get().parent().get() == main, "main code().parent() identity == main");
        check(main.parent().isPresent() && main.parent().get() == cm, "main parent identity == cm");
        // @Deprecated 方法：Deprecated + RuntimeVisibleAnnotations 双属性
        MethodModel dep = find(cm, "depMethod");
        check(dep != null && dep.findAttribute(Attributes.deprecated()).isPresent(), "depMethod DeprecatedAttribute");
        check(dep.findAttribute(Attributes.runtimeVisibleAnnotations()).isPresent(), "depMethod RuntimeVisibleAnnotations");
        check(dep.findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().size() == 1, "depMethod 1 annotation");
        check(dep.findAttribute(Attributes.runtimeVisibleAnnotations()).get().annotations().get(0).className().stringValue().equals("Ljava/lang/Deprecated;"), "depMethod annotation className");
        // throws 子句：ExceptionsAttribute 精确表
        MethodModel tw = find(cm, "throwsM");
        check(tw != null && tw.findAttribute(Attributes.exceptions()).isPresent(), "throwsM ExceptionsAttribute");
        List<String> exNames = new ArrayList<>();
        tw.findAttribute(Attributes.exceptions()).get().exceptions().forEach(ce -> exNames.add(ce.asInternalName()));
        check(exNames.equals(List.of("java/lang/IllegalStateException", "java/lang/InterruptedException")), "throwsM exceptions exact: " + exNames);
        // 泛型方法：Signature 精确串
        MethodModel gm = find(cm, "genericM");
        check(gm != null && gm.findAttribute(Attributes.signature()).isPresent(), "genericM SignatureAttribute");
        check(gm.findAttribute(Attributes.signature()).get().signature().stringValue().startsWith("<T:Ljava/lang/Object;>"), "genericM signature prefix");
        check(gm.findAttribute(Attributes.signature()).get().signature().stringValue().endsWith("(TT;Ljava/util/List<Ljava/lang/String;>;)TT;"), "genericM signature suffix");
        // elementList：全为 MethodElement；有 code 的方法 = [AccessFlags, CodeModel]
        for (MethodElement e : main.elementList()) check(e != null, "main element non-null");
        int afCount = 0, codeCount = 0;
        for (MethodElement e : main.elementList()) { if (e instanceof AccessFlags) afCount++; if (e instanceof CodeModel) codeCount++; }
        check(afCount == 1 && codeCount == 1, "main elementList [AccessFlags, CodeModel] (" + afCount + "," + codeCount + ")");
        check(main instanceof CompoundElement, "MethodModel instanceof CompoundElement");
        check(main instanceof ClassElement, "MethodModel instanceof ClassElement");
        check(main instanceof AttributedElement, "MethodModel instanceof AttributedElement");
        check(cf.verify(cm).isEmpty(), "self verify clean");
    }

    // ---- Part 4: 构造矩阵：abstract/native/无 code 方法 + 元素表 ----
    static void testMethodShapes() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("pkg.Shapes"), cb -> cb
                .withMethod("am", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_ABSTRACT, mb -> {})
                .withMethod("nm", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_PUBLIC | ClassFile.ACC_NATIVE, mb -> {})
                .withMethodBody("sm", MethodTypeDesc.ofDescriptor("(II)I"), ClassFile.ACC_STATIC, c -> c.iload(0).iload(1).isub().ireturn())
                .withMethod("wm", MethodTypeDesc.ofDescriptor("()V"), ClassFile.ACC_PRIVATE,
                        mb -> mb.with(ExceptionsAttribute.ofSymbols(ClassDesc.of("java.io.IOException"))).withCode(c -> c.return_())));
        ClassModel cm = cf.parse(b);
        MethodModel am = cm.methods().get(0), nm = cm.methods().get(1), sm = cm.methods().get(2), wm = cm.methods().get(3);
        check(am.methodName().stringValue().equals("am") && am.code().isEmpty(), "abstract code empty");
        check(am.flags().has(AccessFlag.ABSTRACT), "abstract has ABSTRACT");
        check(nm.code().isEmpty(), "native code empty");
        check(nm.flags().has(AccessFlag.NATIVE), "native has NATIVE");
        check(am.elementList().size() == 1 && am.elementList().get(0) instanceof AccessFlags, "abstract elementList [AccessFlags] only");
        check(nm.elementList().size() == 1, "native elementList 1 element");
        check(sm.code().isPresent(), "static code present");
        check(sm.methodType().stringValue().equals("(II)I"), "sm descriptor");
        check(sm.methodTypeSymbol().parameterCount() == 2, "sm parameterCount 2");
        check(wm.findAttribute(Attributes.exceptions()).isPresent(), "wm ExceptionsAttribute present");
        List<String> exNames = new ArrayList<>();
        wm.findAttribute(Attributes.exceptions()).get().exceptions().forEach(ce -> exNames.add(ce.asInternalName()));
        check(exNames.equals(List.of("java/io/IOException")), "wm exceptions [java/io/IOException]");
        check(cf.verify(cm).isEmpty(), "shapes verify clean");
        // parent 循环：每个方法 parent == cm；code().parent() == 方法
        for (MethodModel m : cm.methods()) {
            check(m.parent().isPresent() && m.parent().get() == cm, "shape parent identity");
            if (m.code().isPresent()) check(m.code().get().parent().get() == m, "shape code parent identity");
        }
    }

    // ---- Part 5: defineClass 运行闭环 ----
    static int dynSeq;
    static void testDefineClassLoop() throws Exception {
        ClassFile cf = ClassFile.of();
        String name = "DynMethodModel" + (++dynSeq);
        byte[] b = cf.build(ClassDesc.of(name), cb -> cb.withMethodBody("add", MethodTypeDesc.ofDescriptor("(II)I"), ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                c -> c.iload(0).iload(1).iadd().ireturn()));
        ClassModel rm = cf.parse(b);
        MethodModel m = rm.methods().get(0);
        check(m.methodName().stringValue().equals("add"), "dyn add name");
        check(m.methodType().stringValue().equals("(II)I"), "dyn add descriptor");
        Class<?> dc = MethodHandles.lookup().defineClass(b);
        check(dc.getMethod("add", int.class, int.class).invoke(null, 20, 22).equals(42), "dyn add(20,22)==42 运行闭环");
        check(cf.verify(rm).isEmpty(), "dyn verify clean");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSelfMainMethod();
        testMethodShapes();
        testDefineClassLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
