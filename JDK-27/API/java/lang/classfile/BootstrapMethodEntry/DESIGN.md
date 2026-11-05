# java.lang.classfile.BootstrapMethodEntry — 设计案

## 1. API 功能
`java.lang.classfile.BootstrapMethodEntry`（interface）。工厂/静态方法：(none)。查询/实例方法：constantPool, bsmIndex, bootstrapMethod, arguments。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `BootstrapMethodEntryTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.BootstrapMethodEntry` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`BootstrapMethodEntryTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.BootstrapMethodEntry")` + public/interface/继承 + 关键成员。黑盒（`BootstrapMethodEntryTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 4.1 深化（契约覆盖版，80 断言）

- `testLambdaRealClass()`：解析本测试类自身字节码（javac 产物）——BootstrapMethodsAttribute 存在且 bootstrapMethodsSize()==list.size()；LambdaMetafactory 条目：handle 方法名 metafactory、kind==6（REF_invokeStatic）、3 个静态参数类型表 [MethodTypeEntry(sam), MethodHandleEntry(impl), MethodTypeEntry(instantiated)]、args 均为 LoadableConstantEntry、bsmIndex>=0、constantPool()==model 池；StringConcatFactory 条目（字符串 + 拼接）同在；InvokeDynamicInstruction 联动（name/typeSymbol/bootstrapArgs/owner）。
- `testBsmEntryBuilder()`：ConstantPoolBuilder.bsmEntry 双重载——同 desc+args 去重（==，bsmIndex 相同）；List.of("a","b") ConstantDesc 参数自动转池条目（StringEntry 回读、绑定同一池）；bsmEntry(MethodHandleEntry, List) 重载；bootstrapMethod() 的 reference 面（owner/name/MethodRefEntry）；池身份。
- `testInvokeDynamicInjection()`：build 注入 invokedynamic(DynamicCallSiteDesc) → BootstrapMethodsAttribute 自动生成（无需手动 with）：1 条目、3 args、bsmIndex 0、owner==LambdaMetafactory，verify 干净；指令↔条目联动——InvokeDynamicInstruction.invokedynamic().bootstrapMethodIndex()==attr 条目 bsmIndex()；InvokeDynamicEntry.bootstrap()/nameAndType() 链；build 回调内 cb.constantPool().bsmEntry(...) 可用；BootstrapMethodsAttribute 无公开 of() 工厂（实测遍历 getMethods 断言）。

实测锁定契约事实：
1. BootstrapMethodsAttribute 只能由 build 自动生成或从既有字节码解析获得，公开 API 无 of 工厂（与大多数标准属性不同）。
2. BSM 条目去重键 = (MethodHandle desc, 参数表)；参数以 LoadableConstantEntry 形态挂池。
3. InvokeDynamicInstruction 无 bsmIndex() 访问器，须经 invokedynamic()（InvokeDynamicEntry）→ bootstrapMethodIndex()；InvokeDynamicEntry 继承 DynamicConstantPoolEntry，另有 bootstrap()/nameAndType() 面。
4. javac 对 lambda 与字符串 + 拼接分别注入 LambdaMetafactory.metafactory 与 StringConcatFactory.makeConcat* 两族 BSM。
