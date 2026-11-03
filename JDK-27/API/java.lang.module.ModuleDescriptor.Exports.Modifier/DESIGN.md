# java.lang.module.ModuleDescriptor.Exports.Modifier — 设计方案

## 1. API 功能
枚举（2 常量：SYNTHETIC、MANDATED）：exports 修饰符，用于标记编译器/工具生成的导出。

## 2. Java 用户如何使用
```java
newModule("m").exports(Set.of(Modifier.MANDATED), "p.q").build()
 .exports().iterator().next().modifiers();
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`：自动模块推导的导出标记 MANDATED。
- JDK `test/jdk/java/lang/module/AutomaticModulesTest.java` 验证其出现。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、Enum 父类、2 常量、嵌套于 Exports。
黑盒：常量名/顺序；valueOf 往返+未知名 IAE；ordinal/compareTo；每个常量经 Builder→Exports 回读；普通 exports 修饰符为空。
对应 `ModifierTest.java` 的 `testBlackBox()`。
