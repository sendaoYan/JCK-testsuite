# ArithmeticException — 设计方案（JDK-8 侧新建，lcO1 批）

## 1. API 功能
`java.lang.ArithmeticException` 继承 `RuntimeException`，构造器 2 个：`()`、`(String)`。
`serialVersionUID = 2256477558314496007L`（双侧一致）。无特有访问器，全部行为继承 Throwable。

## 2. 使用方式
JVM 在整数除零/取模零抛出（消息恒为 "/ by zero"）；`Math.*Exact` 系列在溢出时抛出（"integer overflow"/"long overflow"）。

## 3. 参考来源
JLS 15.17.2（整数除法抛 AE）；Oracle javadoc Math.addExact 等；双侧实测消息矩阵全部一致（"/ by zero"、"integer overflow"、"long overflow" 均直锁）。

## 4. 测试用例设计 + 易错规范细节
- 除零矩阵：int/long 的 `/` 与 `%`、byte/short 提升为 int、复合赋值 `/=` `%=`、`Math.floorDiv/floorMod`（int/long/负被除数）全部消息 "/ by zero" 逐字。
- 溢出矩阵：add/subtract/multiply/negate/increment/decrementExact（int→"integer overflow"、long→"long overflow"）、`toIntExact`→"integer overflow"。
- 负例（不抛）：`Integer.MIN_VALUE / -1` 静默回绕为 MIN_VALUE、`% -1`==0、floorDiv(-7,2)==-4/floorMod==1。
- 版本差异：`Math.absExact` JDK 15+ 才有——8 侧反射断言 ABSENT（NoSuchMethodException），27 侧直测消息 "Overflow to represent absolute value of Integer.MIN_VALUE"/"...Long.MIN_VALUE"。
- 通用契约（构造矩阵/cause/suppressed/trace/print/SUID/序列化）+ JVM 抛出实例详情（cause null、toString 全等）。
- 断言数 8 → 91（27 侧 20 → 104）。
