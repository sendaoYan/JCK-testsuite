# UnionLayout — 设计方案

## 1. API 功能
`java.lang.foreign.UnionLayout`（接口，继承 `GroupLayout`）描述联合体布局：所有成员共享同一偏移，size/alignment 取成员最大值；`withName`/`withoutName`/`withByteAlignment` 协变返回 `UnionLayout`。

## 2. Java 用户如何使用
```java
UnionLayout un = MemoryLayout.unionLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
long size = un.byteSize(); // 8
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestLayouts` 验证 union 成员重叠与 size= max(member)。
- jextract 为 C union 生成 `MemoryLayout.unionLayout(...)`。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 `GroupLayout` + 协变 withName/withoutName/withByteAlignment。
黑盒：union 的 memberLayouts/byteSize/byteAlignment/成员重叠偏移 0；命名/对齐往返；equals/hashCode/toString。
对应 `UnionLayoutTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（JDK-27 实测锁定）
- 全员 offset 0 叠置；size/alignment=成员最大值；成员顺序保留；空 union 合法（size 0）。
- union 分配 honoring alignment 8。
- **overlay 语义**（端序无关断言）：long 写 -1 后 byte@0 写 0——byte@0==0 而 byte@1..7 仍 0xFF（最小成员覆写不触碰其余成员字节）。
- withName/withoutName 协变链保持 union。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
