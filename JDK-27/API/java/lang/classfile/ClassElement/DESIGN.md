# java.lang.classfile.ClassElement — 设计案

## 1. API 功能
`java.lang.classfile.ClassElement`（空标记接口，extends `ClassFileElement`）。实现者（实测分类）：`FieldModel`、`MethodModel`（成员层面）、`ClassFileVersion`、`Superclass`、`Interfaces`、`AccessFlags`（同时实现 Class/Method/Field 三种元素）、`SourceFileAttribute`、`DeprecatedAttribute`、`SignatureAttribute`、`RuntimeVisibleAnnotationsAttribute`、`CustomAttribute`（实现全部四种元素接口）。**非**实现者：`CodeModel`（是 MethodElement）、指令/伪指令、`ExceptionsAttribute`（MethodElement 专属）、`ConstantValueAttribute`（FieldElement 专属）、`StackMapTableAttribute`（CodeElement 专属）。

## 2. Java 用户如何使用
`ClassModel.elementList()` 的元素类型；`ClassTransform.accept(ClassBuilder, ClassElement)` 的元素币种；build 侧 `cb.with(ClassElement)`。属性按其声明的层级参与（SourceFile 只在类层、Exceptions 只在方法层）。

## 3. 参考来源
- JDK 27 javadoc + `javap` 各属性接口的 extends 子句（LineNumberTable/LocalVariableTable/LocalVariableTypeTable **只** extends Attribute&lt;&gt;，不实现任何元素接口——编译器级事实）。
- OpenJDK `test/jdk/jdk/classfile/`（ClassTransform 各用例）。
- 本机实测：CodeModel instanceof ClassElement == false；StackMapTable instanceof CodeElement == true 且 ClassElement == false。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：Superclass/ClassFileVersion 两正例）。
- `testPositiveFamily`（深化）：工厂产物（Superclass/ClassFileVersion/Interfaces/Deprecated/SourceFile/Signature/CustomAttribute）+ 模型产物（FieldModel/MethodModel/class AccessFlags）+ assignable 关系 + **真类 bound 属性**（SourceFile/字段 Deprecated）+ self elementList 全员 instanceof 循环。
- `testNegativeFamily`（深化）：CodeModel NOT ClassElement（但 IS MethodElement，正确定位）；Instruction/PseudoInstruction/ExceptionsAttribute/ConstantValueAttribute NOT；code.elementList 中指令全非；StackMapTableAttribute **is CodeElement only**（双断言）。
- `testTransformChannel`（深化）：DynClassElement+seq → 以 ClassElement 谓词只保留 MethodModel 的 transform → methods==1/fields==0/superclass 保留 → defineClass → go()==5 运行闭环 + verify 干净。
- 易错点：`LineNumberTableAttribute` 等调试属性**不是任何元素**（只挂在 attributes()）；负例 instanceof 部分编译不过，用 `Class.isInstance`。
