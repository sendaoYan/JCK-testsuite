/*
 * @test
 * @summary java.lang.classfile.AttributeMapper conformance: white-box structure + black-box behavior.
 * @run main AttributeMapperTest
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

public class AttributeMapperTest {
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
        Class<?> c = Class.forName("java.lang.classfile.AttributeMapper");
        check(c != null, "java.lang.classfile.AttributeMapper loads");
        check(c.getName().equals("java.lang.classfile.AttributeMapper"), "binary name java.lang.classfile.AttributeMapper");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.AttributeMapper is public");
        check(c.isInterface(), "java.lang.classfile.AttributeMapper is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.AttributeMapper has no superclass");
        check(declares(c, "name"), "declares name");
        check(declares(c, "readAttribute"), "declares readAttribute");
        check(declares(c, "writeAttribute"), "declares writeAttribute");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        AttributeMapper<SourceFileAttribute> m = Attributes.sourceFile();
        check(m.name().equals("SourceFile"), "name");
        check(!m.allowMultiple(), "sourceFile allowMultiple false");
        check(m.stability() == AttributeMapper.AttributeStability.CP_REFS, "stability CP_REFS");
        AttributeMapper<CodeAttribute> cm = Attributes.code();
        check(cm.name().equals("Code") && !cm.allowMultiple(), "code mapper");

    }

    // ---- Part 3: Attributes 注册表全量（36 个 mapper 方法，javap 锁定）非 null + name() 与 NAME_* 常量全等 + 重复调用同实例 ----
    static final String[][] REGISTRY = {
            {"annotationDefault", "NAME_ANNOTATION_DEFAULT", "AnnotationDefault"},
            {"bootstrapMethods", "NAME_BOOTSTRAP_METHODS", "BootstrapMethods"},
            {"characterRangeTable", "NAME_CHARACTER_RANGE_TABLE", "CharacterRangeTable"},
            {"code", "NAME_CODE", "Code"},
            {"compilationId", "NAME_COMPILATION_ID", "CompilationID"},
            {"constantValue", "NAME_CONSTANT_VALUE", "ConstantValue"},
            {"deprecated", "NAME_DEPRECATED", "Deprecated"},
            {"enclosingMethod", "NAME_ENCLOSING_METHOD", "EnclosingMethod"},
            {"exceptions", "NAME_EXCEPTIONS", "Exceptions"},
            {"innerClasses", "NAME_INNER_CLASSES", "InnerClasses"},
            {"lineNumberTable", "NAME_LINE_NUMBER_TABLE", "LineNumberTable"},
            {"localVariableTable", "NAME_LOCAL_VARIABLE_TABLE", "LocalVariableTable"},
            {"localVariableTypeTable", "NAME_LOCAL_VARIABLE_TYPE_TABLE", "LocalVariableTypeTable"},
            {"methodParameters", "NAME_METHOD_PARAMETERS", "MethodParameters"},
            {"module", "NAME_MODULE", "Module"},
            {"moduleHashes", "NAME_MODULE_HASHES", "ModuleHashes"},
            {"moduleMainClass", "NAME_MODULE_MAIN_CLASS", "ModuleMainClass"},
            {"modulePackages", "NAME_MODULE_PACKAGES", "ModulePackages"},
            {"moduleResolution", "NAME_MODULE_RESOLUTION", "ModuleResolution"},
            {"moduleTarget", "NAME_MODULE_TARGET", "ModuleTarget"},
            {"nestHost", "NAME_NEST_HOST", "NestHost"},
            {"nestMembers", "NAME_NEST_MEMBERS", "NestMembers"},
            {"permittedSubclasses", "NAME_PERMITTED_SUBCLASSES", "PermittedSubclasses"},
            {"record", "NAME_RECORD", "Record"},
            {"runtimeInvisibleAnnotations", "NAME_RUNTIME_INVISIBLE_ANNOTATIONS", "RuntimeInvisibleAnnotations"},
            {"runtimeInvisibleParameterAnnotations", "NAME_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS", "RuntimeInvisibleParameterAnnotations"},
            {"runtimeInvisibleTypeAnnotations", "NAME_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS", "RuntimeInvisibleTypeAnnotations"},
            {"runtimeVisibleAnnotations", "NAME_RUNTIME_VISIBLE_ANNOTATIONS", "RuntimeVisibleAnnotations"},
            {"runtimeVisibleParameterAnnotations", "NAME_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS", "RuntimeVisibleParameterAnnotations"},
            {"runtimeVisibleTypeAnnotations", "NAME_RUNTIME_VISIBLE_TYPE_ANNOTATIONS", "RuntimeVisibleTypeAnnotations"},
            {"signature", "NAME_SIGNATURE", "Signature"},
            {"sourceDebugExtension", "NAME_SOURCE_DEBUG_EXTENSION", "SourceDebugExtension"},
            {"sourceFile", "NAME_SOURCE_FILE", "SourceFile"},
            {"sourceId", "NAME_SOURCE_ID", "SourceID"},
            {"stackMapTable", "NAME_STACK_MAP_TABLE", "StackMapTable"},
            {"synthetic", "NAME_SYNTHETIC", "Synthetic"},
    };

    static void testFullRegistry() throws Exception {
        check(REGISTRY.length == 36, "registry table has 36 mappers: " + REGISTRY.length);
        int verified = 0;
        for (String[] row : REGISTRY) {
            AttributeMapper<?> m = (AttributeMapper<?>) Attributes.class.getMethod(row[0]).invoke(null);
            String nameConst = (String) Attributes.class.getField(row[1]).get(null);
            check(m != null, "mapper non-null: " + row[0]);
            check(m.name() != null, "mapper name non-null: " + row[0]);
            check(m.name().equals(nameConst), "mapper name == " + row[1] + " (" + row[0] + "): " + m.name());
            check(nameConst.equals(row[2]), row[1] + " literal == " + row[2]);
            check(m.name().equals(row[2]), "mapper name literal == " + row[2]);
            check(m.stability() != null, "stability non-null: " + row[0]);
            AttributeMapper<?> again = (AttributeMapper<?>) Attributes.class.getMethod(row[0]).invoke(null);
            check(again == m, "repeat call same mapper instance (==): " + row[0]);
            verified++;
        }
        check(verified >= 30, "at least 30 mapper/constant pairs verified in loop: " + verified);
        // 注册表完备性：Attributes 的静态工厂方法恰 36 个返回 AttributeMapper（javap 锁定）
        int mapperMethods = 0;
        for (Method mth : Attributes.class.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isStatic(mth.getModifiers()) && mth.getReturnType() == AttributeMapper.class && mth.getParameterCount() == 0) mapperMethods++;
        check(mapperMethods == 36, "Attributes exposes exactly 36 mapper factories: " + mapperMethods);
        int nameFields = 0;
        for (java.lang.reflect.Field f : Attributes.class.getDeclaredFields())
            if (f.getType() == String.class && f.getName().startsWith("NAME_")) nameFields++;
        check(nameFields == 36, "Attributes exposes exactly 36 NAME_* constants: " + nameFields);
    }

    // ---- Part 4: stability()/allowMultiple() 精确表（实测锁定）----
    static void testStabilityAndAllowMultipleTable() {
        // stability：STATELESS（内容与位置/常量池无关）/ CP_REFS（引用常量池）/ LABELS（依赖码内 label 位置）
        check(Attributes.deprecated().stability() == AttributeMapper.AttributeStability.STATELESS, "deprecated STATELESS");
        check(Attributes.synthetic().stability() == AttributeMapper.AttributeStability.STATELESS, "synthetic STATELESS");
        check(Attributes.sourceFile().stability() == AttributeMapper.AttributeStability.CP_REFS, "sourceFile CP_REFS");
        check(Attributes.signature().stability() == AttributeMapper.AttributeStability.CP_REFS, "signature CP_REFS");
        check(Attributes.code().stability() == AttributeMapper.AttributeStability.CP_REFS, "code CP_REFS");
        check(Attributes.innerClasses().stability() == AttributeMapper.AttributeStability.CP_REFS, "innerClasses CP_REFS");
        check(Attributes.nestHost().stability() == AttributeMapper.AttributeStability.CP_REFS, "nestHost CP_REFS");
        check(Attributes.enclosingMethod().stability() == AttributeMapper.AttributeStability.CP_REFS, "enclosingMethod CP_REFS");
        check(Attributes.exceptions().stability() == AttributeMapper.AttributeStability.CP_REFS, "exceptions CP_REFS");
        check(Attributes.constantValue().stability() == AttributeMapper.AttributeStability.CP_REFS, "constantValue CP_REFS");
        check(Attributes.record().stability() == AttributeMapper.AttributeStability.CP_REFS, "record CP_REFS");
        check(Attributes.permittedSubclasses().stability() == AttributeMapper.AttributeStability.CP_REFS, "permittedSubclasses CP_REFS");
        check(Attributes.module().stability() == AttributeMapper.AttributeStability.CP_REFS, "module CP_REFS");
        check(Attributes.runtimeVisibleAnnotations().stability() == AttributeMapper.AttributeStability.CP_REFS, "runtimeVisibleAnnotations CP_REFS");
        check(Attributes.stackMapTable().stability() == AttributeMapper.AttributeStability.LABELS, "stackMapTable LABELS");
        check(Attributes.lineNumberTable().stability() == AttributeMapper.AttributeStability.LABELS, "lineNumberTable LABELS");
        check(Attributes.localVariableTable().stability() == AttributeMapper.AttributeStability.LABELS, "localVariableTable LABELS");
        check(Attributes.localVariableTypeTable().stability() == AttributeMapper.AttributeStability.LABELS, "localVariableTypeTable LABELS");
        check(Attributes.characterRangeTable().stability() == AttributeMapper.AttributeStability.LABELS, "characterRangeTable LABELS");
        check(Attributes.moduleResolution().stability() == AttributeMapper.AttributeStability.STATELESS, "moduleResolution STATELESS");
        check(Attributes.sourceDebugExtension().stability() == AttributeMapper.AttributeStability.STATELESS, "sourceDebugExtension STATELESS");
        // 唯二 UNSTABLE：TypeAnnotation 族两个 mapper（引用码内偏移，码移动即失稳——实测）
        check(Attributes.runtimeVisibleTypeAnnotations().stability() == AttributeMapper.AttributeStability.UNSTABLE, "runtimeVisibleTypeAnnotations UNSTABLE");
        check(Attributes.runtimeInvisibleTypeAnnotations().stability() == AttributeMapper.AttributeStability.UNSTABLE, "runtimeInvisibleTypeAnnotations UNSTABLE");
        // 标准属性 stability 全部落在真实四值闭域 {STATELESS,CP_REFS,LABELS,UNSTABLE}，且无 UNKNOWN（实测）
        for (String[] row : REGISTRY) {
            AttributeMapper<?> m;
            try { m = (AttributeMapper<?>) Attributes.class.getMethod(row[0]).invoke(null); }
            catch (Exception e) { throw new AssertionError(e); }
            boolean inDomain = m.stability() == AttributeMapper.AttributeStability.STATELESS
                    || m.stability() == AttributeMapper.AttributeStability.CP_REFS
                    || m.stability() == AttributeMapper.AttributeStability.LABELS
                    || m.stability() == AttributeMapper.AttributeStability.UNSTABLE;
            check(inDomain && m.stability() != AttributeMapper.AttributeStability.UNKNOWN,
                    "standard stability in {STATELESS,CP_REFS,LABELS,UNSTABLE} and never UNKNOWN: " + row[0] + "=" + m.stability());
        }
        // allowMultiple 恰 6 个 true（实测）：synthetic/deprecated/characterRangeTable/lineNumberTable/localVariableTable/localVariableTypeTable
        int multiCount = 0;
        for (String[] row : REGISTRY) {
            AttributeMapper<?> m;
            try { m = (AttributeMapper<?>) Attributes.class.getMethod(row[0]).invoke(null); }
            catch (Exception e) { throw new AssertionError(e); }
            if (m.allowMultiple()) multiCount++;
        }
        check(multiCount == 6, "exactly 6 standard mappers allow multiple: " + multiCount);
        check(Attributes.deprecated().allowMultiple(), "deprecated allowMultiple true");
        check(Attributes.synthetic().allowMultiple(), "synthetic allowMultiple true");
        check(Attributes.lineNumberTable().allowMultiple(), "lineNumberTable allowMultiple true");
        check(Attributes.localVariableTable().allowMultiple(), "localVariableTable allowMultiple true");
        check(Attributes.localVariableTypeTable().allowMultiple(), "localVariableTypeTable allowMultiple true");
        check(Attributes.characterRangeTable().allowMultiple(), "characterRangeTable allowMultiple true");
        check(!Attributes.sourceFile().allowMultiple(), "sourceFile allowMultiple false");
        check(!Attributes.code().allowMultiple(), "code allowMultiple false");
        check(!Attributes.stackMapTable().allowMultiple(), "stackMapTable allowMultiple false");
        check(!Attributes.signature().allowMultiple(), "signature allowMultiple false");
        check(!Attributes.constantValue().allowMultiple(), "constantValue allowMultiple false");
        check(!Attributes.exceptions().allowMultiple(), "exceptions allowMultiple false");
        check(!Attributes.innerClasses().allowMultiple(), "innerClasses allowMultiple false");
    }

    // ---- Part 5: 自造 mapper 的接口面（readAttribute/writeAttribute 白盒 + allowMultiple 默认 false）----
    static void testCustomMapperInterface() throws Exception {
        check(declares(AttributeMapper.class, "readAttribute"), "declares readAttribute");
        check(declares(AttributeMapper.class, "writeAttribute"), "declares writeAttribute");
        check(declares(AttributeMapper.class, "stability"), "declares stability");
        check(declares(AttributeMapper.class, "allowMultiple"), "declares allowMultiple (default)");
        check(AttributeMapper.class.getMethod("allowMultiple").isDefault(), "allowMultiple is default method");
        check(!AttributeMapper.class.getMethod("stability").isDefault(), "stability abstract");
        check(!AttributeMapper.class.getMethod("name").isDefault(), "name abstract");
        // 自造 mapper 不覆写 allowMultiple → 默认 false
        AttributeMapper<CA> minimal = new AttributeMapper<>() {
            public String name() { return "MinAttr"; }
            public CA readAttribute(AttributedElement e, ClassReader cr, int p) { return new CA(this); }
            public void writeAttribute(BufWriter bw, CA a) { bw.writeIndex(bw.constantPool().utf8Entry("MinAttr")); bw.writeInt(0); }
            public AttributeStability stability() { return AttributeMapper.AttributeStability.STATELESS; }
        };
        check(!minimal.allowMultiple(), "custom mapper default allowMultiple false");
        check(minimal.stability() == AttributeMapper.AttributeStability.STATELESS, "custom mapper stability passthrough");
        check(minimal.name().equals("MinAttr"), "custom mapper name");
        // AttributeMapper 可作为 findAttribute 的键（mapper 即注册表键）
        ClassFile regCf = ClassFile.of(ClassFile.AttributeMapperOption.of(u -> u.equalsString("MinAttr") ? minimal : null));
        byte[] b = regCf.build(ClassDesc.of("AMC"), cb -> cb.with(new CA(minimal)));
        ClassModel cm = regCf.parse(b);
        check(cm.findAttribute(minimal).isPresent(), "custom mapper usable as findAttribute key");
        check(cm.findAttribute(minimal).get().attributeMapper() == minimal, "roundtrip mapper identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFullRegistry();
        testStabilityAndAllowMultipleTable();
        testCustomMapperInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
