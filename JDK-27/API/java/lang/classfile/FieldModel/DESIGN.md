# java.lang.classfile.FieldModel — 设计案

## 1. API 功能
`java.lang.classfile.FieldModel`（interface，extends `CompoundElement<FieldElement>` + `AttributedElement` + **`ClassElement`**）。实例方法：`flags()`、`parent()`（Optional&lt;ClassModel&gt;）、`fieldName()`/`fieldType()`（Utf8Entry）、`fieldTypeSymbol()`（default，ClassDesc）。

## 2. Java 用户如何使用
`ClassFile.of().parse(bytes).fields()` 按声明序取得；`fieldType().stringValue()` 是擦除后描述符（泛型字段为 `Ljava/util/List;` 而非签名形式）；常量值经 `findAttribute(Attributes.constantValue())` 的 `constant()`（ConstantValueEntry，按类型 cast：int/boolean→IntegerEntry、long→LongEntry、String→StringEntry）。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.FieldModel`。
- OpenJDK `test/jdk/jdk/classfile/`（ClassBuildingTest、AttributesTest）。
- 本机实测：CONST=42 ConstantValue 回读、long/boolean/String 常量分型、gfield Signature、elementList=[AccessFlags]（无属性字段）/[AccessFlags, ConstantValue]（有属性）。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：build→parse name/descriptor/typeSymbol/flags/parent/elements）。
- `testSelfFields`（深化）：真类字段族全断言 —— `static final int CONST=42`→`((IntegerEntry)cv.constant()).intValue()==42`、flagsMask==0x0018（STATIC|FINAL）、location()==FIELD；`long`→LongEntry.longValue()、`boolean`→IntegerEntry 1、`String`→StringEntry.stringValue()；泛型字段 Signature=="Ljava/util/List&lt;Ljava/lang/String;&gt;;" 且 fieldType=="Ljava/util/List;"（**擦除**）；@Deprecated 字段双属性；volatile 字段 flag；elementList instanceof 计数；parent 身份。
- `testFieldShapes`（深化）：无属性字段 elementList 恰 [AccessFlags]；`fb.withFlags(int)` + `with(ConstantValueAttribute.of(7))` 注入回读；`SignatureAttribute.of(Signature.parseFrom(...))` 回读。
- `testDefineClassLoop`（深化）：DynFieldModel+seq 带常量 99 字段 + getstatic 方法 → defineClass → 反射 CV==99、get()==99 运行闭环。
- 易错点：**boolean 常量在 ConstantValue 里是 IntegerEntry(1)**；泛型字段 fieldType 是擦除描述符、Signature 才含类型实参；`withField(String,ClassDesc,int,Consumer)` 重载**不存在**——flags 与回调分两套（`withField(name,desc,fb->fb.withFlags(...).with(attr))`）。
