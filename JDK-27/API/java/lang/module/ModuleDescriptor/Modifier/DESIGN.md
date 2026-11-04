# java.lang.module.ModuleDescriptor.Modifier — 设计方案

## 1. API 功能
枚举（4 常量，声明序 OPEN→AUTOMATIC→SYNTHETIC→MANDATED）：模块级修饰符，经 `newModule(name, Set<Modifier>)` 使用。

## 2. Java 用户如何使用
```java
ModuleDescriptor d = ModuleDescriptor.newModule("m", Set.of(Modifier.OPEN)).build();
d.isOpen(); d.modifiers();
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`：OPEN 驱动 isOpen、AUTOMATIC 由 newAutomaticModule 注入。
- JDK `test/jdk/java/lang/module/ModuleDescriptorTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、Enum 父类、4 常量。
黑盒：常量名/顺序；valueOf 往返+IAE；ordinal/compareTo；`newModule(name, Set)` 逐修饰符回读（OPEN⇒isOpen、SYNTHETIC 单独既非 open 非 automatic）；newAutomaticModule ⇒ {AUTOMATIC}；普通模块修饰符为空。
对应 `ModifierTest.java` 的 `testBlackBox()`。
