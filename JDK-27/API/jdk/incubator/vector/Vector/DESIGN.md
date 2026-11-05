# jdk.incubator.vector.Vector 设计案

## 1. API 功能
向量基接口（`abstract class Vector<E>`，实现向量的顶层抽象）。分组：查询（`species/elementType/elementSize/shape/length`）、运算（`lanewise/add/sub`、`broadcast(long)`、`eq`）、规约（`reduceLanesToLong`）。

## 2. Java 用户如何使用
通常以具体类型（`IntVector` 等）编程，通过 `Vector<Integer>` 引用访问公共接口：`Vector<Integer> v = IntVector.broadcast(IntVector.SPECIES_256, 7); Vector<Integer> s = v.add(v.broadcast(1)); long r = v.reduceLanesToLong(VectorOperators.ADD);` 链路：broadcast → add/eq → reduceLanesToLong。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/Vector.java`（定义 `broadcast/lanewise/reduceLanesToLong/eq`）。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/incubator/vector/AbstractVectorTest.java`（Vector 接口的抽象测试骨架，被各具体向量测试复用）。

## 4. 测试用例设计 + 自动化实现
白盒：public/abstract/关键方法（species/elementType/length/shape/broadcast/add/reduceLanes）。黑盒：以 `IntVector.SPECIES_256` 实例化，组合 `broadcast → add → lane(经强转 IntVector) → reduceLanesToLong → eq(自身)`，断言逐 lane 精确值。
## 5. 契约深化（JDK-27）
- 结构不变量：elementSize*length==vectorBitSize、shape==species.vectorShape、viewAsIntegralLanes 对整型 species 恒等。
- 双精度位视图 viewAsIntegralLanes→long（同 lane 数）+ viewAsFloatingLanes 往返恢复值。
- compress/expand/selectFrom（this 为索引向量：result[i]=w[lane(i)]）；slice/unslice 掩码版；reduceLanesToLong 带掩码；compare/eq 掩码位置；hashCode 值等价、equals 顺序敏感；withLane 不可变；IS_DEFAULT。
