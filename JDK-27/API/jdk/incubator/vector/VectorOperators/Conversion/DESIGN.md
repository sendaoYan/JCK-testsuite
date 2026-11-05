# VectorOperators.Conversion（类型转换操作符） 设计案

## 1. API 功能
lane 值转换：X2Y 值转型矩阵（B/S/I/L/F/D 互转）、REINTERPRET_F2I/D2L/I2F/L2D 位重释、ZERO_EXTEND_B2I/S2I/S2L/B2S/B2L/I2L 零扩展；工厂 ofCast/ofReinterpret；元数据 domainType/rangeType。

## 2. Java 用户如何使用
v.convert(I2D, 0)（part 0）、v.convertShape(D2I, species, 0)。

## 3. 开源代码实际用法调研
来源：JDK-27 src.zip convert(name, kind, dom, ran, ...)；ofCast 同型返回 COPY_X2X；ofReinterpret 按位宽分派。

## 4. 测试用例设计 + 易错点
domain/range 表；ofCast(int,double)==I2D、ofCast(double,double).name=="COPY_D2D"、ofReinterpret(f,i)==REINTERPRET_F2I；ofCast(int,boolean) UOE 精确消息、null NPE；值转换 vs 位重释分叉（I2D 7→7.0 与 REINTERPRET_F2I 1.0f→0x3F800000、ZERO_EXTEND_B2I(-1)→255）。

