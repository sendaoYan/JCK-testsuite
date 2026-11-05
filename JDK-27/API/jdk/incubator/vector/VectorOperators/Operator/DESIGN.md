# VectorOperators.Operator（操作符元接口） 设计案

## 1. API 功能
所有操作符的公共元数据协议：name()（常量名）、operatorName()（Java 符号/表达式）、arity()（1-3）、isBoolean()、rangeType()、isAssociative()、compatibleWith(Class)。

## 2. Java 用户如何使用
框架/工具按元数据分派；用户一般只经具体子接口消费。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip；sealed，permits {Unary, Binary, Ternary, Test, Comparison, Conversion, OperatorImpl}。

## 4. 测试用例设计 + 易错点
sealed 断言 + permits 列表；元数据真值表（ADD arity2 assoc；LT boolean range=boolean.class；I2D rangeType=double.class 覆盖默认）；compatibleWith 矩阵（SQRT 拒 int、AND 拒 double）。

