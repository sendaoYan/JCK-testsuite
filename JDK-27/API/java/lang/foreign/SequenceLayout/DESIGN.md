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

## 5. 契约深化（JDK-27 实测锁定）
- **变长已移除**：-1/-2 计数（构造与 withElementCount）全 IAE；seq(0) size 0。
- **reshape 矩阵**：维度乘积必须等于 elementCount（reshape(5)/reshape(2) 于 seq(4) → IAE）；reshape(1,2,2) 嵌套 [2:[2:i4]] 且总 size 不变；flatten 精确还原（equals 原序）。
- 对齐继承元素布局；sequenceElement(i) 偏移=base+i*元素 size；select(sequenceElement()) 返回元素布局本体（==）。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
