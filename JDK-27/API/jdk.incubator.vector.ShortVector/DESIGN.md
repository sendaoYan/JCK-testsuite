# jdk.incubator.vector.ShortVector 设计案

## 1. API 功能
`short` 类型向量，`abstract class`。分组：工厂（`SPECIES_*`、`broadcast`、`fromArray`）、运算（`add/sub/mul`、`lanewise`）、查询/转换（`lane`、`reduceLanes`、`intoArray`）。

## 2. Java 用户如何使用
`ShortVector a = ShortVector.broadcast(ShortVector.SPECIES_256, (short) 9); ShortVector s = a.add(b); short[] out = new short[sp.length()]; s.intoArray(out, 0);` 链路：broadcast → add → lane → intoArray/fromArray → reduceLanes。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/ShortVector.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/incubator/vector/Short128VectorTests.java`、`Short256VectorTests.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/abstract/extends Vector/species 常量/关键方法。黑盒：`SPECIES_256` 固定宽度，broadcast + add + lane + intoArray/fromArray 回读 + reduceLanes；short 加法断言精确值。
