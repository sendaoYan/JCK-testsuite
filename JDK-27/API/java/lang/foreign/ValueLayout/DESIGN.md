# ValueLayout — 设计方案

## 1. API 功能
`java.lang.foreign.ValueLayout`（接口，继承 `MemoryLayout`）描述标量值布局。常量 `JAVA_BYTE/BOOLEAN/CHAR/SHORT/INT/LONG/FLOAT/DOUBLE`、`ADDRESS` 及对应 `*_UNALIGNED`；查询 `order()`/`carrier()`；操作 `withOrder`/`withName`/`withoutName`/`withByteAlignment`/`varHandle()`。

## 2. Java 用户如何使用
```java
ValueLayout.OfInt intLayout = ValueLayout.JAVA_INT;
int v = seg.get(intLayout, 0);
ValueLayout bigEndian = intLayout.withOrder(ByteOrder.BIG_ENDIAN);
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/foreign/TestLayouts` 验证各 primitive 布局的 size/alignment/order/carrier。
- jextract 用 `ValueLayout.JAVA_INT` 等描述 C `int`/`long` 布局；Panama 教程用 `withOrder` 处理网络字节序。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 继承 `MemoryLayout` + 关键常量类型 + order/withOrder/carrier/varHandle 签名。
黑盒：8 种 primitive 布局 size/alignment；carrier；默认 order/withOrder；命名/对齐往返；unaligned 变体；varHandle set/get。
对应 `ValueLayoutTest.java` 白盒 + 黑盒部分。

## 5. 契约深化（JDK-27 实测锁定）
- **全家族表**：8 个 JAVA_* 的 size{1,1,2,2,4,4,8,8}==alignment（紧凑对齐）+ carrier 精确表 + order==nativeOrder + 默认无名。
- **7 个 *_UNALIGNED 变体**（含 ADDRESS_UNALIGNED）：alignment 1、size/carrier 不变。
- JAVA_INT.withByteAlignment(1) 合法；withByteAlignment(3) IAE；withName/withoutName 值往返 equals。
- varHandle：varType 精确（long.class）、坐标 (MemorySegment, long)。
- UNALIGNED@1 真实可访问而 JAVA_INT@1 IAE（同段对照）。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
