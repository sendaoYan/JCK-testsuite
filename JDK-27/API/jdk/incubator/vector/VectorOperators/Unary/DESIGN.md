# VectorOperators.Unary（一元操作符） 设计案

## 1. API 功能
一元 lane 操作标记接口：NOT/ABS/NEG/ZOMO/SQRT/CBRT/三角对数族/BIT_COUNT/TRAILING_ZEROS_COUNT/LEADING_ZEROS_COUNT/REVERSE/REVERSE_BYTES。

## 2. Java 用户如何使用
v.lanewise(NOT)。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip unary("NOT", "~", ...)。

## 4. 测试用例设计 + 易错点
token 表（"~"/"-a"/"abs"/"sqrt"/"a==0?0:-1"/"bitCount"…）；NOFP 断言（NOT 拒 double）；ZOMO 真值 0→0/-1 行为闭环。

