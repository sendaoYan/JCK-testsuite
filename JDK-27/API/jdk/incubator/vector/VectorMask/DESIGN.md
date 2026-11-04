# jdk.incubator.vector.VectorMask 设计案

## 1. API 功能
向量掩码（`abstract class VectorMask<E>`）。分组：工厂（`fromArray/fromValues`，species 的 `maskAll/maskFromValues`）、查询（`length/trueCount/allTrue/anyTrue/laneIsSet`）、逻辑（`and/or/not`）、转换（`toVector`）。

## 2. Java 用户如何使用
`VectorMask<Integer> m = IntVector.SPECIES_256.maskAll(true); VectorMask<Integer> n = m.not(); boolean b = m.and(n).anyTrue();` 链路：maskAll → not/and/or → trueCount/allTrue/anyTrue → toVector。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/VectorMask.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/incubator/vector/` 中各向量 mask 测试（`*VectorMaskTests` 类）以 `maskAll` + `fromArray` 组合验证逻辑运算。

## 4. 测试用例设计 + 自动化实现
白盒：public/abstract/关键方法。黑盒：用 `IntVector.SPECIES_256`，`maskAll(true)` → `not/and/or` → `trueCount/laneIsSet/allTrue/anyTrue` → `toVector`（强转 IntVector 断言 lane 值），全部为确定布尔/计数断言。
