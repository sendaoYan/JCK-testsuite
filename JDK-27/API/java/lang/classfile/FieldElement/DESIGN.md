# java.lang.classfile.FieldElement — 设计案

## 1. API 功能
`java.lang.classfile.FieldElement`（空标记接口，extends `ClassFileElement`）。实现者（实测分类）：字段级 `AccessFlags`、`ConstantValueAttribute`、`DeprecatedAttribute`、`SignatureAttribute`、`RuntimeVisibleAnnotationsAttribute`、`CustomAttribute`。**非**实现者：`FieldModel`（本身是 **ClassElement**）、`SourceFileAttribute`、`ExceptionsAttribute`、`Superclass`、`ClassFileVersion`、`Interfaces`、`MethodModel`、指令/伪指令。

## 2. Java 用户如何使用
`FieldModel.elementList()` 的元素类型（实测直方图：无属性字段 `[AccessFlags]`；带 ConstantValue 字段 `[AccessFlags, ConstantValueAttribute]`）；`FieldTransform`/`fb.with(FieldElement)` 的元素币种。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.FieldElement` 及属性接口 extends 子句。
- OpenJDK `test/jdk/jdk/classfile/`。
- 本机实测：FieldModel instanceof FieldElement == **false**；plain 字段 elementList==[AccessFlags]。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：ConstantValue is FieldElement、字段 elementList 成员断言）。
- `testPositiveFamily`（深化）：工厂产物（ConstantValue/Deprecated/Signature/CustomAttribute）+ 模型产物（字段 AccessFlags）+ **真类 bound 属性族**（CONST 的 ConstantValue、gfield 的 Signature、depField 的 Deprecated+RuntimeVisibleAnnotations）+ CONST elementList 直方图 [AccessFlags=1, ConstantValue=1] + 全员非空 + verify。
- `testNegativeFamily`（深化）：SourceFile/Exceptions/Superclass/ClassFileVersion/Instruction/LineNumber NOT；plain 字段 elementList 恰 [AccessFlags]；MethodModel NOT；**FieldModel NOT FieldElement（实测，易错）**。
- `testTransformChannel`（深化）：DynFieldElement+seq（静态常量字段+getstatic 方法）→ ClassElement 谓词剥全部字段 → dst.fields() 空 → defineClass（原始 bytes）→ get()==11 运行闭环。
- 易错点：**FieldModel ≠ FieldElement**（同 MethodModel 的坑）；`ConstantValueAttribute.of(int)` 直接收值。
