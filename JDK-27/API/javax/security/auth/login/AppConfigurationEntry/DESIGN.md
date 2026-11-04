# AppConfigurationEntry 设计案（行为化测试）

## 1. API 功能
`javax.security.auth.login.AppConfigurationEntry` 表示 Configuration 中单个 LoginModule 的配置条目，不可变：
- 构造 `AppConfigurationEntry(String loginModuleName, LoginModuleControlFlag controlFlag, Map<String,?> options)`；
- `getLoginModuleName()`：返回模块类名；
- `getControlFlag()`：返回 REQUIRED/REQUISITE/SUFFICIENT/OPTIONAL 之一；
- `getOptions()`：返回选项的不可修改视图。

构造校验（源码 `~/git/jdk25u-dev/src/java.base/share/classes/javax/security/auth/login/AppConfigurationEntry.java`）：loginModuleName 为 null 或空、controlFlag 为 null 或非法、options 为 null 均抛 `IllegalArgumentException`；options 用 `Collections.unmodifiableMap(options)` 包装（包装原 map 而非拷贝）。

## 2. Java 用户如何使用
Configuration 子类实现 `getAppConfigurationEntry(String name)` 时构造条目返回给 `LoginContext`：
```java
AppConfigurationEntry[] entries = new AppConfigurationEntry[] {
    new AppConfigurationEntry("com.example.MyLoginModule",
        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
        Map.of("debug", "true"))
};
```
读取条目：`entry.getLoginModuleName()` / `entry.getControlFlag()` / `entry.getOptions()`；选项只读，写入会抛 `UnsupportedOperationException`。

## 3. 开源代码实际用法调研
- JDK 默认配置实现 `sun.security.provider.ConfigFile`（`~/git/jdk25u-dev/src/java.base/share/classes/sun/security/provider/ConfigFile.java`）在解析 `java.security.auth.login.config` 文件后构造 `AppConfigurationEntry` 数组。
- JDK 测试 `~/git/jdk25u-dev/test/jdk/javax/security/auth/login/LoginContext/MyConfiguration.java`、`.../Configuration/GetInstance.java` 用自定义 `Configuration` 返回 `AppConfigurationEntry[]` 驱动 LoginContext 登录。

## 4. 测试用例设计 + 自动化实现
黑盒断言（`AppConfigurationEntryTest.java` 的 `testBlackBox()`）：
- 三 getter 回读构造参数；controlFlag 为同一对象引用；
- `getOptions().put(...)` 抛 `UnsupportedOperationException` 且不改变内容；
- options 是调用方 map 的活视图（构造后改原 map，getOptions 可见）；
- 四个 controlFlag 分别 round-trip；
- 构造边界：null/空模块名、null flag、null options 均抛 `IllegalArgumentException`。
白盒（`testWhiteBox()`）：public 具体类、直接父类 Object、构造器与三 getter 存在。
