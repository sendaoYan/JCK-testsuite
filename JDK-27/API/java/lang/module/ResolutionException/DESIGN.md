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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 序列化 round-trip、精确 toString、防御拷贝、setStackTrace、initCause 一次性（重设 ISE）、printStackTrace 前缀。
- 真触发：双模块环与三模块环（resolve 与 resolveAndBind 双路径）→ "Cycle detected: a -> b -> a"（断言前缀 + 双名 + 箭头，不锁遍历顺序）。

### 2. 使用方式
- finderOf 构造环图，resolve/resolveAndBind 双入口。

### 3. 参考来源
- Oracle javadoc ResolutionException；环检测消息实测。

### 4. 测试用例设计 + 易错规范细节
- 断言数 16→36。
- 易错点：环消息中模块出现顺序取决于内部遍历，只断言 "Cycle detected:" 前缀、包含双方、"->" 分隔。
