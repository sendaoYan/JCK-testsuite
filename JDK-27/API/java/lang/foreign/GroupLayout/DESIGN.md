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

## 5. 契约深化（JDK-27 实测锁定）
- alignment=成员最大对齐；紧密打包（struct(I,seq(3,B))=7）；padding 成员在 memberLayouts 中可见（instanceof PaddingLayout）。
- 成员降对齐可拉低组对齐（struct(I, L/4) align 4）。
- **memberLayouts() 不可修改**（add → UOE，27 实测）。
- equals 对成员顺序敏感（同 size 不同序不等）；空 struct/union 均为合法 GroupLayout。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
