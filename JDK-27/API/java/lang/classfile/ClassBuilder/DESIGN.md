# java.lang.classfile.ClassBuilder — 设计案

## 1. API 功能
`java.lang.classfile.ClassBuilder`（interface）。工厂/静态方法：(none)。查询/实例方法：withVersion, withFlags, withSuperclass, withInterfaces, withInterfaceSymbols, withField, transformField, withMethod, withMethodBody, transformMethod。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ClassBuilderTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassBuilder` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ClassBuilderTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassBuilder")` + public/interface/继承 + 关键成员。黑盒（`ClassBuilderTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 4.1 深化（契约覆盖版，59 断言）

- `testChannelMatrix()`：全部通道方法矩阵——withVersion(major,minor) 双参回读 / withFlags(int+AccessFlag... 双重载) / withSuperclass(ClassDesc) 与 withSuperclass(ClassEntry) 等效 / withInterfaceSymbols(List) 与 varargs / withInterfaces(ClassEntry...) / withField 的 4 重载（String+desc / Utf8Entry+desc / String+Utf8Entry / 全条目）/ withMethod 与 withMethodBody / withMethodBody(Utf8Entry 名重载) / with(ClassFileVersion 元素) / with(Interfaces 元素重注入叠加) ；fields()/methods()/elementList() 顺序保持断言。
- `testNegativeMatrix()`：负例——withVersion/withFlags/withSuperclass(null)/withField(null,...)/withMethod(null) 等 6 个 NPE 精确消息；withMethodBody 空 body（不发射 return）→ build 抛 IAE，消息含 "Code length 0"。
- `testDuplicatesAndTransform()`：同名字段/同签名方法在 builder 层允许（不查重，fields().size()==2，verify 拒绝但 build 放行）；transformClass 回调收 ClassBuilder、constantPool() 非 null、endHandler 链；transformField/transformMethod 回调通道。
- `testDefineClassLoop()`：build 从零 → `MethodHandles.lookup().defineClass` → 反射调用运行闭环（DynCB go()==1、add(20,22)==42）。

实测锁定契约事实：
1. builder 不查重：同名同描述符字段/方法可重复注入，build 成功（结构层放行），仅 verify 报错。
2. withMethodBody 的 body 必须发射终止指令（return/athrow），否则 build 抛 IAE 且消息含 "Code length 0"。
3. withSuperclass/withInterfaces 的 ClassDesc 与 Entry 重载产物逐字节等价；Interfaces 元素可多次叠加。
4. defineClass 要求生成类名与 lookup 类同包（默认包测试类 → 类名不带点号），静态方法需 ACC_PUBLIC|ACC_STATIC 才能反射 getMethod。
