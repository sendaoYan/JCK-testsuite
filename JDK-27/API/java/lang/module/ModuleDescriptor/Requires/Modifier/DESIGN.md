# java.lang.module.ModuleDescriptor.Requires.Modifier — 设计方案

## 1. API 功能
枚举（4 常量，声明序 TRANSITIVE→STATIC→SYNTHETIC→MANDATED）：requires 的修饰符。
- `TRANSITIVE`：传递依赖；`STATIC`：编译期（requires static）；`SYNTHETIC`/`MANDATED`：生成代码标记。

## 2. Java 用户如何使用
```java
newModule("m").requires(Set.of(Modifier.TRANSITIVE, Modifier.STATIC), "java.sql", version)
Requires r = ...; r.modifiers().contains(Modifier.TRANSITIVE);
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`：普通模块的隐式 java.base 依赖即 MANDATED。
- JDK `test/jdk/java/lang/module/ModuleDescriptorTest.java`：各修饰符 Builder 回读。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、Enum 父类、4 常量。
黑盒：常量名与顺序 join 断言；全体 valueOf 往返 + 未知名 IAE；每个修饰符经 Builder→Requires 回读；ordinal/compareTo/name；真实语义：newModule 隐式 java.base 为 MANDATED。
对应 `ModifierTest.java` 的 `testBlackBox()`。
