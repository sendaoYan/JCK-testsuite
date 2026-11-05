# java.lang.classfile.CompoundElement — 设计案

## 1. API 功能
`java.lang.classfile.CompoundElement&lt;E extends ClassFileElement&gt;`（interface，extends `ClassFileElement` + **`Iterable&lt;E&gt;**`）。方法：`forEach(Consumer)`（抽象）、`iterator()`（default）、`elementStream()`（default，Stream&lt;E&gt;）、`elementList()`（default，List&lt;E&gt;）、`toDebugString()`（default，String）。四个模型实现：`ClassModel : CompoundElement<ClassElement>`、`MethodModel : CompoundElement<MethodElement>`、`FieldModel : CompoundElement<FieldElement>`、`CodeModel : CompoundElement<CodeElement>`。

## 2. Java 用户如何使用
任何模型都可直接 for-each（Iterable）；`elementList()` 给快照列表（**每次调用元素可能是新包装对象**——类型序列稳定但实例身份不保证）；`elementStream()` 支持流式过滤；`toDebugString()` 人类可读。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.CompoundElement`。
- OpenJDK `test/jdk/jdk/classfile/`（遍历形态遍布全部用例）。
- 本机实测：for-each/forEach/elementStream/elementList 四方计数一致；两次 elementList() 元素**类型相同但对象身份不同**；iterator() 每次新实例。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：elementList 过滤 FieldModel、elementStream 计数、toDebugString 非空）。
- `testHierarchy`（深化）：四模型 instanceof CompoundElement（运行时 `CompoundElement.class.isInstance`）；`Iterable.isAssignableFrom(CompoundElement)`；`ClassFileElement.isAssignableFrom(CompoundElement)`。
- `testIterationConsistency`（深化）：**四方计数一致**（for-each == forEach 计数 == elementStream().count() == elementList().size()）；elementStream 过滤 FieldModel 计数；两个独立 iterator 各自完整迭代且互不干扰、耗尽后 hasNext()==false；**elementList() 稳定性**（两次调用 size 相同、逐位元素类型相同——实例身份不保证，实测）；toDebugString 含类名；CodeModel 迭代全为指令时的计数一致；verify 干净。
- `testDefineClassLoop`（深化）：DynCompoundElement+seq → for-each 计数字段/方法 → defineClass → go()==7 运行闭环。
- 易错点：elementStream() 是单次消费流（重复用需再调）；不要断言两次 elementList() 的元素 `==`。
