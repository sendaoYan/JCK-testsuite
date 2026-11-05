# java.lang.classfile.MethodElement — 设计案

## 1. API 功能
`java.lang.classfile.MethodElement`（空标记接口，extends `ClassFileElement`）。实现者（实测分类）：`CodeModel`、方法级 `AccessFlags`、`DeprecatedAttribute`、`ExceptionsAttribute`、`SignatureAttribute`、`MethodParametersAttribute`、`RuntimeVisibleAnnotationsAttribute`、`CustomAttribute`。**非**实现者：`MethodModel`（本身是 **ClassElement**，其元素才是 MethodElement）、`SourceFileAttribute`、`ConstantValueAttribute`、`Superclass`、`ClassFileVersion`、`Interfaces`、`FieldModel`、指令/伪指令。

## 2. Java 用户如何使用
`MethodModel.elementList()` 的元素类型（实测直方图：`[AccessFlags, CodeModel]`，有 code 的方法恰好两元素；abstract/native 方法仅 `[AccessFlags]`）；`MethodTransform`/`mb.with(MethodElement)` 的元素币种。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.MethodElement` 及属性接口 extends 子句。
- OpenJDK `test/jdk/jdk/classfile/`（transform 用例）。
- 本机实测：MethodModel instanceof MethodElement == **false**（它是 ClassElement）；CodeModel instanceof MethodElement == true；方法 elementList=[AccessFlags, CodeModel]。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：CodeModel/DeprecatedAttribute 两正例）。
- `testPositiveFamily`（深化）：工厂产物（Deprecated/Exceptions/Signature/**MethodParameters**/CustomAttribute）+ 模型产物（CodeModel/方法 AccessFlags）+ 方法 elementList 直方图 [AccessFlags=1, CodeModel=1] + 全员非空循环 + **真类 bound 属性**（depMethod 的 Deprecated、throwsM 的 Exceptions、genericM 的 Signature 均 MethodElement；SourceFile NOT）。
- `testNegativeFamily`（深化）：Superclass/ClassFileVersion/Interfaces/SourceFile/ConstantValue/Instruction/LineNumber NOT MethodElement；FieldModel NOT；**MethodModel NOT MethodElement（实测，易错）**。
- `testTransformChannel`（深化）：DynMethodElement+seq → ClassElement 谓词按方法名过滤 → kept==1 → defineClass → go()==2 运行闭环 + verify 干净。
- 易错点：**MethodModel ≠ MethodElement**（泛型参数是 MethodElement 不代表自身实现它）；负例 instanceof 部分编译不过（如 SourceFileAttribute instanceof MethodElement），须用 `Class.isInstance`。
