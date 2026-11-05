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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 序列化 round-trip：message/cause 保真、类保持；精确 toString "java.lang.module.FindException: ser" 与 no-arg 无冒号形。
- Throwable 机制：getSuppressed 防御拷贝、setStackTrace/getStackTrace 往返与拷贝独立、printStackTrace 输出含 "Suppressed:" 节、fillInStackTrace 返回 this。
- 真触发三路：缺根 "Module no.such.root not found"；缺依赖 "Module jck.fe.absent not found, required by jck.fe.user"；finder 自身抛 FindException 原样传播。

### 2. 使用方式
- resolve 负路径 + 自抛 finder 双轨。

### 3. 参考来源
- Oracle javadoc FindException；实际 Resolver 的消息格式实测。

### 4. 测试用例设计 + 易错规范细节
- 断言数 22→41。
- 易错点：缺根/缺依赖的消息措辞不同（后者带 "required by"）；两者 getCause 均为 null。
