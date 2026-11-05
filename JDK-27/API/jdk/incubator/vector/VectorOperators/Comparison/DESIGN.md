# VectorOperators.Comparison（比较操作符） 设计案

## 1. API 功能
二元布尔比较：EQ/NE/LT/LE/GT/GE + 无符号 ULT/ULE/UGT/UGE（整型专属）。

## 2. Java 用户如何使用
v.compare(LT, w) → VectorMask；无符号族用于把 -1 视作最大值。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip compare(...)，BT_* 字节码测试码。

## 4. 测试用例设计 + 易错点
token 表；boolean range；ULT 拒 double；signed vs unsigned -1 双断言（LT true / ULT false / UGT true）。

