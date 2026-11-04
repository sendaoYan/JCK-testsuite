# SequenceLayout — 设计方案

## 1. API 功能
`java.lang.foreign.SequenceLayout`（接口，继承 `MemoryLayout`）描述重复元素序列。查询 `elementLayout()`/`elementCount()`；操作 `withElementCount(long)`、`reshape(long...)`、`flatten()`；命名/对齐协变方法。

## 2. Java 用户如何使用
```java
SequenceLayout seq = MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT);
SequenceLayout mat = MemoryLayout.sequenceLayout(2, MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_INT));
long off = seq.byteOffset(PathElement.sequenceElement(2));
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestLayouts` 验证 sequence 的 reshape/flatten/offset。
- jextract 用 sequenceLayout 描述 C 数组；Netty ffm 用其做缓冲区步进。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 `MemoryLayout` + elementLayout/elementCount/withElementCount/reshape/flatten 返回类型。
黑盒：elementCount/elementLayout/byteSize/offset；withElementCount；嵌套 sequence reshape(6)/reshape(2,3)/flatten 的组合链路与 byteSize 守恒。
对应 `SequenceLayoutTest.java` 白盒 + 黑盒部分。
