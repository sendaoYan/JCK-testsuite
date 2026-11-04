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
