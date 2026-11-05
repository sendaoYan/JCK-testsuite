# java.lang.module.Configuration — 设计方案

## 1. API 功能
模块解析结果（final、不可变）：`empty()`（空配置单例）、`resolve/resolveAndBind`（实例版以 this 为父、静态版可传多父）、`parents()`、`modules()`（Set<ResolvedModule>）、`findModule(String)`（Optional）、`toString`（模块名集合）。

## 2. Java 用户如何使用
```java
Configuration parent = ModuleLayer.boot().configuration();
Configuration cf = Configuration.resolve(ModuleFinder.ofSystem(), List.of(parent),
        ModuleFinder.of(), Set.of("java.sql"));
ModuleLayer layer = ModuleLayer.defineModules(cf, clf, definedModules -> ...).layer();
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/Configuration.java`：resolve 图算法、服务绑定（resolveAndBind 迭代 provides）。
- JDK `test/jdk/java/lang/module/ConfigurationTest.java`：空根、未知根 FindException、多父合并。
- ModuleLayer.defineModulesWithOneLoader 之前必须先得到 Configuration。

## 4. 测试用例设计 + 自动化实现
白盒：final、无 public 构造器、7 个 public 方法及返回类型。
黑盒：empty() 单例与空集；boot 配置含 java.base、parent 是 empty 配置；实例 resolve(空查找器,空根) 得父为本配置的空图；静态 resolve(java.sql) 拉入 java.xml/java.logging 依赖；resolveAndBind 不小于 resolve；未知根抛 FindException 且消息含模块名。
对应 `ConfigurationTest.java` 的 `testBlackBox()`。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 结构契约：boot 配置 parents==[empty()]、modules()/parents() 不可变、findModule(null) NPE、toString 是名字列表（empty 为空串）。
- 空根解析：`resolve(of(), of(), Set.of())` → 无模块子配置，父即接收者；findModule 沿双亲委托（java.base 可见）；resolveAndBind 同形。
- 服务绑定闭环：uses+provides 时 resolveAndBind 把 provider 拉进图（resolve 只有 root）；无 uses 则 provider 不绑定；provider reads [java.base, 消费者]。
- 失败模式精确锁定：缺根 → FindException "Module no.such.root not found"；缺依赖 → "Module jck.cfg.missing not found, required by jck.cfg.user"；环 → ResolutionException "Cycle detected: a -> b -> a"。
- finder 优先级：before 胜 after（descriptor 身份可验）；多亲 resolve：依赖从相应父配置满足并保持其 configuration 身份；根模块总是从 finder 重新解析（即使父配置已有同名模块），而 requires 边由父满足则不重复。

### 2. 使用方式
- 自造 finderOf 闭包做图实验；静态/实例 resolve 对 java.sql 的名字集一致性对照。

### 3. 参考来源
- Oracle javadoc Configuration（resolve 的 before/after 语义、服务绑定）；OpenJDK Configuration 测试思路（自写）。

### 4. 测试用例设计 + 易错规范细节
- 断言数 32→122；原有 PASS 行位于 testBlackBox 尾部，新增调用排在它之前以汇总计数。
- 易错点：
  - 实例 `resolve(finder, finder, roots)` 是 3 参；静态是 4 参（含 parents 列表），用 `Configuration.resolve(...)` 名字直接调 3 参会编译错；
  - null finder / null roots / null parents → NPE；
  - 子配置自身 modules() 不含父配置模块，但 findModule 可见；
  - "java.base 作根" 会在子配置中重新解析出一份（configuration()==child），与直觉相反——实测锁定。
