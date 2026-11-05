/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.EmptyTarget conformance: white-box structure + black-box behavior.
 * @run main EmptyTargetTest
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

public class EmptyTargetTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$EmptyTarget");
        check(c != null, "java.lang.classfile.TypeAnnotation.EmptyTarget loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$EmptyTarget"), "binary name java.lang.classfile.TypeAnnotation$EmptyTarget");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.EmptyTarget is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.EmptyTarget is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.EmptyTarget has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation.EmptyTarget t = TypeAnnotation.TargetInfo.ofMethodReturn();
        check(t.targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "targetType");

    }

    // ===== 深化：真 javac 产物载体（字段 / 方法返回值两个 EmptyTarget 位点） =====
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface ETA { String value() default "x"; }

    static class Holder {
        @ETA("f") String f;
        @ETA("ret") String ret() { return null; }
    }

    static String aval(TypeAnnotation t) {
        for (AnnotationElement e : t.annotation().elements())
            if (e.name().stringValue().equals("value"))
                return ((AnnotationValue.OfString) e.value()).stringValue();
        return "?";
    }
    static TypeAnnotation findOne(java.util.List<TypeAnnotation> l, String val) {
        TypeAnnotation r = null;
        for (TypeAnnotation t : l) {
            if (aval(t).equals(val)) {
                if (r != null) throw new AssertionError("duplicate " + val);
                r = t;
            }
        }
        if (r == null) throw new AssertionError("missing " + val);
        return r;
    }

    // ---- Part 3: of(TargetType) 只收 FIELD/METHOD_RETURN/METHOD_RECEIVER；命名工厂全集 ----
    static void testFactoryMatrix() {
        check(TypeAnnotation.TargetInfo.of(TypeAnnotation.TargetType.FIELD).targetType() == TypeAnnotation.TargetType.FIELD, "of(FIELD)");
        check(TypeAnnotation.TargetInfo.of(TypeAnnotation.TargetType.METHOD_RETURN).targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "of(METHOD_RETURN)");
        check(TypeAnnotation.TargetInfo.of(TypeAnnotation.TargetType.METHOD_RECEIVER).targetType() == TypeAnnotation.TargetType.METHOD_RECEIVER, "of(METHOD_RECEIVER)");
        TypeAnnotation.EmptyTarget f = TypeAnnotation.TargetInfo.ofField();
        check(f instanceof TypeAnnotation.EmptyTarget && f.targetType() == TypeAnnotation.TargetType.FIELD && f.size() == 0, "ofField empty size 0");
        check(TypeAnnotation.TargetInfo.ofMethodReturn().targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "ofMethodReturn");
        check(TypeAnnotation.TargetInfo.ofMethodReturn().size() == 0, "ofMethodReturn size 0");
        check(TypeAnnotation.TargetInfo.ofMethodReceiver().targetType() == TypeAnnotation.TargetType.METHOD_RECEIVER, "ofMethodReceiver");
        check(TypeAnnotation.TargetInfo.ofMethodReceiver().size() == 0, "ofMethodReceiver size 0");
        // 非 Empty 目标类型一律 IAE（实测消息 Wrong target type specified X）
        for (TypeAnnotation.TargetType bad : new TypeAnnotation.TargetType[]{
                TypeAnnotation.TargetType.CAST, TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER,
                TypeAnnotation.TargetType.CLASS_EXTENDS, TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER_BOUND, TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER_BOUND,
                TypeAnnotation.TargetType.METHOD_FORMAL_PARAMETER, TypeAnnotation.TargetType.THROWS, TypeAnnotation.TargetType.LOCAL_VARIABLE,
                TypeAnnotation.TargetType.RESOURCE_VARIABLE, TypeAnnotation.TargetType.EXCEPTION_PARAMETER, TypeAnnotation.TargetType.INSTANCEOF,
                TypeAnnotation.TargetType.NEW, TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE, TypeAnnotation.TargetType.METHOD_REFERENCE,
                TypeAnnotation.TargetType.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT, TypeAnnotation.TargetType.METHOD_INVOCATION_TYPE_ARGUMENT,
                TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT, TypeAnnotation.TargetType.METHOD_REFERENCE_TYPE_ARGUMENT}) {
            try {
                TypeAnnotation.TargetInfo.of(bad);
                check(false, "of(" + bad + ") must IAE");
            } catch (IllegalArgumentException e) {
                check(e.getMessage() != null && e.getMessage().contains("Wrong target type"), "of(" + bad + ") IAE msg");
            }
        }
        try { TypeAnnotation.TargetInfo.of(null); check(false, "of(null) must NPE"); }
        catch (NullPointerException e) { check(true, "of(null) NPE"); }
    }

    // ---- Part 4: 真 javac 产物（FIELD / METHOD_RETURN；METHOD_RECEIVER javac 不可产 → 仅工厂） ----
    static void testRealProduct() throws Exception {
        byte[] hb = EmptyTargetTest.class.getResourceAsStream("EmptyTargetTest$Holder.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(hb);
        java.util.List<TypeAnnotation> fs = cm.fields().get(0).findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(fs.size() == 1, "field RVTA count 1");
        check(fs.get(0).targetInfo() instanceof TypeAnnotation.EmptyTarget, "field target is EmptyTarget");
        check(fs.get(0).targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "field targetType FIELD");
        check(fs.get(0).targetInfo().size() == 0, "field EmptyTarget size 0");
        check(fs.get(0).targetPath().isEmpty(), "field path empty");
        check(fs.get(0).annotation().className().stringValue().endsWith("$ETA;"), "field ann descriptor");
        MethodModel rm = null;
        for (MethodModel m : cm.methods())
            if (m.methodName().stringValue().equals("ret")) rm = m;
        java.util.List<TypeAnnotation> ms = rm.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(ms.size() == 1, "method return RVTA count 1");
        check(findOne(ms, "ret").targetInfo() instanceof TypeAnnotation.EmptyTarget, "return target is EmptyTarget");
        check(findOne(ms, "ret").targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "return targetType METHOD_RETURN");
        check(findOne(ms, "ret").targetPath().isEmpty(), "return path empty");
        // 注入闭环：EmptyTarget 三形态全部 build→re-parse 回读
        Annotation an = Annotation.of(ClassDesc.of("p.E"));
        byte[] b = ClassFile.of().build(ClassDesc.of("p.ETInj"), cb -> cb
            .withField("fx", ConstantDescs.CD_String, fb -> fb.with(RuntimeVisibleTypeAnnotationsAttribute.of(
                    TypeAnnotation.of(TypeAnnotation.TargetInfo.ofField(), java.util.List.of(), an))))
            .withMethod("m", MethodTypeDesc.ofDescriptor("()V"), 0, mb -> mb.with(
                    RuntimeVisibleTypeAnnotationsAttribute.of(
                            TypeAnnotation.of(TypeAnnotation.TargetInfo.ofMethodReturn(), java.util.List.of(), an),
                            TypeAnnotation.of(TypeAnnotation.TargetInfo.ofMethodReceiver(), java.util.List.of(), an)))));
        ClassModel back = ClassFile.of().parse(b);
        check(back.fields().get(0).findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations().get(0)
                .targetInfo().targetType() == TypeAnnotation.TargetType.FIELD, "injected FIELD readback");
        MethodModel im = back.methods().stream().filter(m -> m.methodName().stringValue().equals("m")).findFirst().orElseThrow();
        java.util.List<TypeAnnotation> ias = im.findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(ias.size() == 2, "injected method count 2");
        check(ias.get(0).targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "injected METHOD_RETURN");
        check(ias.get(1).targetInfo().targetType() == TypeAnnotation.TargetType.METHOD_RECEIVER, "injected METHOD_RECEIVER");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testRealProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
