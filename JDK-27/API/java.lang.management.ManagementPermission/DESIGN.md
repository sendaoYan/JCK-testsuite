# ManagementPermission — 设计方案

## 1. API 功能
`java.lang.management.ManagementPermission`（final，继承 `BasicPermission`）控制访问管理接口的权限。构造 `(String name)` 与 `(String name, String actions)`；actions 必须为 null 或空字符串，否则抛 `IllegalArgumentException`。

## 2. Java 用户如何使用
```java
new ManagementPermission("control");
new ManagementPermission("monitor", null);
```

## 3. 开源代码实际用法调研
- JDK 安全管理器相关用例用 `ManagementPermission("control")` 保护 MXBean 写操作。
- 应用在自定义 SecurityManager 策略文件中声明 `java.lang.management.ManagementPermission "control"`。

## 4. 测试用例设计 + 自动化实现
白盒：final 类 + 继承 BasicPermission + 两个构造器。
黑盒：getName/getActions（null/空 actions）；implies 同名校验；非空 actions 抛 IllegalArgumentException；点分扩展名可接受。
对应 `ManagementPermissionTest.java` 白盒 + 黑盒部分。
