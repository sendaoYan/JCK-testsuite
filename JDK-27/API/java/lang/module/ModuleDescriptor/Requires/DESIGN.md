# java.lang.module.ModuleDescriptor.Requires — 设计方案

## 1. API 功能
模块依赖项（final、值对象、无 public 构造器，经 Builder 创建）：
- 查询：`name()`、`modifiers()`（TRANSITIVE/STATIC/SYNTHETIC/MANDATED）、`accessFlags()`、`compiledVersion()`/`rawCompiledVersion()`（Optional）
- 比较：`compareTo`（按依赖模块名）、`equals/hashCode`（名+修饰符+版本）

## 2. Java 用户如何使用
```java
for (Requires r : module.getDescriptor().requires()) {
    if (r.modifiers().contains(Requires.Modifier.TRANSITIVE)) ...
    r.compiledVersion().ifPresent(v -> ...);
}
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`：`Requires.toString` 形如 `transitive static java.base`。
- JDK `test/jdk/java/lang/module/ModuleDescriptorTest.java`：Requires 各重载与 equals/compareTo。
- jdeps 输出模块依赖图时按 requires().name() 遍历。

## 4. 测试用例设计 + 自动化实现
白盒：final、Comparable、无 public 构造器、9 个方法存在。
黑盒：带版本+双修饰符的回读（name/modifiers/accessFlags/compiledVersion/rawCompiledVersion/toString 含修饰符）；plain 变体（无修饰符无版本、toString 即模块名）；Version 对象重载；equals/hashCode/compareTo（含按名排序、修饰符参与 equals）。
对应 `RequiresTest.java` 的 `testBlackBox()`。
