# java.lang.module.FindException — 设计方案

## 1. API 功能
模块查找失败（RuntimeException，4 个 public 构造器：无参/String/Throwable/String+Throwable），模块解析器找不到根模块或依赖时抛出。

## 2. Java 用户如何使用
```java
try {
    Configuration.resolve(finder, parents, ModuleFinder.of(), Set.of("my.missing"));
} catch (FindException e) { ... }
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/Resolver.java`：抛 "Module %s not found"。
- JDK `test/jdk/java/lang/module/ConfigurationTest.java`：unknown root 期望 FindException。

## 4. 测试用例设计 + 自动化实现
白盒：父类 RuntimeException、4 构造器、serialVersionUID。
黑盒：各构造器 message/cause 语义（cause-only 的 message == cause.toString()）、identity 传递、toString 前缀、addSuppressed/initCause（二次 ISE）、栈非空；真实触发：resolve 未知根抛出且消息含模块名、find(absent) 返回空 Optional 不抛。
对应 `FindExceptionTest.java` 的 `testBlackBox()`。
