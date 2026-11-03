# jdk.incubator.vector.DoubleVector 设计案

## 1. API 功能
`double` 类型向量，`abstract class`。分组：工厂（`SPECIES_*`、`broadcast`、`fromArray`）、运算（`add/sub/mul/div/neg`、`lanewise`）、查询/转换（`lane`、`reduceLanes`、`intoArray`）。

## 2. Java 用户如何使用
`DoubleVector a = DoubleVector.broadcast(DoubleVector.SPECIES_256, 6.0); DoubleVector s = a.add(b); double[] out = new double[sp.length()]; s.intoArray(out, 0);` 链路：broadcast → add/sub → lane → intoArray → fromArray → reduceLanes。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/DoubleVector.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/incubator/vector/Double128VectorTests.java`、`Double256VectorTests.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/abstract/extends Vector/species 常量/关键方法。黑盒：`SPECIES_256` 固定宽度组合 broadcast → add/sub → lane → intoArray/fromArray → reduceLanes；简单 double 运算断言精确值，累加用容差。
