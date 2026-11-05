/*
 * @test
 * @summary java.lang.classfile.Signature.TypeArg.Unbounded conformance: white-box structure + black-box behavior.
 * @run main UnboundedTest
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

public class UnboundedTest {
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

    // ---- 真类闭环素材：javac 为 List<?> 字段生成无界通配实参 ----
    static final java.util.List<?> WILDCARD_FIELD = java.util.List.of();
    static final java.util.Map<?, ?> WILDCARD_MAP_FIELD = java.util.Map.of();

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$TypeArg$Unbounded");
        check(c != null, "java.lang.classfile.Signature.TypeArg.Unbounded loads");
        check(c.getName().equals("java.lang.classfile.Signature$TypeArg$Unbounded"), "binary name java.lang.classfile.Signature$TypeArg$Unbounded");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Signature.TypeArg.Unbounded is public");
        check(c.isInterface(), "java.lang.classfile.Signature.TypeArg.Unbounded is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.Signature.TypeArg.Unbounded has no superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        Signature.TypeArg.Unbounded u = Signature.TypeArg.unbounded();
        check(u != null, "unbounded non-null");

    }

    // ---- Part 3: 标记接口白盒 + 单例语义 ----
    static void testMarkerAndSingleton() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.Signature$TypeArg$Unbounded");
        check(c.getDeclaredMethods().length == 0, "Unbounded declares no methods of its own");
        boolean extendsTypeArg = false;
        for (Class<?> i : c.getInterfaces()) if (i == Signature.TypeArg.class) extendsTypeArg = true;
        check(extendsTypeArg, "Unbounded extends TypeArg directly");
        Signature.TypeArg.Unbounded u = Signature.TypeArg.unbounded();
        Signature.TypeArg asArg = u; // 以父接口静态视图做跨子型 instanceof
        check(u != null, "unbounded() non-null");
        check(u == Signature.TypeArg.unbounded(), "unbounded() singleton ==");
        check(u.equals(Signature.TypeArg.unbounded()), "unbounded() equals");
        check(asArg instanceof Signature.TypeArg, "Unbounded is TypeArg");
        check(!(asArg instanceof Signature.TypeArg.Bounded), "Unbounded not Bounded");
        check(u.hashCode() == Signature.TypeArg.unbounded().hashCode(), "singleton hashCode stable");
    }

    // ---- Part 4: 解析嵌入矩阵（List<*> / Map<**> / Map<*,?> 组合 / 嵌套泛型） ----
    static void testParseEmbedding() {
        Signature.ClassTypeSig lst = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<*>;");
        check(lst.typeArgs().size() == 1, "List<*> 1 arg");
        check(lst.typeArgs().get(0) instanceof Signature.TypeArg.Unbounded, "List<*> arg Unbounded");
        check(!(lst.typeArgs().get(0) instanceof Signature.TypeArg.Bounded), "List<*> arg not Bounded");
        Signature.ClassTypeSig map = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/Map<**>;");
        check(map.typeArgs().size() == 2, "Map<**> 2 args");
        check(map.typeArgs().get(0) instanceof Signature.TypeArg.Unbounded
                && map.typeArgs().get(1) instanceof Signature.TypeArg.Unbounded, "Map<**> both Unbounded");
        // 混合：Unbounded 与 Bounded 同列
        Signature.ClassTypeSig mix = (Signature.ClassTypeSig) Signature.parseFrom(
                "Ljava/util/Map<*Ljava/lang/String;>;");
        check(mix.typeArgs().size() == 2, "mixed Map 2 args");
        check(mix.typeArgs().get(0) instanceof Signature.TypeArg.Unbounded, "mixed arg0 Unbounded");
        check(mix.typeArgs().get(1) instanceof Signature.TypeArg.Bounded, "mixed arg1 Bounded");
        // 嵌套：外层 Bounded、内层 Unbounded
        Signature.ClassTypeSig nested = (Signature.ClassTypeSig) Signature.parseFrom("Ljava/util/List<Ljava/util/List<*>;>;");
        Signature.TypeArg.Bounded outer = (Signature.TypeArg.Bounded) nested.typeArgs().get(0);
        check(((Signature.ClassTypeSig) outer.boundType()).typeArgs().get(0)
                instanceof Signature.TypeArg.Unbounded, "inner wildcard Unbounded");
        // round-trip 幂等
        String[] legal = {"Ljava/util/List<*>;", "Ljava/util/Map<**>;", "Ljava/util/Map<*Ljava/lang/String;>;",
                "Ljava/util/List<Ljava/util/List<*>;>;"};
        for (String s : legal) {
            String out = Signature.parseFrom(s).signatureString();
            check(out.equals(s), "round-trip '" + s + "' -> '" + out + "'");
            check(Signature.parseFrom(out).signatureString().equals(s), "re-parse idempotent: " + s);
        }
    }

    // ---- Part 5: 发射组合（ClassTypeSig.of 携带 unbounded 实参 → 精确串） ----
    static void testCompositionEmission() {
        check(Signature.ClassTypeSig.of(ClassDesc.of("java.util.List"), Signature.TypeArg.unbounded())
                .signatureString().equals("Ljava/util/List<*>;"), "emit List<*>");
        check(Signature.ClassTypeSig.of(ClassDesc.of("java.util.Map"),
                        Signature.TypeArg.unbounded(), Signature.TypeArg.unbounded())
                .signatureString().equals("Ljava/util/Map<**>;"), "emit Map<**>");
        Signature.ClassTypeSig emitted = Signature.ClassTypeSig.of(ClassDesc.of("java.util.List"), Signature.TypeArg.unbounded());
        Signature.ClassTypeSig back = (Signature.ClassTypeSig) Signature.parseFrom(emitted.signatureString());
        check(back.typeArgs().get(0) instanceof Signature.TypeArg.Unbounded, "emitted wildcard survives round-trip");
        check(back.typeArgs().get(0).equals(Signature.TypeArg.unbounded()), "parsed unbounded == unbounded()");
        // 数组元素位含无界通配
        check(Signature.parseFrom("[Ljava/util/List<*>;") instanceof Signature.ArrayTypeSig, "array of List<?>");
    }

    // ---- Part 6: 真类闭环（javac List<?> / Map<?,?> 字段 → Unbounded 实参） ----
    static void testRealClassFieldLoop() throws Exception {
        byte[] self = UnboundedTest.class.getResourceAsStream("UnboundedTest.class").readAllBytes();
        ClassModel cm = ClassFile.of().parse(self);
        String[][] rows = {{"WILDCARD_FIELD", "Ljava/util/List<*>;", "1"},
                {"WILDCARD_MAP_FIELD", "Ljava/util/Map<**>;", "2"}};
        for (String[] row : rows) {
            FieldModel fm = null;
            for (FieldModel f : cm.fields()) if (f.fieldName().stringValue().equals(row[0])) fm = f;
            check(fm != null, row[0] + " found");
            String fs = fm.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue();
            check(fs.equals(row[1]), row[0] + " sig exact: " + fs);
            Signature.ClassTypeSig model = (Signature.ClassTypeSig) Signature.parseFrom(fs);
            check(model.typeArgs().size() == Integer.parseInt(row[2]), row[0] + " arg count");
            for (int i = 0; i < model.typeArgs().size(); i++)
                check(model.typeArgs().get(i) instanceof Signature.TypeArg.Unbounded, row[0] + " arg" + i + " Unbounded");
            check(Signature.parseFrom(model.signatureString()).equals(model), row[0] + " model idempotent");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMarkerAndSingleton();
        testParseEmbedding();
        testCompositionEmission();
        testRealClassFieldLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
