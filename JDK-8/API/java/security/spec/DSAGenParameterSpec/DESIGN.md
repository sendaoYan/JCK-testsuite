# java.security.spec.DSAGenParameterSpec — 设计案

## 1. API 功能
DSA 参数生成规范（JDK-8 起）

final 类，2/3 参构造（primePLen,primeQLen[,seedLen]）；seedLen 缺省=primeQLen。
AlgorithmParameterGenerator.getInstance("DSA").init(spec) 消费。

## 2. 使用方式
预生成 DSA p/q 参数的请求

## 3. 参考来源
- Oracle javadoc `java.security.spec.DSAGenParameterSpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
合法域 {(1024,160),(2048,224),(2048,256),(3072,256)} 与 seedLen>=qLen；非法矩阵精确异常消息（primePLen/subprimeQLen/seedLen 各自文案）；final 类；身份 equals；非 Serializable（final 类反射断言）。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
