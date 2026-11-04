# MemoryLayout — 设计方案

## 1. API 功能
`java.lang.foreign.MemoryLayout`（接口）是所有布局的根。查询 `byteSize`/`byteAlignment`/`name`；命名 `withName`/`withoutName`；对齐 `withByteAlignment`；偏移计算 `byteOffset`/`byteOffsetHandle`/`scale`/`scaleHandle`；访问句柄 `varHandle`/`arrayElementVarHandle`/`sliceHandle`；`select` 选取子布局；静态工厂 `paddingLayout`/`sequenceLayout`/`structLayout`/`unionLayout`。

## 2. Java 用户如何使用
```java
MemoryLayout seq = MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT);
long off = seq.byteOffset(PathElement.sequenceElement(2));
VarHandle vh = seq.arrayElementVarHandle(PathElement.sequenceElement());
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestLayouts` 系统验证 layout 组合/偏移/varHandle。
- jextract 生成 `MemoryLayout.structLayout(...)` 与 `PathElement.groupElement` 访问字段。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 各查询/操作/静态工厂返回类型。
黑盒：padding/sequence/struct/union 的 byteSize/alignment；命名/对齐往返；`scale`/`scaleHandle`；`select`；`arrayElementVarHandle` set/get；equals/hashCode/toString。
对应 `MemoryLayoutTest.java` 白盒 + 黑盒部分。
