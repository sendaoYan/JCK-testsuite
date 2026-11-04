# java.lang.module.ModuleDescriptor.Exports — 设计方案

## 1. API 功能
导出包项（final 值对象）：`source()`（包名）、`targets()`（限定目标模块集合，空=非限定）、`isQualified()`、`modifiers()`（SYNTHETIC/MANDATED）、`accessFlags()`、`compareTo`（按 source）。

## 2. Java 用户如何使用
```java
for (Exports e : descriptor.exports()) {
    if (e.isQualified()) e.targets().forEach(t -> ...);
    else exportToAll(e.source());
}
newModule("m").exports("p", Set.of("friend")).build();
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleDescriptor.java`。
- JDK `test/jdk/java/lang/module/ModuleDescriptorTest.java`：qualified/unqualified 构造与相等。
- JPMS 兼容库（bnd/OSGi 适配）读取 exports 决定可见性。

## 4. 测试用例设计 + 自动化实现
白盒：final、Comparable、无 public 构造器、9 方法存在。
黑盒：qualified（双修饰符+双目标）与 unqualified（无修饰符/目标）回读；equals/hashCode/compareTo（按 source 排序）；targets 参与相等；单目标限定导出；真实模块 java.base 对 java.lang 的非限定导出。
对应 `ExportsTest.java` 的 `testBlackBox()`。
