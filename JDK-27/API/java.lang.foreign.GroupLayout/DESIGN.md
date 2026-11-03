# GroupLayout — 设计方案

## 1. API 功能
`java.lang.foreign.GroupLayout`（接口，继承 `MemoryLayout`）是 `StructLayout`/`UnionLayout` 的公共父接口。查询 `memberLayouts()`；继承并协变返回 `GroupLayout` 的 `withName`/`withoutName`/`withByteAlignment`。

## 2. Java 用户如何使用
```java
GroupLayout g = MemoryLayout.structLayout(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG);
List<MemoryLayout> members = g.memberLayouts();
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign` 布局测试用 `GroupLayout` 类型承载 struct/union 并遍历 `memberLayouts`。
- `~/git/jdk25u-dev` 中 `AbstractGroupLayout` 实现 struct/union 的公共 byteSize/alignment 计算。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 `MemoryLayout` + `memberLayouts`/协变 `withName`/`withoutName`/`withByteAlignment`。
黑盒：struct/union 作为 `GroupLayout` 的 `memberLayouts`/`byteSize`/`withName`/`withByteAlignment` 断言。
对应 `GroupLayoutTest.java` 白盒 + 黑盒部分。
