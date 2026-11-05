# PaddingLayout — 设计方案

## 1. API 功能
`java.lang.foreign.PaddingLayout`（接口，继承 `MemoryLayout`）表示填充字节。工厂 `MemoryLayout.paddingLayout(long)`；继承 `withName`/`withoutName`/`withByteAlignment` 协变返回 `PaddingLayout`。

## 2. Java 用户如何使用
```java
MemoryLayout st = MemoryLayout.structLayout(
    ValueLayout.JAVA_INT, MemoryLayout.paddingLayout(4), ValueLayout.JAVA_LONG);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestLayouts` 用 padding 对齐结构体成员。
- jextract 生成含 padding 的 struct 描述以匹配 C 内存布局。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 `MemoryLayout` + 协变 `withName`/`withoutName`/`withByteAlignment`。
黑盒：`paddingLayout(8)` 的 byteSize/alignment；命名/对齐往返；padding 在 struct 中正确对齐后续成员。
对应 `PaddingLayoutTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（JDK-27 实测锁定）
- paddingLayout(0)/负 → IAE；**可命名**（name 回读）；withByteAlignment 可调高（8 合法）。
- 自然对齐恒 1；纯 padding struct（pad16）size 16 align 1。
- equals 按 size+name 值语义；组内偏移路径 groupElement(i) 精确。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
