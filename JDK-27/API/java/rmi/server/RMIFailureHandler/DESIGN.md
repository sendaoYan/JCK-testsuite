# java.rmi.server.RMIFailureHandler 行为测试设计

## 1. API 功能
- 接口，单一抽象方法：`boolean failure(Exception ex)`。
- 函数式接口（可用 lambda 实现）。
- 由 `RMISocketFactory.setFailureHandler` 注册，socket 创建失败时回调。

## 2. Java 用户如何使用
- `RMIFailureHandler h = (ex) -> { log(ex); return true; }; RMISocketFactory.setFailureHandler(h);`。
- 返回 true 表示已处理，false 表示未处理。

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RMIFailureHandler.java`：单方法接口。
- JDK 源码 `src/java.rmi/share/classes/java/rmi/server/RMISocketFactory.java`：`setFailureHandler`/`getFailureHandler` 静态状态。
- RMI 运行时在 socket 创建失败时调用注册的 failure handler。

## 4. 测试用例设计 + 自动化实现
- 白盒：public interface、无父接口、单抽象方法 `failure(Exception)`。
- 黑盒：lambda 实例化；非 null/null 入参返回；记录接收到的 Exception 验证回调入参。
- 实现见 `RMIFailureHandlerTest.java`。
