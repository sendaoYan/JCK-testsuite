# java.lang.module.ResolutionException — 设计方案

## 1. API 功能
模块解析失败（RuntimeException，4 个 public 构造器）：依赖循环、requires 非法等解析期错误。

## 2. Java 用户如何使用
```java
try {
    ModuleLayer.defineModules(cf, clf, mapper);
} catch (ResolutionException e) { ... }
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/Resolver.java`：循环检测 "Cycle detected"。
- JDK `test/jdk/java/lang/module/ConfigurationTest.java`：cyclic requires 期望 ResolutionException。

## 4. 测试用例设计 + 自动化实现
白盒：父类、4 构造器、serialVersionUID。
黑盒：4 构造器 message/cause 全组合、cause-only message=cause.toString()、identity、toString 前缀、suppressed、可作为无 throws 声明的 RuntimeException 传播。
对应 `ResolutionExceptionTest.java` 的 `testBlackBox()`。
