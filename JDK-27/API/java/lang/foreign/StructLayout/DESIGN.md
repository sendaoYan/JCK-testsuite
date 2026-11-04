# StructLayout — 设计方案

## 1. API 功能
`java.lang.foreign.StructLayout`（接口，继承 `GroupLayout`）描述结构体布局。成员布局按声明顺序排列、需自然对齐；`withName`/`withoutName`/`withByteAlignment` 协变返回 `StructLayout`。

## 2. Java 用户如何使用
```java
StructLayout st = MemoryLayout.structLayout(
    ValueLayout.JAVA_INT.withName("i"),
    MemoryLayout.paddingLayout(4),
    ValueLayout.JAVA_LONG.withName("l"));
long off = st.byteOffset(PathElement.groupElement("l"));
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestLayouts` 验证 struct 成员偏移/对齐（含 padding）。
- jextract 为每个 C struct 生成 `MemoryLayout.structLayout(...)` 描述。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 `GroupLayout` + 协变 withName/withoutName/withByteAlignment。
黑盒：struct 的 memberLayouts/byteSize/byteAlignment/成员偏移/成员命名；命名/对齐往返；equals/hashCode。
对应 `StructLayoutTest.java` 白盒 + 黑盒部分。
