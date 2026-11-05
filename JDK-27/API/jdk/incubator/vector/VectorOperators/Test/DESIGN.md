# VectorOperators.Test（一元布尔测试） 设计案

## 1. API 功能
一元布尔 lane 测试：IS_DEFAULT(bits(a)==0)/IS_NEGATIVE(bits(a)<0)/IS_FINITE/IS_NAN/IS_INFINITE（后三浮点专属）。

## 2. Java 用户如何使用
v.test(IS_NAN) → VectorMask。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip predicate(...)。

## 4. 测试用例设计 + 易错点
arity 1 + isBoolean + rangeType boolean.class；IS_NAN 拒 int、IS_DEFAULT 接 int；NaN/有限行为闭环。

