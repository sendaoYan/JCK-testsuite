# javax.naming.spi.ResolveResult — 测试设计

## 1. API 功能
- 值类，`Resolver.resolveToClass` 的返回值，含已解析对象与剩余名称；实现 `java.io.Serializable`。
- 构造器：`ResolveResult(Object, String)`（字符串按 `CompositeName` 解析）、`ResolveResult(Object, Name)`、受保护的默认构造器。
- 查询：`getResolvedObj()`、`getRemainingName()`。
- 修改：`setRemainingName(Name)`（克隆）、`appendRemainingName(Name)`、`appendRemainingComponent(String)`、`setResolvedObj(Object)`。

## 2. Java 用户如何使用
- 命名解析器（`Resolver`）返回部分解析结果：已解析对象 + 剩余名称，供上层续接。
- 组合链路：`new ResolveResult(obj, "a/b")` → `getRemainingName()` 为两段 `CompositeName` → `appendRemainingComponent("c")` → `"a/b/c"` → `setResolvedObj(next)`。

## 3. 开源代码实际用法调研
来源：`~/git/jdk25u-dev`。
- `com/sun/jndi/toolkit/ctx/ComponentContext`、`PartialCompositeContext` 使用 `ResolveResult` 返回部分解析结果。
- `javax/naming/spi/ContinuationContext.resolveToClass` 返回 `ResolveResult` 以表达续接剩余名。

## 4. 测试用例设计
黑盒断言（`ResolveResultTest.testBlackBox`）：
- 字符串构造器把 `"a/b"` 解析为两段 `CompositeName`，`size/get/toString` 正确。
- Name 构造器克隆输入名，源名后续修改不影响结果。
- `setRemainingName` 替换/克隆、`setRemainingName(null)` 清空。
- `appendRemainingName`/`appendRemainingComponent`（含 null 无操作）拼接正确。
- `setResolvedObj` 替换对象。
白盒断言（`testWhiteBox`）：public class、继承 `Object`、实现 `Serializable`、方法与构造器签名。
