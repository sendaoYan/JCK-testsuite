# java.lang.classfile.Interfaces — 设计案

## 1. API 功能
`java.lang.classfile.Interfaces`（interface，extends `ClassElement`）。静态工厂 4 个：`of(ClassEntry...)`、`of(List&lt;ClassEntry&gt;)`、`ofSymbols(ClassDesc...)`、`ofSymbols(List&lt;ClassDesc&gt;)`。实例方法：`interfaces()`（List&lt;ClassEntry&gt;，**不可变视图**）。

## 2. Java 用户如何使用
`cb.withInterfaceSymbols(descs...)`（varargs/List 两重载）在 build 时声明；`cb.with(Interfaces.of(...))` 以元素形式注入；`cm.interfaces()` 回读。空列表合法（生成 count==0 的 interfaces 段）；**重复接口不去重**；null 负例全部 NPE。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.Interfaces`。
- OpenJDK `test/jdk/jdk/classfile/`（ClassBuildingTest）。
- 本机实测：of(null arr)→NPE "Cannot read the array length"；of(null list)→NPE "Cannot invoke java.util.Collection.isEmpty()"；ofSymbols((ClassDesc)null)→NPE；ofSymbols(null list)→NPE；dup（Runnable×2）size==2；interfaces().add→UOE。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：ofSymbols 2 接口 + build 往返）。
- `testFactoryMatrix`（深化）：**4 个工厂全重载**（varargs/List × entries/symbols）+ 条目身份（== 同一 ClassEntry）；空工厂（of()/ofSymbols() size 0）；重复接口保留 size 2；**null 负例 4 种精确 NPE**；**interfaces() 不可变**（add/remove → UOE）；层次 instanceof。
- `testBuildReadback`（深化）：3 接口 build 全量回读（顺序精确）；**Interfaces 元素在 elementList 且其 entries 与 cm.interfaces() 同一对象（==）**；空接口 build → interfaces() 空 + 空 Interfaces 元素仍存在；withInterfaceSymbols(List) 重载；真实 javac 嵌套 implements 类（精确顺序 [Runnable, InterfacesTest$NestedIf, Serializable]）；标记接口（无 extends）interfaces() 空；verify 干净。
- `testDefineClassLoop`（深化）：DynInterfaces+seq 实现 Runnable → defineClass → `Runnable.class.isAssignableFrom(dc)`。
- 易错点：interfaces() 是不可修改视图；`ClassBuilder.withInterfaceSymbols` 与元素注入 `with(Interfaces.of(...))` 等价（后者会在 elementList 出现独立元素）。
