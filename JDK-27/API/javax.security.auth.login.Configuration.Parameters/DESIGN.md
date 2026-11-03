# Configuration.Parameters 设计案（行为化测试）

## 1. API 功能
`javax.security.auth.login.Configuration.Parameters` 是一个空标记接口（marker interface），用作 `Configuration.getInstance(String, Parameters, ...)` 的类型化参数载体；本身不声明任何方法。实现类自行携带配置参数（如 URI、Provider 选项等）。

## 2. Java 用户如何使用
实现该接口传递自定义参数：
```java
class MyParams implements Configuration.Parameters { URI uri; ... }
Configuration c = Configuration.getInstance("JavaLoginConfig", new MyParams());
```
或匿名实现：`Configuration.Parameters p = new Configuration.Parameters() {};`。Provider 侧的 `ConfigurationSpi` 若无法理解参数应抛 `IllegalArgumentException`。

## 3. 开源代码实际用法调研
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/login/Configuration.java` 定义 `public interface Parameters { }` 并贯穿 `getInstance` 的三个重载。
- JDK 测试 `~/git/jdk25u-dev/test/jdk/javax/security/auth/login/Configuration/GetInstance.java` 使用 `Configuration.Parameters` 的 `BadParam`、`URIParameter` 等实现验证参数透传与 `IllegalArgumentException` 校验。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`ParametersTest.java` 的 `testBlackBox()`）：
- 匿名实现 `instanceof Configuration.Parameters`；
- 自定义实现可携带自身状态（toString 回读）；
- 作为参数传入 `Configuration.getInstance(未知类型, p)`，异常路径为 `NoSuchAlgorithmException`（证明参数被接受并进入 Provider 查找流程）。
白盒（`testWhiteBox()`）：public 接口、无父接口、无声明方法（标记接口形态）。
