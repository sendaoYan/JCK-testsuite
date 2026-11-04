# java.security.spec.AlgorithmParameterSpec — 设计案

## 1. API 功能
算法参数规范的标记接口

所有 *ParameterSpec 类型的公共标记接口；KeyFactory/AlgorithmParameters/Signature.setParameter 等引擎以本接口类型接受参数对象。
接口无方法、无父接口；具体实现有 DSAParameterSpec、DSAGenParameterSpec、MGF1ParameterSpec、PSSParameterSpec、RSAKeyGenParameterSpec 等。

## 2. 使用方式
算法参数透明承载接口

## 3. 参考来源
- Oracle javadoc `java.security.spec.AlgorithmParameterSpec`（构造器异常契约、常量值、格式名）。
- OpenJDK `test/jdk/java/security/spec/` 与 `test/jdk/java/security/KeyFactory` 相关回归（思路借鉴）。
- 本地双 JDK javap/运行实测（JDK-8u372 / JDK-27.0.1）。

## 4. 测试用例设计 + 易错规范细节
标记语义验证（instanceof/isAssignableFrom 矩阵、无声明方法、Object-only 公共方法面、null 可赋值、数组组件类型）。
- 统一骨架：`static int passed; static void check(boolean,String)` + `checkThrows/checkThrowsMsg`（异常类型+消息包含断言）+ `PASS (N assertions)`。
