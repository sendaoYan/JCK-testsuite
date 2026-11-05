# VectorOperators.Associative（可结合操作符） 设计案

## 1. API 功能
可结合二元操作符（reduceLanes 合法输入）：ADD/MUL/MIN/MAX/FIRST_NONZERO/AND/OR/XOR/SUADD/UMIN/UMAX。

## 2. Java 用户如何使用
v.reduceLanes(ADD)、v.reduceLanes(MIN, mask)。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip assoc(...)，VO_ASSOC 标志。

## 4. 测试用例设计 + 易错点
全家族 isAssociative==true 表；SUB/DIV==false；FIRST_NONZERO 归约（[0,0,5,0]→5）行为闭环。

