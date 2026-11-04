# java.lang.module.ModuleFinder — 设计方案

## 1. API 功能
模块定位接口：`find(String)`→Optional<ModuleReference>、`findAll()`→Set；静态工厂 `ofSystem()`（运行时镜像）、`of(Path...)`（目录/模块化 jar）、`compose(ModuleFinder...)`（按序合并、先者胜）。

## 2. Java 用户如何使用
```java
ModuleFinder finder = ModuleFinder.compose(ModuleFinder.of(paths), ModuleFinder.ofSystem());
ModuleReference ref = finder.find("com.example").orElseThrow();
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ModuleFinder.java`。
- jlink/jdeps 用 ofSystem() 遍历系统模块；ModuleLayer.defineModules 前的自定义 finder 组合。
- JDK `test/jdk/java/lang/module/ModuleFinderTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口、5 个方法、静态/返回类型。
黑盒：ofSystem find(java.base/java.logging/absent)、location 为 jrt URI、findAll 与 find 一致性；of() 空查找器；of(不存在的目录) 视为空；compose 的并集语义、空 compose、重复模块去重（同 finder 组合 size 不变）。
对应 `ModuleFinderTest.java` 的 `testBlackBox()`。
