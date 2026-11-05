# javax.xml.stream.FactoryConfigurationError — 设计案

## 1. API 功能
javax.xml.stream.FactoryConfigurationError 类。实例/声明方法：getException, getCause, getMessage。

## 2. Java 用户如何使用
四种构造 → getMessage/getException/getCause。

## 3. 开源代码实际用法调研
JDK 源码自用（javax.xml.stream.FactoryConfigurationError）与 JDK `test/jdk` 内 JAXP/XML 用例；本仓库 FactoryConfigurationErrorTest 白盒结构 + 黑盒行为。

## 4. 测试用例设计 + 自动化实现
黑盒（`FactoryConfigurationErrorTest.testBlackBox()`）覆盖：四种构造 + cause。
## 5. 契约深化（本轮追加：构造链/发现触发）
4 构造重载 getException/getCause/getMessage 同一性矩阵；Error 可抛可捕；系统属性指向不存在类 → newFactory() 抛出（finally 恢复）。
