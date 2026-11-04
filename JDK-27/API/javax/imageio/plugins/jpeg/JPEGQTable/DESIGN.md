# javax.imageio.plugins.jpeg.JPEGQTable — DESIGN

## 1. API 功能
`JPEGQTable` 封装 8x8 JPEG 量化表（自然序，非 zig-zag）。方法分组：
- 内置常量：`K1Luminance`、`K1Div2Luminance`、`K2Chrominance`、`K2Div2Chrominance`。
- 构造器：`JPEGQTable(int[] table)`（需 64 元素，复制入参）。
- 查询/变换：`getTable()`（返回副本）、`getScaledInstance(float, boolean)`、`toString()`。

## 2. Java 用户如何使用
用户用内置表或自建 64 元素 `int[]`，`new JPEGQTable(arr)` 后经 `getScaledInstance(scaleFactor, forceBaseline)` 生成质量缩放表；再传给 `JPEGImageReadParam.setDecodeTables` / `JPEGImageWriteParam.setEncodeTables`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/imageio/plugins/jpeg/JPEGQTable.java`：内置表来自 JPEG 规范 K.1/K.2；`getScaledInstance` 把每个元素乘 `scaleFactor` 加 0.5 后截断，并 clamp 到 `[1,32767]`（`forceBaseline` 时 `[1,255]`）。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/jpeg/JPEGImageWriter.java`：写 JPEG 时依据压缩质量从默认量化表缩放生成实际表。
- JDK 自带测试 `test/jdk/javax/imageio/plugins/jpeg/` 有压缩质量往返测试。

## 4. 测试用例设计 + 自动化实现（`JPEGQTableTest.testBlackBox`）
- 四个内置表非空且 64 元素、`toString` 前缀。
- 构造器复制语义：入参往返、`getTable()` 返回副本、构造后改入参不影响内部。
- `getScaledInstance`：`1.0f` 精确保留、`2.0f` 每项翻倍、`forceBaseline` clamp `[1,255]`、非 baseline clamp `[1,32767]`。
- 非法入参：null、长度 63/65 均抛 `IllegalArgumentException`。

## 5. 契约深化补充（evtplug，2026-11）
- 反射断言公共方法集恰 3 个；**未覆写 equals/hashCode → 同一性语义**（实测）。
- 构造矩阵：null/63/65 长 → IAE；恰 64 合法；**0/1000/-5 原样存储**（构造不校验数值范围，规范未写）。
- `getScaledInstance` 取整公式 `(int)(v*scale+0.5f)`：3×0.5→2、5×0.5→3（四舍五入）；clamp `[1,32767]` / forceBaseline `[1,255]`；scale 0/负/NaN → 全 1；+Inf → 32767/255；调用不改 this。
- 标准表锚点：K1[0]==16、K2[0]==17、K1Div2==逐项 (K1+1)/2 全表断言。
