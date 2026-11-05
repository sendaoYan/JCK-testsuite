# java.lang.classfile.ClassFileElement — 设计案

## 1. API 功能
`java.lang.classfile.ClassFileElement`（**空标记接口**，无任何方法）。四个直接子标记：`ClassElement`、`MethodElement`、`FieldElement`、`CodeElement`；`CompoundElement<E extends ClassFileElement>` 也继承它。一切可出现在 classfile 元素树的类型都实现本接口。

## 2. Java 用户如何使用
作为 transform 谓词/收集通道的类型上限（`ClassTransform.accept(ClassBuilder, ClassElement)` 内部即以本族为币）；判定某对象"是不是元素"用 instanceof。注意：`Label` 的实现对象（newLabel() 返回值）实测同时实现 LabelTarget→CodeElement→ClassFileElement（**不能当负例用**）。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.ClassFileElement` 及全部子接口。
- OpenJDK `test/jdk/jdk/classfile/`（所有 transform 用例）。
- 本机实测：正例家族=ClassFileVersion/Superclass/Interfaces/AccessFlags/四个模型/属性（对应层级）/指令/伪指令/CustomAttribute；负例=Opcode/ClassDesc/String；LabelImpl 实现双接口（Label+LabelTarget）。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：ClassFileVersion/DeprecatedAttribute 两个正例）。
- `testFamilyMatrix`（深化）：**正例矩阵**——类级元素（ClassFileVersion/Superclass/Interfaces/Deprecated/SourceFile/CustomAttribute）、四个模型（ClassModel/FieldModel/MethodModel/CodeModel）、三级 AccessFlags（class/field/method flags()）、ConstantValue 属性、Instruction、PseudoInstruction；**四层 elementList 全员 instanceof**（class/field/method/code 各自循环）；子接口继承关系（isAssignableFrom 四连）；**负例**（Opcode/ClassDesc/String NOT ClassFileElement）；空标记接口断言 `getDeclaredMethods().length==0`。
- `testElementPassThrough`（深化）：以 ClassFileElement 通道 forEach 收齐元素 → 计数==elementList().size()；ACCEPT_ALL transform 元素数守恒 + verify 干净。
- 易错点：`instanceof` 对不可能的转换（如 SourceFileAttribute instanceof MethodElement）是**编译错误**，负例必须走 `Class.isInstance` 运行时通道；Label 实例是隐藏正例。
