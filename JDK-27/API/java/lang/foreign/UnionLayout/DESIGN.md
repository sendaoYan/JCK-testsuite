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
