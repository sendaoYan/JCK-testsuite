# jdk.incubator.vector.IntVector 设计案

## 1. API 功能
`int` 类型向量，`abstract class`（继承 `Vector<Integer>`）。核心分组：
- 工厂：`SPECIES_64/128/256/512`（`VectorSpecies<Integer>`）、`broadcast(VectorSpecies, int)`、`fromArray(VectorSpecies, int[], int)`。
- 运算：`add/sub/mul/div/neg`、`lanewise(VectorOperators, ...)`。
- 查询/转换：`lane(int)`、`length()`、`reduceLanes(VectorOperators.Associative)`、`intoArray(int[], int)`、`eq(...)`（返回 `VectorMask<Integer>`）。

## 2. Java 用户如何使用
`VectorSpecies<Integer> sp = IntVector.SPECIES_256; IntVector a = IntVector.broadcast(sp, 7); IntVector s = a.add(IntVector.broadcast(sp, 3)); int[] out = new int[sp.length()]; s.intoArray(out, 0);` 典型组合：broadcast → add/sub → lane/reduceLanes → intoArray/fromArray 回读。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/jdk.incubator.vector/share/classes/jdk/incubator/vector/IntVector.java`（自身实现，含 `broadcast`/`add`/`fromArray`/`intoArray` 等）。
- 来源：`~/git/jdk25u-dev/test/jdk/jdk/incubator/vector/` 下 `Int128VectorTests.java`、`Int256VectorTests.java` 等（`IntVector` 数值/装载/存储向量测试）。
- 用法以固定 species（`SPECIES_256`）做 broadcast + 算术 + 回读数组为主，跨硬件一致。

## 4. 测试用例设计 + 自动化实现
场景：白盒断言 public/abstract/extends Vector/四档 species 常量/关键方法存在；黑盒用 `SPECIES_256` 固定宽度，组合 `broadcast → add/sub/neg → lane → intoArray → fromArray → reduceLanes(ADD) → eq`，断言逐 lane 精确值（整数运算无容差）。
## 5. 契约深化（JDK-27）
- 旗舰向量化内核闭环：13 元素 a[i]*b[i]+c[i] 主循环（loopBound(13)==8? SPECIES_128→loopBound==12）+ indexInRange 尾掩码（"Mask[T...]"），逐 lane 与标量循环对账。
- lanewise 矩阵：ROL/ROR/LSHL31/ASHR 符号/LSHR 零填/BIT_COUNT/TZ/LZ(0→32)/ZOMO/COMPRESS_BITS/EXPAND_BITS/AND_NOT。
- FIRST_NONZERO 归约三态；compress/expand；selectFrom 语义 b[this[i]]；slice/unslice 掩码版；reinterpretShape 小端字节+往返；I2D/I2L/I2F/F2I 截断/REINTERPRET_F2I 位级；ULT/UGT/UGE/ULE 无符号比较表；掩码归约/blend/lanewise；toString/hashCode。
