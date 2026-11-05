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

## 5. 契约深化（JDK-27 实测锁定——对齐规则大改）**
- **structLayout 无隐式填充**：成员必须落在自身 byteAlignment 上，否则 **IAE "Invalid alignment constraint for member layout"**——`struct(JAVA_INT, JAVA_LONG)` 直接非法（long 将落在 offset 4）。需显式 `paddingLayout(4)`（size 16）或成员 `withByteAlignment(4)`（size 12）。
- **无尾填充**：struct(JAVA_LONG, JAVA_INT) size=12（非 16）；struct(JAVA_INT, seq(3,B)) size=7。
- **空组合合法**：structLayout()/unionLayout() size 0（27 实测，任务书预判 IAE 修正）。
- **变长 sequence 已移除**：sequenceLayout(-1)/(-2)、withElementCount(-1)/(-2) 全 IAE；seq(0) size 0。
- **withByteAlignment**：0/负/非 2 幂（3）→ IAE；16（超自然对齐）合法。
- **paddingLayout**：0/负 → IAE；**可命名**（27 实测）；自然对齐恒 1，withByteAlignment 可调高。
- **路径算术**：groupElement(name)/groupElement(i)/sequenceElement(i) 精确偏移表；select 返回**带名字的成员**（非规范实例）；byteOffset(未知名) → IAE。
- scale(base,factor)=base+factor*byteSize。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
