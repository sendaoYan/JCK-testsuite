# java.lang.annotation.IncompleteAnnotationException — 设计方案（JDK 8）

## 1. API 功能
`RuntimeException`：注解类型在编译使用方后新增了无默认值成员，读取该注解时抛出。
- 唯一 public 构造器：`(Class<? extends Annotation> annotationType, String elementName)`
- `annotationType()`、`elementName()`
- `serialVersionUID = 8445097402741811912L`

## 2. Java 用户如何使用
```java
catch (IncompleteAnnotationException e) {
    Class<?> anno = e.annotationType(); // 缺成员的注解类型
    String missing = e.elementName();   // 缺失成员名
}
```

## 3. 开源代码实际用法调研
- JDK `sun/reflect/annotation/AnnotationInvocationHandler.invoke`：成员缺失且无默认时抛出。
- JDK `test/jdk/java/lang/annotation/` 双版本类触发路径。
- Spring `AnnotationUtils.getRequiredAttribute` 转译该异常。

## 4. 测试用例设计 + 自动化实现
- 结构：RuntimeException 子类、`annotationType()`/`elementName()` 返回类型、唯一 public 构造器。
- `annotationType()`/`elementName()` 精确身份回读；message 自动生成含 `missing element`。
- **构造器 NPE**：任一参数为 null 抛 NullPointerException。
- 两实例独立；suppression/stackTrace/toString。
- 序列化 round-trip：annotationType + elementName 均存活；serialVersionUID 稳定。

## 易错规范细节（JDK 8）
- 构造器对 null 参数抛 NPE（javadoc 明确）。
- message 使用 `annotationType.getName() + " missing element " + elementName`；不硬编码完整 message（类型名实现相关）。

## 契约深化（2026-11）
### API 功能
- 构造器矩阵：恰 1 个声明构造器 public (Class<? extends Annotation>, String)，参数列表精确（javap 双侧一致）。
- **精确消息锁定（双侧逐字一致）**：`<类型 getName()> missing element <elementName>`；toString 全等 "java.lang.annotation.IncompleteAnnotationException: ..."；嵌套注解类型用二进制名（`Outer$Local`）——getName() 而非 getCanonicalName()。
- elementName 任意串（含标点/空格）回读；构造器接受任意注解 Class（含元注解 Target）。

### 使用方式
构造 + getMessage 全等 + 嵌套类型 fixture 验证二进制名形态。

### 参考来源
javap 双侧；运行时消息观察。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testMessageExact` / `testConstructorMatrixAndViews`；新增嵌套 @LocalMarker fixture。
- 真触发（注解类型演化后旧类文件缺成员）需双版本编译产物，按批次简报记为跳过，仅锁构造路径（确定性）。
- 易错：消息用 getName()（`$` 形态）；NPE 于任一 null 实参（既有已锁）。
