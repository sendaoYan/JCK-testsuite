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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- Throwable 机制：序列化 round-trip、精确 toString、防御拷贝、setStackTrace、printStackTrace、initCause、getLocalizedMessage、构造面（仅 ()/(String)，无 Throwable 形参构造器）。
- 真触发四路（ModuleDescriptor.read）：坏版本号 "Unsupported major.minor version 0.3"；空输入/空 ByteBuffer "Truncated module-info.class"；非 module-info 类文件 "access_flags should be ACC_MODULE"；read(null) NPE；失败后正常 read 不受影响（无全局状态）。

### 2. 使用方式
- 固定字节数组 + String.class 字节双轨。

### 3. 参考来源
- Oracle javadoc ModuleDescriptor.read 的异常条款；消息实测。

### 4. 测试用例设计 + 易错规范细节
- 断言数 12→35。
- 易错点：字节数组固定 {CA FE BA BE, minor=0x0003, major=0x0000,...} → 消息 "0.3"；String.class 也能触发（ACC_MODULE 校验在读 magic 之后立即发生）。
