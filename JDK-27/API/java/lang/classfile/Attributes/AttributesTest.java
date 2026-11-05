/*
 * @test
 * @summary java.lang.classfile.Attributes conformance: white-box structure + black-box behavior.
 * @run main AttributesTest
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

public class AttributesTest {
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
        Class<?> c = Class.forName("java.lang.classfile.Attributes");
        check(c != null, "java.lang.classfile.Attributes loads");
        check(c.getName().equals("java.lang.classfile.Attributes"), "binary name java.lang.classfile.Attributes");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.Attributes is public");
        check(!c.isInterface() && !c.isEnum(), "java.lang.classfile.Attributes is a class");
        check(declares(c, "annotationDefault"), "declares annotationDefault");
        check(declares(c, "bootstrapMethods"), "declares bootstrapMethods");
        check(declares(c, "characterRangeTable"), "declares characterRangeTable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(Attributes.NAME_CODE.equals("Code"), "NAME_CODE");
        check(Attributes.NAME_SOURCE_FILE.equals("SourceFile"), "NAME_SOURCE_FILE");
        check(Attributes.NAME_SIGNATURE.equals("Signature"), "NAME_SIGNATURE");
        check(Attributes.NAME_CONSTANT_VALUE.equals("ConstantValue"), "NAME_CONSTANT_VALUE");
        check(Attributes.NAME_STACK_MAP_TABLE.equals("StackMapTable"), "NAME_STACK_MAP_TABLE");
        check(Attributes.code().name().equals("Code"), "code mapper name");
        check(Attributes.sourceFile().name().equals("SourceFile"), "sourceFile mapper");
        check(Attributes.signature().name().equals("Signature"), "signature mapper");
        check(Attributes.constantValue().name().equals("ConstantValue"), "constantValue mapper");
        check(Attributes.synthetic().name().equals("Synthetic"), "synthetic mapper");
        check(Attributes.stackMapTable().name().equals("StackMapTable"), "stackMapTable mapper");
        check(Attributes.sourceId().name().equals("SourceID"), "sourceId mapper");
        check(Attributes.compilationId().name().equals("CompilationID"), "compilationId mapper");

    }

    // ---- Part 3: NAME_* 常量与 JVM 规范属性名逐字全等表（36 个全覆盖，javap 锁定）----
    static void testNameConstantTable() throws Exception {
        String[][] names = {
                {"NAME_ANNOTATION_DEFAULT", "AnnotationDefault"},
                {"NAME_BOOTSTRAP_METHODS", "BootstrapMethods"},
                {"NAME_CHARACTER_RANGE_TABLE", "CharacterRangeTable"},
                {"NAME_CODE", "Code"},
                {"NAME_COMPILATION_ID", "CompilationID"},
                {"NAME_CONSTANT_VALUE", "ConstantValue"},
                {"NAME_DEPRECATED", "Deprecated"},
                {"NAME_ENCLOSING_METHOD", "EnclosingMethod"},
                {"NAME_EXCEPTIONS", "Exceptions"},
                {"NAME_INNER_CLASSES", "InnerClasses"},
                {"NAME_LINE_NUMBER_TABLE", "LineNumberTable"},
                {"NAME_LOCAL_VARIABLE_TABLE", "LocalVariableTable"},
                {"NAME_LOCAL_VARIABLE_TYPE_TABLE", "LocalVariableTypeTable"},
                {"NAME_METHOD_PARAMETERS", "MethodParameters"},
                {"NAME_MODULE", "Module"},
                {"NAME_MODULE_HASHES", "ModuleHashes"},
                {"NAME_MODULE_MAIN_CLASS", "ModuleMainClass"},
                {"NAME_MODULE_PACKAGES", "ModulePackages"},
                {"NAME_MODULE_RESOLUTION", "ModuleResolution"},
                {"NAME_MODULE_TARGET", "ModuleTarget"},
                {"NAME_NEST_HOST", "NestHost"},
                {"NAME_NEST_MEMBERS", "NestMembers"},
                {"NAME_PERMITTED_SUBCLASSES", "PermittedSubclasses"},
                {"NAME_RECORD", "Record"},
                {"NAME_RUNTIME_INVISIBLE_ANNOTATIONS", "RuntimeInvisibleAnnotations"},
                {"NAME_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS", "RuntimeInvisibleParameterAnnotations"},
                {"NAME_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS", "RuntimeInvisibleTypeAnnotations"},
                {"NAME_RUNTIME_VISIBLE_ANNOTATIONS", "RuntimeVisibleAnnotations"},
                {"NAME_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS", "RuntimeVisibleParameterAnnotations"},
                {"NAME_RUNTIME_VISIBLE_TYPE_ANNOTATIONS", "RuntimeVisibleTypeAnnotations"},
                {"NAME_SIGNATURE", "Signature"},
                {"NAME_SOURCE_DEBUG_EXTENSION", "SourceDebugExtension"},
                {"NAME_SOURCE_FILE", "SourceFile"},
                {"NAME_SOURCE_ID", "SourceID"},
                {"NAME_STACK_MAP_TABLE", "StackMapTable"},
                {"NAME_SYNTHETIC", "Synthetic"},
        };
        check(names.length == 36, "36 NAME_* constants: " + names.length);
        for (String[] row : names)
            check(((String) Attributes.class.getField(row[0]).get(null)).equals(row[1]), row[0] + " == \"" + row[1] + "\"");
        // 常量字段完备性：恰 36 个 NAME_ String 字段（无遗漏/无多余）
        int cnt = 0;
        for (java.lang.reflect.Field f : Attributes.class.getDeclaredFields())
            if (f.getName().startsWith("NAME_") && f.getType() == String.class) cnt++;
        check(cnt == 36, "exactly 36 NAME_ fields via reflection: " + cnt);
    }

    // ---- Part 4: 全部 36 个 mapper 工厂非 null + name() 与 NAME_* 对应常量全等 + 重复调用同一实例 ----
    static void testMapperRegistryIdentity() throws Exception {
        String[][] pairs = {
                {"annotationDefault", "NAME_ANNOTATION_DEFAULT"},
                {"bootstrapMethods", "NAME_BOOTSTRAP_METHODS"},
                {"characterRangeTable", "NAME_CHARACTER_RANGE_TABLE"},
                {"code", "NAME_CODE"},
                {"compilationId", "NAME_COMPILATION_ID"},
                {"constantValue", "NAME_CONSTANT_VALUE"},
                {"deprecated", "NAME_DEPRECATED"},
                {"enclosingMethod", "NAME_ENCLOSING_METHOD"},
                {"exceptions", "NAME_EXCEPTIONS"},
                {"innerClasses", "NAME_INNER_CLASSES"},
                {"lineNumberTable", "NAME_LINE_NUMBER_TABLE"},
                {"localVariableTable", "NAME_LOCAL_VARIABLE_TABLE"},
                {"localVariableTypeTable", "NAME_LOCAL_VARIABLE_TYPE_TABLE"},
                {"methodParameters", "NAME_METHOD_PARAMETERS"},
                {"module", "NAME_MODULE"},
                {"moduleHashes", "NAME_MODULE_HASHES"},
                {"moduleMainClass", "NAME_MODULE_MAIN_CLASS"},
                {"modulePackages", "NAME_MODULE_PACKAGES"},
                {"moduleResolution", "NAME_MODULE_RESOLUTION"},
                {"moduleTarget", "NAME_MODULE_TARGET"},
                {"nestHost", "NAME_NEST_HOST"},
                {"nestMembers", "NAME_NEST_MEMBERS"},
                {"permittedSubclasses", "NAME_PERMITTED_SUBCLASSES"},
                {"record", "NAME_RECORD"},
                {"runtimeInvisibleAnnotations", "NAME_RUNTIME_INVISIBLE_ANNOTATIONS"},
                {"runtimeInvisibleParameterAnnotations", "NAME_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS"},
                {"runtimeInvisibleTypeAnnotations", "NAME_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS"},
                {"runtimeVisibleAnnotations", "NAME_RUNTIME_VISIBLE_ANNOTATIONS"},
                {"runtimeVisibleParameterAnnotations", "NAME_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS"},
                {"runtimeVisibleTypeAnnotations", "NAME_RUNTIME_VISIBLE_TYPE_ANNOTATIONS"},
                {"signature", "NAME_SIGNATURE"},
                {"sourceDebugExtension", "NAME_SOURCE_DEBUG_EXTENSION"},
                {"sourceFile", "NAME_SOURCE_FILE"},
                {"sourceId", "NAME_SOURCE_ID"},
                {"stackMapTable", "NAME_STACK_MAP_TABLE"},
                {"synthetic", "NAME_SYNTHETIC"},
        };
        check(pairs.length == 36, "36 mapper/constant pairs: " + pairs.length);
        for (String[] row : pairs) {
            AttributeMapper<?> m = (AttributeMapper<?>) Attributes.class.getMethod(row[0]).invoke(null);
            String cname = (String) Attributes.class.getField(row[1]).get(null);
            check(m != null, "mapper non-null: " + row[0]);
            check(m.name().equals(cname), row[0] + "().name() == " + row[1] + ": " + m.name());
            check(Attributes.class.getMethod(row[0]).invoke(null) == m, "repeat " + row[0] + "() same instance (==)");
        }
        // mapper 方法计数恰 36（javap 锁定，无遗漏）
        int methods = 0;
        for (Method mth : Attributes.class.getDeclaredMethods())
            if (Modifier.isStatic(mth.getModifiers()) && mth.getReturnType() == AttributeMapper.class && mth.getParameterCount() == 0) methods++;
        check(methods == 36, "exactly 36 mapper factory methods: " + methods);
    }

    // ---- Part 5: 注册表 mapper 与真实属性闭环（SourceFile/Deprecated build→parse 回读用同一 mapper 键取回）----
    static void testRegistryRoundtrip() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("ATR"), cb -> cb
                .with(SourceFileAttribute.of("ATR.java"))
                .with(DeprecatedAttribute.of())
                .withField("x", ConstantDescs.CD_int, fb -> fb.with(ConstantValueAttribute.of(3))));
        ClassModel cm = cf.parse(b);
        check(cm.findAttribute(Attributes.sourceFile()).isPresent(), "sourceFile mapper as lookup key");
        check(cm.findAttribute(Attributes.sourceFile()).get().attributeMapper() == Attributes.sourceFile(), "parsed attr mapper identity sourceFile");
        check(cm.findAttribute(Attributes.deprecated()).get().attributeMapper() == Attributes.deprecated(), "parsed attr mapper identity deprecated");
        check(cm.fields().get(0).findAttribute(Attributes.constantValue()).get().attributeMapper() == Attributes.constantValue(), "parsed attr mapper identity constantValue");
        // Attributes 不可实例化（私有构造）——注册表是纯静态面
        check(!Modifier.isPublic(Attributes.class.getDeclaredConstructors()[0].getModifiers()), "Attributes constructor non-public");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNameConstantTable();
        testMapperRegistryIdentity();
        testRegistryRoundtrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
