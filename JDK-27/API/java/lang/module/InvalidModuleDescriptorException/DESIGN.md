# java.lang.module.InvalidModuleDescriptorException — 设计方案

## 1. API 功能
模块描述符损坏/非法（RuntimeException，仅 2 个 public 构造器：无参、String）。与 FindException 区别：模块找到了但 module-info 无法解析。

## 2. Java 用户如何使用
```java
try {
    ModuleFinder.of(dirWithBadModuleInfo).findAll();
} catch (FindException | InvalidModuleDescriptorException e) { ... }
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptorReader.java`：读取 module-info.class 字节时对非法内容抛出。
- JDK `test/jdk/java/lang/module/ModuleFinderTest.java`：损坏 jar/目录的行为验证。

## 4. 测试用例设计 + 自动化实现
白盒：父类、恰好 2 个 public 构造器、serialVersionUID。
黑盒：无参/带消息构造器、message/cause、toString 前缀、suppressed、initCause 可用（因无 cause 构造器）。
对应 `InvalidModuleDescriptorExceptionTest.java` 的 `testBlackBox()`。
