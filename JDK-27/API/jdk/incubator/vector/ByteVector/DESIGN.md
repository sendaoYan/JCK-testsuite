# jdk.incubator.vector.ByteVector 设计案

## 1. API 功能
`byte` 类型向量，`abstract class`。分组：工厂（`SPECIES_*`、`broadcast`、`fromArray`）、运算（`add/sub/mul`、`lanewise`）、查询/转换（`lane`、`reduceLanes`、`intoArray`）。

## 2. Java 用户如何使用
`ByteVector a = ByteVector.broadcast(ByteVector.SPECIES_256, (byte) 5); ByteVector s = a.add(b); byte[] out = new byte[sp.length()]; s.intoArray(out, 0);` 链路：broadcast → add → lane → intoArray/fromArray → reduceLanes。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/ByteVector.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/incubator/vector/Byte128VectorTests.java`、`Byte256VectorTests.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/abstract/extends Vector/species 常量/关键方法。黑盒：`SPECIES_256` 固定宽度，broadcast + add + lane + intoArray/fromArray 回读 + reduceLanes；byte 加法断言精确值。
## 5. 契约深化（JDK-27）
- SADD/SSUB/SUADD/SUSUB 饱和语义与 VectorMath 标量方法逐 lane 对账（220 饱和 127 / 无符号 220 / 下溢 0）。
- UMIN/UMAX 无符号极值（vs 255）；AND/OR/XOR/AND_NOT/NOT/LSHL/ASHR/LSHR 位级真值。
- fromArray/intoArray 带偏移（数组须容纳 offset+length，越界 IOOBE）；reinterpretShape↔ShortVector 小端 0x0102 往返。
- 掩码 fromArray/intoArray 未选中 lane 置零；compress/expand lane 重排；FIRST_NONZERO/AND/XOR/OR/MIN/MAX 归约；EQ/LT/ULT 比较；withLane；lane 越界 IAE。
