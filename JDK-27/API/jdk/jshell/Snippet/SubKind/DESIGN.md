# jdk.jshell.Snippet.SubKind 设计案

## 1. API 功能
19 个细分子类枚举，附 kind()/isExecutable()/hasValue()；是 Kind 的精化。

## 2. Java 用户是否使用
分析工具经 subKind() 区分导入四形态、类型五形态、var 三形态、表达式四形态、语句、UNKNOWN。

## 3. 参考来源
- JDK-27 javadoc（JDK 21+ 有 MODULE_IMPORT_SUBKIND，共 19 值）。

## 4. 测试用例设计 + 易错规范细节
- 枚举表：19 值精确顺序、valueOf bogus IAE。
- 属性表逐项：kind() 归属全 19 项；isExecutable/hasValue 联动（声明类 false/false，var 与表达式 true/true，STATEMENT true/false）。
- 实测映射：四导入形态、class/interface/enum/record/@interface、METHOD、var 有无初始化式、`2+3`→TEMP_VAR、裸名→VAR_VALUE、赋值→ASSIGNMENT、void 调用→STATEMENT、坏输入→UNKNOWN。
- 易错：lambda/方法引用独立 eval 会 REJECTED（UNKNOWN），不会得到 OTHER_EXPRESSION_SUBKIND。
