# java.lang.Compiler 测试设计案（JDK-8 独有）

> 覆盖目标：**完整契约**。no-op JIT 门面：final 类、0 公开构造器、恰 5 个自有公开方法且全部
> static native、无编译器时全部安全 no-op（compileClass/compileClasses 返 false、command 返 null、
> enable/disable 无返回可任意调用）、null 参数不抛。JDK-9 起 Companion 模块移除后此类不复存在
> （本套件 JDK-27 无此目录）。

## 1. API 功能

- **类**：`java.lang.Compiler`（final，0 公开构造器，不可实例化）。
- **5 个自有公开方法**（全部 `public static native`，实测 javap）：
  - `boolean compileClass(Class<?> clazz)`
  - `boolean compileClasses(String string)`
  - `Object command(Object any)`
  - `void enable()` / `void disable()`
- **no-op 语义**：无 JIT 编译器接入时——compileClass/compileClasses 恒 `false`（「编译失败/无操作」）、command 恒 `null`、enable/disable 可任意调用无效果。
- **JDK-8 无 @Deprecated 注解**（尽管 javadoc 早已标记 deprecated 文档性说明）。

## 2. Java 用户如何使用

- 现代代码完全不用；它仅为 Java 1.0 JIT 抽象的遗留。

```java
Compiler.compileClass(MyClass.class); // false：无编译器
Compiler.command("info");             // null
```

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Compiler.html`：no-op 契约、「methods do nothing」条款）。
- **javap 实测**（Temurin 8u372）：`javap -public java.lang.Compiler` 输出恰 5 方法全 static native、0 构造器。
- **实测 JDK-8 运行时**（探针 /tmp/lcF）：compileClass(null)/compileClasses(null)/command(null) 均不抛；enable/disable 任意顺序调用无异常。

## 4. 测试用例设计（映射到 CompilerTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | enable/disable 可调用 | testCompiler |
| P0 | 基础 no-op（String 类/包名/命令） | testCompiler |
| P0 | final/extends Object/0 公开构造器/无 @Deprecated/恰 5 方法全 static native/返回类型 | testWhiteBox |
| P0 | null 参数不抛（compileClass/compileClasses/command）+ 基本类型类 + 未知类名 + 空串 | testBehavior |
| P1 | enable/disable 状态切换后 no-op 不变 | testBehavior |

## 关键规范细节（易错，已锁入断言）

- **null 参数全部安全**：`compileClass(null)`/`compileClasses(null)`/`command(null)` 返回 false/false/null，**不抛 NPE**（对照 RuntimePermission 的空串 IAE，这里没有参数校验）。
- **`getConstructors().length == 0`**（final + 私有化构造）；`getMethods()` 中自有方法恰 5 个，其余全是 Object 继承（equals/hashCode/toString/wait/notify/notifyAll/getClass/registerNatives 时代残留不在此列）。
- **enable/disable 无幂等约束**：连续/交替调用均合法无效果。
- **JDK-9+ 此类仍在 java.lang 但恒 no-op 且 `@Deprecated(since=9, forRemoval=true)`**——本套件 JDK-27 无该目录，8 侧独测。

## 注意事项

- 全 static native 方法调用无需加载本地库（JVM 内建符号解析）；测试不触碰任何 so。
