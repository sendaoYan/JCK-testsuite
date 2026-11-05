# jdk.incubator.vector.VectorShuffle 设计案

## 1. API 功能
向量洗牌/重排描述（`abstract class VectorShuffle<E>`）。分组：工厂（`iota/fromValues/fromArray`）、查询（`length/laneSource/vectorSpecies`）、转换（`toVector`、`rearrange`）。

## 2. Java 用户如何使用
`VectorShuffle<Integer> s = VectorShuffle.iota(IntVector.SPECIES_256, 0, 1, false); int src = s.laneSource(0);` 链路：iota/fromValues → laneSource → rearrange（组合两个 shuffle）→ vectorSpecies。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/VectorShuffle.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/incubator/vector/` 中 shuffle/重排相关测试（`VectorRearrangeTests` 等）。

## 4. 测试用例设计 + 自动化实现
白盒：public/abstract/关键方法。黑盒：用 `IntVector.SPECIES_256`，`iota` 验证顺位 `laneSource`，`fromValues` 验证逆序，`rearrange` 组合两个 shuffle 后断言 laneSource，全部确定。
## 5. 契约深化（JDK-27）
- toString "Shuffle[3, 1, ...]"；toArray/intoArray 往返；toVector lane==源索引；laneIsValid 标记 -1 无效。
- 负源索引保留原值（laneSource==-1），wrapIndexes 包装为 n-1，checkIndexes 抛 IndexOutOfBoundsException；iota(start,step,wrap) 步进回绕。
- fromArray/fromOp(λ)；shuffle.rearrange(shuffle) 源复合；makeZip/makeUnzip 半区交错（含 -n/-8 负偏移语义）；cast 同 lane 数成功/异 lane 数 IAE 精确消息；数据向量 rearrange 取 data[sh[i]]。
