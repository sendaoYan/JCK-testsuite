# jdk.incubator.vector.VectorOperators（操作符常量表） 设计案

## 1. API 功能
全部 lane 级操作符常量：Unary（NOT/ABS/NEG/ZOMO/BIT_COUNT/…/SIN..LOG1P 数学族）、Binary（SUB/DIV/AND_NOT/移位/ROL/ROR/COMPRESS_BITS…）、Associative（ADD/MUL/MIN/MAX/AND/OR/XOR/FIRST_NONZERO/SUADD/UMIN/UMAX）、Ternary（FMA/BITWISE_BLEND）、Test（IS_DEFAULT/IS_NEGATIVE/IS_FINITE/IS_NAN/IS_INFINITE）、Comparison（EQ/NE/LT/LE/GT/GE + ULT/ULE/UGT/UGE）、Conversion（X2Y 全矩阵 + REINTERPRET_* + ZERO_EXTEND_*）。

## 2. Java 用户如何使用
v.lanewise(ADD, w)、v.reduceLanes(MIN)、v.compare(LT, w)、v.test(IS_NAN)、v.convert(I2D, part)。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip VectorOperators.java；110 个 public static final 常量由 unary/binary/assoc/ternary/predicate/compare/convert 工厂构造，VO_ALL/VO_NOFP/VO_ONLYFP 决定 compatibleWith。

## 4. 测试用例设计 + 易错点
常量存在性（含 JDK-27 新增 SADD/SUADD/UMIN/UMAX/ROL/COMPRESS_BITS/FIRST_NONZERO/ZOMO）；instanceof 到 sealed 子接口图；Operator 方法面恰 {arity,compatibleWith,isAssociative,isBoolean,name,operatorName,rangeType}。

