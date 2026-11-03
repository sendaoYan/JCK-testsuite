# java.lang.module.ModuleDescriptor.Opens.Modifier — 设计方案

## 1. API 功能
枚举（2 常量：SYNTHETIC、MANDATED）：opens 修饰符，用于标记生成的开放声明。

## 2. Java 用户如何使用
```java
newModule("m").opens(Set.of(Modifier.SYNTHETIC), "p.q").build()
 .opens().iterator().next().modifiers();
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`。
- JDK `test/jdk/java/lang/module/ModuleDescriptorTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、Enum 父类、2 常量、嵌套于 Opens。
黑盒：常量名/顺序；valueOf 往返+IAE；ordinal/compareTo；每个常量经 Builder→Opens 回读；普通 opens 修饰符为空。
对应 `ModifierTest.java` 的 `testBlackBox()`。
