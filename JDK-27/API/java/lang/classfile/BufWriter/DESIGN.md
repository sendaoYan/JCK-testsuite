# java.lang.classfile.BufWriter — 设计案

## 1. API 功能
`java.lang.classfile.BufWriter`（interface）。工厂/静态方法：(none)。查询/实例方法：constantPool, canWriteDirect, reserveSpace, writeU1, writeU2, writeInt, writeFloat, writeLong, writeDouble, writeBytes。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `BufWriterTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.BufWriter` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`BufWriterTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.BufWriter")` + public/interface/继承 + 关键成员。黑盒（`BufWriterTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 4.1 深化（契约覆盖版，28 断言）

BufWriter 无公开工厂、无法直接实例化——唯一黑盒触达路径是 `ClassFile.AttributeMapperOption` 注册 CustomAttribute mapper 后在 `writeAttribute(BufWriter, A)` 回调中捕获实例（本测试全部经此通道）；无法触达的面不造 API。

- `testPrimitiveSizeTable()`：全原语写方法 size() 精确增量表——writeU1=1 / writeU2=2 / writeInt=writeFloat=4 / writeLong=writeDouble=8 / writeBytes=N / writeBytes(off,len)=len / writeIndex(池条目)=2 / writeIndexOrZero(null 或条目)=2 / writeIntBytes(2/4)=2/4 / reserveSpace(4)=0（只扩容量不动逻辑 size，后续 writeInt 照常 +4）；canWriteDirect(自身池)==true、canWriteDirect(外来池)==false。
- `testPatchIntRoundtrip()`：patchInt(offset, size, value) 原地改写闭环——mapper 写 u2 名索引 + u4 长度 + payload 占位后 patchInt 两处（u4 与 u2），re-parse 后 readAttribute 回读 patched 值 0x11223344 / 0xABCD；patchInt 不改变 size()（原地）；delta==头6+payload4。
- `testBadLengthNegative()`：长度字段写离谱值（0x7FFFFFFF）→ parse 惰性校验，attributes() 触发时抛 IAE 且消息含 "too big"。

实测锁定契约事实（writeAttribute 布局契约，字节级对照）：
1. mapper 负责写完整属性：u2 属性名索引 + u4 长度 + payload；长度字段框架不代填，写错即整流错位。
2. 进入 writeAttribute 回调时 BufWriter 位置是类文件缓冲的绝对偏移（首个类属性实测 base=6：magic4+version4 之后的访问标志/类索引段），patchInt 的 offset 必须以 bw.size() 为基计算。
3. patchInt(offset, size, value) 按 size(2 或 4) 原地覆写已写字节，size() 不前进；用于回填长度/偏移占位（先 writeInt(0) 占位再 patch）。
4. reserveSpace(n) 仅保证容量（grow），不推进逻辑位置——size() 增量 0；紧随的写操作照常追加。
5. 属性长度字段与实际 payload 字节数必须严格一致：声明 8 实写 6 → 解析端 readAttributes 抛 IAE "attribute X too big to handle"；且属性读取是惰性的，parse() 不触发，attributes()/findAttribute() 才触发。
6. canWriteDirect 是池兼容性判定：仅当目标池与 writer 绑定池可共享（同一池）时 true，外来 ConstantPoolBuilder.of() 为 false。
