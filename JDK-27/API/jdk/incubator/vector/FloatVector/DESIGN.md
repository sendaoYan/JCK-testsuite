# jdk.incubator.vector.FloatVector 设计案

## 1. API 功能
`float` 类型向量，`abstract class`。核心分组：
- 工厂：`SPECIES_*`、`broadcast`、`fromArray`。
- 运算：`add/sub/mul/div/neg`、`lanewise`。
- 查询/转换：`lane`、`reduceLanes`、`intoArray`。

## 2. Java 用户如何使用
`FloatVector a = FloatVector.broadcast(FloatVector.SPECIES_256, 2.5f); FloatVector s = a.add(b); float[] out = new float[sp.length()]; s.intoArray(out, 0);` 组合链路：broadcast → 算术 → intoArray/fromArray → reduceLanes。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/FloatVector.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/incubator/vector/Float128VectorTests.java`、`Float256VectorTests.java`（FloatVector 数值与装载测试）。

## 4. 测试用例设计 + 自动化实现
白盒：public/abstract/extends Vector/species 常量/关键方法。黑盒：`SPECIES_256` 固定宽度，broadcast + add/mul + lane + intoArray/fromArray 回读 + reduceLanes；浮点断言用精确值（加/乘简单运算）或小容差（reduceLanes 累加）。
## 5. 契约深化（JDK-27）
- fma 与 lanewise(FMA) 一致 2*3+1；SQRT；test 族 IS_NAN/IS_FINITE/IS_INFINITE/IS_DEFAULT/IS_NEGATIVE 掩码断言。
- NaN eq 全 false；div/abs/neg；掩码归约；F2I 截断 2.75→2 与 F2D；REINTERPRET_F2I 1.0f→0x3F800000；viewAsIntegralLanes(int) 往返；blend；掩码 load/store 零填充模式；toString。
