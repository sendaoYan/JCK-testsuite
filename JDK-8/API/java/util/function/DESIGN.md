# java.util.function 包 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

43 个函数式接口：真值表、组合顺序矩阵（andThen/compose）、恒等/取反、原语特化与饱和转换、异常中继、null 元素语义。

## 2. Java 用户如何使用

执行计数器验证 andThen(x 后 y) vs compose(y 先 x)；Predicate.and/or 短路；(int)1e100 饱和；identity() 引用直通。

## 3. 参考来源

java.util.function javadoc；JLS 5.1.3 收窄转换。

## 4. 测试用例设计

全部 43 接口 testContracts()（JDK-8 侧另断言 Predicate.not 缺席，JDK-27 侧断言存在+static）。
