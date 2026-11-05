# java.lang.module.ResolvedModule — 设计方案

## 1. API 功能
Configuration 中的已解析模块（final）：`name()`、`configuration()`、`reference()`、`reads()`（Set<ResolvedModule>，同配置内目标）；equals = 同 Configuration + 同名；`toString` = identityHashCode(configuration)/name。

## 2. Java 用户如何使用
```java
for (ResolvedModule rm : configuration.findModule("java.sql").orElseThrow().reads())
    System.out.println(rm.name());
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/module/ResolvedModule.java`：`System.identityHashCode(cf) + "/" + name()`。
- JDK `test/jdk/java/lang/module/BasicModuleBuilderTest.java` / `ConfigurationTest.java`：reads 图与相等性。

## 4. 测试用例设计 + 自动化实现
白盒：final、无 public 构造器、7 方法、reads 返回 Set。
黑盒：java.base（name/config/reference、静态图 reads 为空）；java.sql reads java.base/java.xml 且无自读、reads 目标同配置；equals/hashCode（同配置同名相等、null/异类型/跨配置不等、子图同名模块不相等但名字相同）；toString 精确格式 identityHashCode(configuration)/name。
对应 `ResolvedModuleTest.java` 的 `testBlackBox()`。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- boot 图契约：java.base reads 空、java.logging reads 恰为 {java.base}、java.sql reads 含 base/xml/logging/xa、reads() 不可变、所有 reads 目标共享 boot 配置、toString=="identityHashCode(config)/name"。
- 自建链式图：a←b←c 的 reads 逐层精确（c 只读直接 require b，不传递到 a）；reference 即 finder 存入的同一对象；null location → empty。
- 绑定边：resolveAndBind 后 provider reads 含消费者。
- equals/hashCode：同配置重复查找相等且哈希一致；同名不同配置不等但包装同一 ModuleReference；null/异类型不等。

### 2. 使用方式
- finderOf + 三模块 requires 链 + 绑定图，全部自建、确定。

### 3. 参考来源
- Oracle javadoc ResolvedModule（equals：同配置+同名）；OpenJDK 测试思路（自写）。

### 4. 测试用例设计 + 易错规范细节
- 断言数 31→71。
- 易错点：
  - reads() 只含直接 requires（+java.base），不传递；
  - 同一 finder 解析出的两个配置中同名 ResolvedModule 不 equals，但 reference() 是同一实例（共享 finder 时）；
  - hashCode 只断言 equals 一致性，不锁定公式（configuration 身份哈希参与，跨 run 不稳定）。
