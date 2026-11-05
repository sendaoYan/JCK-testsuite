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

## 5. 契约深化（JDK-27 实测锁定）
- **无隐式填充规则族**：struct(I,L) IAE；struct(I,pad4,L)=16（padding 是 memberLayouts 成员）；struct(L,I)=12 无尾填充；struct(B,I) IAE、struct(B,pad3,I)=8；struct(I, L/align4)=12/align4。
- 嵌套 struct 寻径（groupElement("in").groupElement("a")）偏移精确；成员顺序保留。
- 布局几何驱动真实段访问：byteOffset 结果 set/get 闭环。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
