# jdk.incubator.vector.VectorSpecies 设计案

## 1. API 功能
向量形状与元素类型的组合（`interface VectorSpecies<E>`）。分组：查询（`elementType/elementSize/length/vectorBitSize/vectorShape/vectorType/maskType`）、形状调整（`withShape`）、工厂（`broadcast/fromArray/maskAll`）。

## 2. Java 用户如何使用
`VectorSpecies<Integer> sp = IntVector.SPECIES_256; int n = sp.length(); IntVector v = (IntVector) sp.broadcast(3);` 链路：取 species → length/vectorBitSize → withShape 调整宽度 → broadcast/fromArray/maskAll 生成向量/掩码。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/VectorSpecies.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/incubator/vector/` 中各测试用 `SPECIES_*` 常量作为所有向量运算的起点。

## 4. 测试用例设计 + 自动化实现
白盒：public interface/关键方法。黑盒：用 `IntVector.SPECIES_256`，断言 elementType/elementSize/vectorBitSize/vectorShape，`withShape` 扩/缩宽度后 length 单调变化，`broadcast/maskAll/fromArray` 生成实例。
