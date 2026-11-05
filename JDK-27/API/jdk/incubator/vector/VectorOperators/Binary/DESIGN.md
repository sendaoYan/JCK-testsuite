# VectorOperators.Binary（二元操作符） 设计案

## 1. API 功能
二元 lane 操作标记接口：SUB/DIV/AND_NOT/LSHL/ASHR/LSHR/ROL/ROR/SADD/SSUB/COMPRESS_BITS/EXPAND_BITS/ATAN2/POW/HYPOT。

## 2. Java 用户如何使用
v.lanewise(ROL, 4)。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip binary(...)。

## 4. 测试用例设计 + 易错点
token 表含 "&~"/">>>"/"rotateLeft"；SADD 饱和 int-only；AND_NOT/ROL 真值行为闭环（0xF0 &~ 0xFF == 0；ROL(1,4)==16）。

