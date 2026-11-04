# javax.naming.LinkLoopException — 测试设计

## 1. API 功能
链接解析成环异常（extends LinkException）：`()`/`(String)` 构造器；继承 link 状态与 NamingException 全套机制。

## 2. Java 用户如何使用
provider 检测到链接环时抛出；客户端 `e.getLinkRemainingName()` 查看循环经过的名字链。

## 3. 开源代码实际用法调研
- `com.sun.jndi.*` 链接循环检测路径 throw LinkLoopException。
- JDK test/jdk/javax/naming/** 覆盖链接异常家族。

## 4. 测试用例设计（LinkLoopExceptionTest.java）
- 白盒：extends LinkException、双构造器。
- 黑盒：message 保留；填充 link 状态后 toString 可见链路；可被 LinkException 与 NamingException 捕获；no-arg message null。
