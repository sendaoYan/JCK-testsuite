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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- ofSystem：java.base/java.logging 的 `jrt:/<name>` 精确 location、java.base requires 为空、java.logging requires java.base、findAll 数量>50、名字唯一、全部 jrt URI、结果集不可变。
- of()：空 finder 的 find/findAll/find(null) NPE、findAll 不可变、`of()` 不缓存；of(不存在目录) 与空目录行为一致（空 finder）。
- compose：第一个 finder 同名胜出（descriptor 身份断言）、单 finder 委托、中间空 finder 不遮蔽、结果不可变、并集按名字去重。
- 自定义 finder：find/findAll 与描述符身份往返；直接驱动 `Configuration.resolve` 且 ResolvedModule.reference() 即 finder 的引用。

### 2. 使用方式
- `finderOf(ModuleDescriptor...)` 匿名实现注入到 resolve/compose，验证 finder 协议的下限契约。

### 3. 参考来源
- Oracle javadoc ModuleFinder；OpenJDK ModuleFinder 测试思路（自写）。

### 4. 测试用例设计 + 易错规范细节
- 断言数 27→90。
- 易错点：
  - 自定义 finder 的 `find(null)` 是实现自定义（我们的实现返回 empty），而 `ModuleFinder.of()` 的实现抛 NPE——两者都按各自实现断言；
  - `ofSystem().findAll()` 的 Set 不可变（add → UOE）；
  - 系统 finder 在 compose 首位时，后续 finder 无法遮蔽 java.base（身份相等断言）。
