# java.lang.classfile.ClassModel — 设计案

## 1. API 功能
`java.lang.classfile.ClassModel`（interface，extends `CompoundElement<ClassElement>` + `AttributedElement`）。工厂/静态方法：无。实例方法：`constantPool()`、`flags()`（AccessFlags）、`thisClass()`（ClassEntry）、`majorVersion()`/`minorVersion()`、`fields()`（List&lt;FieldModel&gt;）、`methods()`（List&lt;MethodModel&gt;）、`superclass()`（Optional&lt;ClassEntry&gt;：Object.class 与 module-info 为 empty）、`interfaces()`（List&lt;ClassEntry&gt;）、`isModuleInfo()`。继承自 CompoundElement：`elementList()`/`elementStream()`/`iterator()`/`forEach()`/`toDebugString()`；继承自 AttributedElement：`attributes()`/`findAttribute(mapper)`。

## 2. Java 用户如何使用
`ClassFile.of().parse(byte[])` 得 ClassModel（javac 产物或 build 产物均可）；逐访问器查询类头/成员/属性；`elementList()` 按文件顺序给出全部 ClassElement（直方图实测：`ClassFileVersion、AccessFlags、Superclass、Interfaces、FieldModel×N、MethodModel×M、SourceFile/NestMembers/InnerClasses 属性`）。parse 负例：null→NPE（消息含 classfileBytes）、空/坏魔数→IAE "Bad magic number"、正确魔数截断→IAE。

## 3. 参考来源
- JDK 27 javadoc（java.lang.classfile，JDK 24 起 non-preview）+ 本机 `javap --module java.base java.lang.classfile.ClassModel`。
- OpenJDK `test/jdk/jdk/classfile/`（ClassBuildingTest、ModuleBuilderTest、AttributesTest）。
- 本机实测探针（JDK-27.0.1）：elementList 直方图、SourceFile 值、Object.class/module-info superclass empty、defineClass 闭环。

## 4. 测试用例设计 + 易错规范细节
- 白盒 `testWhiteBox`：类加载/public/interface/关键成员声明。
- 黑盒 `testBlackBox`（原有保留）：build→parse 往返、flags/interfaces/fields/methods/elementList/toDebugString。
- `testSelfParse`（深化）：**parse 自身 .class 字节**（`getResourceAsStream("ClassModelTest.class")`）——thisClass 内名全等、asSymbol==ClassDesc、superclass==java/lang/Object（descriptor `Ljava/lang/Object;`）、major==71/minor==0（javac 27 目标）、flags has PUBLIC 且 location()==CLASS、isModuleInfo false、constantPool().size()>0、interfaces 空；**elementList instanceof 计数（contains 计数而非脆弱全等）**：ClassFileVersion/AccessFlags/Superclass/Interfaces/SourceFile 各恰 1，FieldModel==fields().size()，MethodModel==methods().size()；`findAttribute(Attributes.sourceFile())` 值=="ClassModelTest.java" 且 attributeName==Attributes.NAME_SOURCE_FILE；每方法 `parent()==cm`（身份）、每字段同；verify 干净；for-each 计数==elementList().size()。
- `testBuildFullReadback`（深化）：build 带 superclass=Exception、2 接口、2 字段（含 ConstantValue 42）、2 方法（含 abstract）→ 全量回读（接口顺序精确）；嵌套 implements 类解析（interfaces 精确顺序含 `ClassModelTest$NestedIf`）；**Object.class superclass empty**；**buildModule 产物 superclass empty + isModuleInfo true**；坏 bytes 反例（空数组 IAE "Bad magic number"、截断头 IAE）。
- `testDefineClassLoop`（深化）：动态名 DynClassModel+seq，build（含接口+静态字段+2 方法）→ `MethodHandles.lookup().defineClass` → `Runnable.class.isAssignableFrom(dc)`、go()==1、run() 可调、verify 干净。
- 易错点：**ClassFileVersion 出现在 elementList 而 major/minor 在模型顶层访问器**；self 直方图含 NestMembers/InnerClasses（测试类有嵌套类时）——用 instanceof 计数规避脆弱全等；`interfaces()` 对类是 implements 列表（不含父类）。
