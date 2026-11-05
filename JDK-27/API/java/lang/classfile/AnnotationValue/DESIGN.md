# java.lang.classfile.AnnotationValue — 设计案

## 1. API 功能
`java.lang.classfile.AnnotationValue`（interface）。工厂/静态方法：ofEnum, ofClass, ofString, ofDouble, ofFloat, ofLong, ofInt, ofShort。查询/实例方法：tag。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `AnnotationValueTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.AnnotationValue` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`AnnotationValueTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.AnnotationValue")` + public/interface/继承 + 关键成员。黑盒（`AnnotationValueTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 契约深化补遗（2026-11-05，20→84 断言）

- `of(ConstantDesc)` 统一工厂分派矩阵：Integer/Long/Byte/Short/Character/Boolean/Float/Double/String→对应常量形态，ClassDesc→OfClass（10 种接受）；null→NPE；Object/MethodTypeDesc/EnumDesc→**IllegalArgumentException（消息 `Illegal annotation constant value type ...`）**——枚举必须用 ofEnum 两段式，不能用 EnumDesc 走 of。
- 13 形态 tag 字符表：B=66/C=67/D=68/F=70/I=73/J=74/S=83/Z=90/s=115/e=101/c=99/@=64/[=91（`(char) tag()` 字符语义逐个锁定）。
- 层次谱系实测：**OfString 属 OfConstant**（反直觉，字符串形态是常量形态）；OfEnum/OfClass/OfArray/OfAnnotation **不属** OfConstant（javac 对这四者的 `instanceof OfConstant` 表达式直接编译报错，只能 `OfConstant.class.isInstance` 判定）；13 形态全部 instanceof AnnotationValue。
- 池条目重载矩阵：ofInt/ofLong/ofFloat/ofDouble/ofString(条目) 的 `constant() == 原池条目`（同一性复用）；B/S/C/Z 四形态共享同一 IntegerEntry（常量池只有 int 槽，语义区分在 tag）。
- 全 13 形态混合数组真序列化往返：re-parse 后 tag 序列精确保序 `IsZJBSCFDce@[`，末位空数组嵌套、数组内 enum 两段均回读。
- **equals 语义实测（IEEE `==` 语义，非 bits）**：`ofFloat(NaN).equals(ofFloat(NaN))` **false** 且 `ofDouble(NaN)` 同（但 hashCode 仍一致）；`ofFloat(-0.0f).equals(ofFloat(0.0f))` **true**（ofDouble 同）；int/long、byte/short 跨形态不 equals。
- toString 稳定位：OfString 含值、OfEnum 含常量名+描述符、OfClass 含描述符、OfInt 形如 `OfIntImpl[constant=...]`（记录形态）。
- javac 真类产物：嵌套 @AVAnno(v default 7) + @AVAnno(v=3) 字段 → RuntimeVisibleAnnotations OfInt=3 + AnnotationDefault v()==7。
- 本 JDK 构建 quirk：java.lang.constant 体系若以 `ClassDesc.ofDescriptor("I")` 为第一触点会循环初始化失败（见 OfClass 条目）；本文件用 `static final ClassDesc CD_WARM = ConstantDescs.CD_Object` 先完整初始化规避。
