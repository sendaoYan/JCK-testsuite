# VectorOperators.Ternary（三元操作符） 设计案

## 1. API 功能
三元 lane 操作：FMA（a*b+c，浮点专属）、BITWISE_BLEND（a^((a^b)&c)，整型）。

## 2. Java 用户如何使用
v.lanewise(FMA, b, c)。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip ternary(...)。

## 4. 测试用例设计 + 易错点
arity==3；FMA 拒 int；BITWISE_BLEND 表达式 token；2*3+1==7 行为闭环。

