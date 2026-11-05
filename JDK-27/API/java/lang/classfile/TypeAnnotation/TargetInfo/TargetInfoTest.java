/*
 * @test
 * @summary java.lang.classfile.TypeAnnotation.TargetInfo conformance: white-box structure + black-box behavior.
 * @run main TargetInfoTest
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

public class TargetInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.TypeAnnotation$TargetInfo");
        check(c != null, "java.lang.classfile.TypeAnnotation.TargetInfo loads");
        check(c.getName().equals("java.lang.classfile.TypeAnnotation$TargetInfo"), "binary name java.lang.classfile.TypeAnnotation$TargetInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.TypeAnnotation.TargetInfo is public");
        check(c.isInterface(), "java.lang.classfile.TypeAnnotation.TargetInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.TypeAnnotation.TargetInfo has no superclass");
        check(declares(c, "targetType"), "declares targetType");
        check(declares(c, "size"), "declares size");
        check(declares(c, "ofTypeParameter"), "declares ofTypeParameter");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        TypeAnnotation.TargetInfo t = TypeAnnotation.TargetInfo.ofField();
        check(t.targetType() == TypeAnnotation.TargetType.FIELD, "ofField targetType");
        TypeAnnotation.TargetInfo p = TypeAnnotation.TargetInfo.ofClassTypeParameter(2);
        check(p.targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, "ofClassTypeParameter type");
        check(((TypeAnnotation.TypeParameterTarget) p).typeParameterIndex() == 2, "index 2");
        check(TypeAnnotation.TargetInfo.ofMethodReturn().targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "ofMethodReturn");
        check(TypeAnnotation.TargetInfo.ofExceptionParameter(0).targetType() == TypeAnnotation.TargetType.EXCEPTION_PARAMETER, "ofExceptionParameter");

    }

    // ---- Part 3: 全 32 个静态工厂矩阵（javap 锁定签名 + 实测 size/targetType/访问器） ----
    static void testFactoryMatrix() {
        // 类型参数族
        TypeAnnotation.TypeParameterTarget tp1 = TypeAnnotation.TargetInfo.ofClassTypeParameter(0);
        check(tp1.targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, "ofClassTypeParameter");
        check(tp1.typeParameterIndex() == 0 && tp1.size() == 1, "ofClassTypeParameter idx/size");
        TypeAnnotation.TypeParameterTarget tp2 = TypeAnnotation.TargetInfo.ofMethodTypeParameter(3);
        check(tp2.targetType() == TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER && tp2.typeParameterIndex() == 3, "ofMethodTypeParameter");
        TypeAnnotation.TypeParameterTarget tp3 = TypeAnnotation.TargetInfo.ofTypeParameter(TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER, 2);
        check(tp3.typeParameterIndex() == 2 && tp3.targetType() == TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER, "ofTypeParameter(MTP,2)");
        check(TypeAnnotation.TargetInfo.ofTypeParameter(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, 1)
                .targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, "ofTypeParameter(CTP,1)");
        // 超类型族
        TypeAnnotation.SupertypeTarget st = TypeAnnotation.TargetInfo.ofClassExtends(65535);
        check(st.targetType() == TypeAnnotation.TargetType.CLASS_EXTENDS && st.supertypeIndex() == 65535 && st.size() == 2,
                "ofClassExtends(65535) superclass sentinel");
        check(TypeAnnotation.TargetInfo.ofClassExtends(2).supertypeIndex() == 2, "ofClassExtends(2) interface index");
        // bound 族
        TypeAnnotation.TypeParameterBoundTarget bd1 = TypeAnnotation.TargetInfo.ofClassTypeParameterBound(1, 2);
        check(bd1.targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER_BOUND, "ofClassTypeParameterBound type");
        check(bd1.typeParameterIndex() == 1 && bd1.boundIndex() == 2 && bd1.size() == 2, "ofClassTypeParameterBound idx");
        TypeAnnotation.TypeParameterBoundTarget bd2 = TypeAnnotation.TargetInfo.ofMethodTypeParameterBound(0, 0);
        check(bd2.targetType() == TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER_BOUND && bd2.typeParameterIndex() == 0 && bd2.boundIndex() == 0,
                "ofMethodTypeParameterBound");
        check(TypeAnnotation.TargetInfo.ofTypeParameterBound(TypeAnnotation.TargetType.METHOD_TYPE_PARAMETER_BOUND, 4, 5).boundIndex() == 5,
                "ofTypeParameterBound(MTPB,4,5)");
        check(TypeAnnotation.TargetInfo.ofTypeParameterBound(TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER_BOUND, 4, 5)
                .targetType() == TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER_BOUND, "ofTypeParameterBound(CTPB,4,5)");
        // EmptyTarget 族（仅 FIELD/METHOD_RETURN/METHOD_RECEIVER 三种）
        check(TypeAnnotation.TargetInfo.of(TypeAnnotation.TargetType.FIELD) instanceof TypeAnnotation.EmptyTarget, "of(FIELD)");
        check(TypeAnnotation.TargetInfo.of(TypeAnnotation.TargetType.METHOD_RETURN).targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "of(METHOD_RETURN)");
        check(TypeAnnotation.TargetInfo.of(TypeAnnotation.TargetType.METHOD_RECEIVER).targetType() == TypeAnnotation.TargetType.METHOD_RECEIVER, "of(METHOD_RECEIVER)");
        TypeAnnotation.EmptyTarget ef = TypeAnnotation.TargetInfo.ofField();
        check(ef.targetType() == TypeAnnotation.TargetType.FIELD && ef.size() == 0, "ofField size 0");
        check(TypeAnnotation.TargetInfo.ofMethodReturn().targetType() == TypeAnnotation.TargetType.METHOD_RETURN, "ofMethodReturn");
        check(TypeAnnotation.TargetInfo.ofMethodReceiver().targetType() == TypeAnnotation.TargetType.METHOD_RECEIVER, "ofMethodReceiver");
        // 形参/throws/异常表
        TypeAnnotation.FormalParameterTarget fp = TypeAnnotation.TargetInfo.ofMethodFormalParameter(255);
        check(fp.targetType() == TypeAnnotation.TargetType.METHOD_FORMAL_PARAMETER && fp.formalParameterIndex() == 255 && fp.size() == 1,
                "ofMethodFormalParameter(255) u1 max");
        TypeAnnotation.ThrowsTarget tt = TypeAnnotation.TargetInfo.ofThrows(9);
        check(tt.targetType() == TypeAnnotation.TargetType.THROWS && tt.throwsTargetIndex() == 9 && tt.size() == 2, "ofThrows(9)");
        TypeAnnotation.CatchTarget ct = TypeAnnotation.TargetInfo.ofExceptionParameter(7);
        check(ct.targetType() == TypeAnnotation.TargetType.EXCEPTION_PARAMETER && ct.exceptionTableIndex() == 7 && ct.size() == 2, "ofExceptionParameter(7)");
    }

    // ---- Part 4: 校验负例矩阵（实测：类型匹配先于 Label 判空；u1/u2 越界 IAE 精确消息） ----
    static void testValidation() {
        try { TypeAnnotation.TargetInfo.of(null); check(false, "of(null) must NPE"); }
        catch (NullPointerException e) { check(e.getMessage() != null && e.getMessage().contains("targetType"), "of(null) NPE msg targetType"); }
        for (TypeAnnotation.TargetType bad : new TypeAnnotation.TargetType[]{
                TypeAnnotation.TargetType.CAST, TypeAnnotation.TargetType.CLASS_TYPE_PARAMETER, TypeAnnotation.TargetType.THROWS,
                TypeAnnotation.TargetType.LOCAL_VARIABLE, TypeAnnotation.TargetType.METHOD_FORMAL_PARAMETER, TypeAnnotation.TargetType.NEW}) {
            try {
                TypeAnnotation.TargetInfo.of(bad);
                check(false, "of(" + bad + ") must IAE");
            } catch (IllegalArgumentException e) {
                check(e.getMessage() != null && e.getMessage().contains("Wrong target type"), "of(" + bad + ") IAE msg");
            }
        }
        try { TypeAnnotation.TargetInfo.ofTypeParameter(TypeAnnotation.TargetType.FIELD, 0); check(false, "ofTypeParameter(FIELD) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null && e.getMessage().contains("FIELD"), "ofTypeParameter(FIELD) msg"); }
        try { TypeAnnotation.TargetInfo.ofTypeParameterBound(TypeAnnotation.TargetType.FIELD, 0, 0); check(false, "ofTypeParameterBound(FIELD) must IAE"); }
        catch (IllegalArgumentException e) { check(true, "ofTypeParameterBound(FIELD) IAE"); }
        try { TypeAnnotation.TargetInfo.ofVariable(TypeAnnotation.TargetType.FIELD, java.util.List.of()); check(false, "ofVariable(FIELD) must IAE"); }
        catch (IllegalArgumentException e) { check(true, "ofVariable(FIELD) IAE"); }
        try { TypeAnnotation.TargetInfo.ofOffset(TypeAnnotation.TargetType.FIELD, null); check(false, "ofOffset(FIELD,null) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage() != null && e.getMessage().contains("Wrong target type"), "ofOffset type check precedes null label"); }
        try { TypeAnnotation.TargetInfo.ofTypeArgument(TypeAnnotation.TargetType.FIELD, null, 0); check(false, "ofTypeArgument(FIELD) must IAE"); }
        catch (IllegalArgumentException e) { check(true, "ofTypeArgument(FIELD) IAE"); }
        // u2 越界（负数 / 65536），65535 合法
        try { TypeAnnotation.TargetInfo.ofClassExtends(-1); check(false, "ofClassExtends(-1) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("supertype index out of range of u2: -1"), "ofClassExtends(-1) msg"); }
        try { TypeAnnotation.TargetInfo.ofClassExtends(65536); check(false, "ofClassExtends(65536) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("supertype index out of range of u2: 65536"), "ofClassExtends(65536) msg"); }
        try { TypeAnnotation.TargetInfo.ofThrows(-1); check(false, "ofThrows(-1) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("throws type index out of range of u2"), "ofThrows(-1) msg"); }
        try { TypeAnnotation.TargetInfo.ofThrows(65536); check(false, "ofThrows(65536) must IAE"); }
        catch (IllegalArgumentException e) { check(true, "ofThrows(65536) IAE"); }
        try { TypeAnnotation.TargetInfo.ofExceptionParameter(-1); check(false, "ofExceptionParameter(-1) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("exception table index out of range of u2"), "ofExceptionParameter(-1) msg"); }
        try { TypeAnnotation.TargetInfo.ofExceptionParameter(65536); check(false, "ofExceptionParameter(65536) must IAE"); }
        catch (IllegalArgumentException e) { check(true, "ofExceptionParameter(65536) IAE"); }
        // u1 越界（负数 / 300），255 合法
        try { TypeAnnotation.TargetInfo.ofMethodFormalParameter(-1); check(false, "ofMethodFormalParameter(-1) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().equals("formal parameter index out of range of u1: -1"), "ofMethodFormalParameter(-1) msg"); }
        try { TypeAnnotation.TargetInfo.ofMethodFormalParameter(300); check(false, "ofMethodFormalParameter(300) must IAE"); }
        catch (IllegalArgumentException e) { check(true, "ofMethodFormalParameter(300) IAE"); }
        try { TypeAnnotation.TargetInfo.ofClassTypeParameter(-1); check(false, "ofClassTypeParameter(-1) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("type parameter index out of range of u1"), "ofClassTypeParameter(-1) msg"); }
        try { TypeAnnotation.TargetInfo.ofClassTypeParameterBound(-1, 0); check(false, "ofClassTypeParameterBound(-1,0) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("type parameter index out of range of u1"), "ofClassTypeParameterBound(-1,0) msg"); }
        try { TypeAnnotation.TargetInfo.ofClassTypeParameterBound(0, 300); check(false, "ofClassTypeParameterBound(0,300) must IAE"); }
        catch (IllegalArgumentException e) { check(e.getMessage().contains("bound index out of range of u1"), "ofClassTypeParameterBound(0,300) msg"); }
        try { TypeAnnotation.TargetInfo.ofCastExpr(null, 0); check(false, "ofCastExpr(null,0) must NPE"); }
        catch (NullPointerException e) { check(true, "ofCastExpr(null,0) NPE"); }
        try { TypeAnnotation.TargetInfo.ofNewExpr(null); check(false, "ofNewExpr(null) must NPE"); }
        catch (NullPointerException e) { check(true, "ofNewExpr(null) NPE"); }
        try { TypeAnnotation.LocalVarTargetInfo.of(null, null, 0); check(false, "LocalVarTargetInfo.of(null,null,0) must NPE"); }
        catch (NullPointerException e) { check(true, "LocalVarTargetInfo.of(null,null,0) NPE"); }
    }

    // ---- Part 5: Label 相关工厂（build 上下文内）+ 真产物多态 ----
    static final TypeAnnotation.TargetInfo[] shapes = new TypeAnnotation.TargetInfo[9];
    static final java.util.List<TypeAnnotation.LocalVarTargetInfo> table1 = new java.util.ArrayList<>();

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE_USE, java.lang.annotation.ElementType.LOCAL_VARIABLE})
    @interface TI { }
    @TI String tif;

    static void testLabelFactoriesAndProduct() throws Exception {
        ClassFile.of().build(ClassDesc.of("pkg.TIFab"), cb -> cb.withMethodBody("m", MethodTypeDesc.ofDescriptor("()V"), 0, cob -> {
            Label s = cob.newLabel();
            Label e = cob.newLabel();
            shapes[0] = TypeAnnotation.TargetInfo.ofInstanceofExpr(s);
            shapes[1] = TypeAnnotation.TargetInfo.ofNewExpr(e);
            shapes[2] = TypeAnnotation.TargetInfo.ofConstructorReference(s);
            shapes[3] = TypeAnnotation.TargetInfo.ofMethodReference(e);
            shapes[4] = TypeAnnotation.TargetInfo.ofCastExpr(s, 2);
            shapes[5] = TypeAnnotation.TargetInfo.ofConstructorInvocationTypeArgument(s, 1);
            shapes[6] = TypeAnnotation.TargetInfo.ofMethodInvocationTypeArgument(e, 0);
            shapes[7] = TypeAnnotation.TargetInfo.ofConstructorReferenceTypeArgument(s, 3);
            shapes[8] = TypeAnnotation.TargetInfo.ofMethodReferenceTypeArgument(e, 4);
            table1.add(TypeAnnotation.LocalVarTargetInfo.of(s, e, 2));
            table1.add(TypeAnnotation.LocalVarTargetInfo.of(s, e, 3));
            TypeAnnotation.LocalVarTarget lvo = TypeAnnotation.TargetInfo.ofLocalVariable(table1);
            check(lvo.targetType() == TypeAnnotation.TargetType.LOCAL_VARIABLE && lvo.size() == 2 + 6 * 2, "ofLocalVariable size 2+6n");
            TypeAnnotation.LocalVarTarget lvr = TypeAnnotation.TargetInfo.ofResourceVariable(java.util.List.of(table1.get(0)));
            check(lvr.targetType() == TypeAnnotation.TargetType.RESOURCE_VARIABLE && lvr.size() == 8, "ofResourceVariable size 8");
            check(TypeAnnotation.TargetInfo.ofVariable(TypeAnnotation.TargetType.LOCAL_VARIABLE, table1).table().size() == 2, "ofVariable(LOCAL_VARIABLE)");
            check(TypeAnnotation.TargetInfo.ofVariable(TypeAnnotation.TargetType.RESOURCE_VARIABLE, java.util.List.of()).size() == 2, "ofVariable(RESOURCE, empty)");
            cob.labelBinding(s).labelBinding(e).return_();
        }));
        int[] expectSize = {2, 2, 2, 2, 3, 3, 3, 3, 3};
        TypeAnnotation.TargetType[] expectType = {
                TypeAnnotation.TargetType.INSTANCEOF, TypeAnnotation.TargetType.NEW,
                TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE, TypeAnnotation.TargetType.METHOD_REFERENCE,
                TypeAnnotation.TargetType.CAST, TypeAnnotation.TargetType.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT,
                TypeAnnotation.TargetType.METHOD_INVOCATION_TYPE_ARGUMENT,
                TypeAnnotation.TargetType.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT, TypeAnnotation.TargetType.METHOD_REFERENCE_TYPE_ARGUMENT};
        for (int i = 0; i < 9; i++) {
            check(shapes[i] instanceof TypeAnnotation.TargetInfo, "shape#" + i + " instanceof TargetInfo");
            check(shapes[i].targetType() == expectType[i], "shape#" + i + " targetType");
            check(shapes[i].size() == expectSize[i], "shape#" + i + " size");
        }
        check(((TypeAnnotation.OffsetTarget) shapes[0]).target() != null, "ofInstanceofExpr label non-null");
        check(((TypeAnnotation.TypeArgumentTarget) shapes[4]).typeArgumentIndex() == 2, "ofCastExpr taIdx");
        check(((TypeAnnotation.TypeArgumentTarget) shapes[8]).typeArgumentIndex() == 4, "ofMethodReferenceTypeArgument taIdx");
        check(((TypeAnnotation.TypeArgumentTarget) shapes[6]).target() != null, "ofMethodInvocationTypeArgument label");
        // 真产物多态：本类字段上的 @TI 生成 FIELD EmptyTarget
        byte[] self = TargetInfoTest.class.getResourceAsStream("TargetInfoTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        java.util.List<TypeAnnotation> tifAnns = cm.fields().stream()
                .filter(f -> f.fieldName().stringValue().equals("tif"))
                .findFirst().orElseThrow().findAttribute(Attributes.runtimeVisibleTypeAnnotations()).orElseThrow().annotations();
        check(tifAnns.size() == 1, "tif real product count 1");
        TypeAnnotation.TargetInfo rti = tifAnns.get(0).targetInfo();
        check(rti instanceof TypeAnnotation.EmptyTarget, "real product FIELD is EmptyTarget");
        check(rti.targetType() == TypeAnnotation.TargetType.FIELD, "real product targetType FIELD");
        check(rti.size() == 0, "real product size 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactoryMatrix();
        testValidation();
        testLabelFactoriesAndProduct();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
