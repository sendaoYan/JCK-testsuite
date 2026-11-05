# jdk.incubator.vector.VectorShape 设计案

## 1. API 功能
向量宽度形状（`final class VectorShape extends Enum`）。分组：常量（`S_64_BIT/S_128_BIT/S_256_BIT/S_512_BIT/S_Max_BIT`）、查询（`vectorBitSize`）、工厂（`forBitSize/forIndexBitSize/preferredShape`）、species 生成（`withLanes`）。

## 2. Java 用户如何使用
`VectorShape sh = VectorShape.S_256_BIT; int bits = sh.vectorBitSize(); VectorSpecies<Integer> sp = sh.withLanes(int.class);` 链路：常量 → vectorBitSize → forBitSize → withLanes 生成 species。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/VectorShape.java`。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/incubator/vector/` 中形状/转换测试用 `VectorShape.S_*_BIT` 常量。

## 4. 测试用例设计 + 自动化实现
白盒：public/final/enum/关键方法。黑盒：断言各常量 vectorBitSize，`forBitSize` 映射，`valueOf/values`，`withLanes` 生成正确 elementType 的 species，全部确定。
## 5. 契约深化（JDK-27）
- values() 恰 5 值声明序 [S_64_BIT..S_Max_BIT]；S_Max_BIT 2 的幂 ≥64。
- forBitSize 精确表（64/128/256/512）+ 负例 0/32/65/511/8192 全部 IAE 精确消息 "Bad vector bit-size: N"；forIndexBitSize(32,128)==(64,64)==S_64_BIT；preferredShape 2 的幂；withLanes+withShape 复合长度。
