# ModuleLayer — 设计方案

## 1. API 功能
`java.lang.ModuleLayer`（final）表示模块层（一组已解析模块的运行时视图）：
- 静态：`boot()`、`empty()`
- 查询：`configuration/modules/parents/findModule/findLoader`
- 构建：`defineModulesWithOneLoader/WithManyLoaders/defineModules`

## 2. Java 用户如何使用
- `ModuleLayer.boot()` 获取启动层，`findModule(name)` 查模块，`findLoader(name)` 查类加载器。
```java
ModuleLayer.boot().findModule("java.base").ifPresent(m -> ...);
```

## 3. 开源代码实际用法调研
- 插件/容器用 `defineModules*` 动态构建模块层；JDK `test/jdk/java/lang/ModuleLayer` 系列验证 boot/empty/构建。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.ModuleLayer")` 验证 final、非接口。
黑盒：boot 层含 `java.base`、模块/配置非空、`findLoader("java.base")` 为 null、恰有一个父层（empty 层）；empty 层无模块。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 层拓扑闭环：`boot().parents()==[empty()]` 且 `empty()` 是单例、`empty().configuration()==Configuration.empty()`；自定义层 `parents()` 保序、`configuration()` 即传入配置。
- 可见性/加载器：`findModule` 沿双亲链委托（子层能看 java.base，boot 看不到子层模块）；`findLoader` 委托链；`findLoader(absent)` → IAE "Module X not known to this layer"。
- 工厂三兄弟：one-loader 双模块共享 loader（parent 为给定 loader）、many-loaders 每模块独立 loader、map 函数每模块调用一次。
- Controller：`layer()/addReads/addExports/addOpens` 返回 this；未知包 → IAE "package X not in contents"；他层模块 → IAE；`enableNativeAccess(他层模块)` → IAE "module java.base not in layer"。

### 2. 使用方式
- `Configuration.resolve(bootCfg, of(), of(), Set.of())` 空 roots + `defineModulesWithOneLoader` 空层闭环；自动模块 descriptor + finderOf 自建层做加载器拓扑与 Controller 变异。

### 3. 参考来源
- Oracle javadoc java.lang.ModuleLayer / ModuleLayer.Controller；OpenJDK ModuleLayer 测试思路（自写）。

### 4. 测试用例设计 + 易错规范细节
- 新增 8 个 testXxx：断言数 19→120。
- 易错点：
  - `defineModulesWithOneLoader` 的 parents 列表长度必须等于 configuration.parents()（否则 IAE "wrong number of parents"）；多亲用静态 `Configuration.resolve(finder, List.of(c1,c2), ...)`；
  - split package 真触发 LayerInstantiationException："Package jck.sp in more than one module"（one-loader 冲突；many-loaders 不触发）；
  - `enableNativeAccess` 是受限方法，unnamed 模块调用打 WARNING（JDK-27 仍执行成功，断言 flag 翻转）；
  - `modules()/parents()` 不可变（add → UOE）；empty 层 `toString()==""`。
