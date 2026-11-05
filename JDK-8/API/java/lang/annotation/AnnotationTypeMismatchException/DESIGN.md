# java.lang.annotation.AnnotationTypeMismatchException — 设计方案（JDK 8）

## 1. API 功能
`RuntimeException`：注解元素在类编译后类型变化，反射读取该元素时抛出。
- 唯一 public 构造器：`(Method element, String foundType)`（无 message 参数，message 自动生成）
- `element()`、`foundType()`
- `serialVersionUID = 8125925355765570191L`

## 2. Java 用户如何使用
```java
catch (AnnotationTypeMismatchException e) {
    Method m = e.element();   // 哪个成员变了
    String t = e.foundType(); // classfile 实际发现类型描述符
}
```

## 3. 开源代码实际用法调研
- JDK `sun/reflect/annotation/AnnotationType.matchToMethod` 匹配失败抛出。
- JDK `test/jdk/java/lang/annotation/AnnotationTypeMismatchException/` 双版本类触发路径。
- Spring `AnnotationUtils` 透传该异常处理版本漂移。

## 4. 测试用例设计 + 自动化实现
- 结构：RuntimeException 子类、`element()`/`foundType()` 返回类型、唯一 public 构造器。
- `element()`/`foundType()` 精确回读；message 前缀 `Incorrectly typed data found for annotation element`。
- null element / null foundType 合法（javadoc 明确允许）。
- RuntimeException 捕获、两实例互异、suppression/stackTrace。
- 序列化：serialVersionUID 稳定；**JDK 8 element 非 transient** → 带真实 Method 写序列化抛 NotSerializableException；null element 时 round-trip 且 foundType 存活。

## 易错规范细节（JDK 8）
- **序列化差异**：JDK 8 的 `element` 字段非 transient，而 `Method` 不可序列化，故带真实 Method 序列化抛 NotSerializableException；JDK 27 起 `element` 改为 transient，round-trip 后 `element()` 返回 null（javadoc：序列化后可能不可用）。
- message 由构造器自动生成，无需 message 参数；不断言完整 message（含 Method.toString()，实现相关）。

## 契约深化（2026-11）
### API 功能
- 构造器签名 javap 双侧实测：**两侧均恰 1 个声明构造器 public (Method, String)**——简报中"8 可能是 (Method,Class)"的猜测被实测否定（8u372 与 27 同签名）。
- 精确消息组成锁定（双侧逐字一致）：`Incorrectly typed data found for annotation element <Method.toString()> (Found data of type <foundType>)`；null 实参亦有确定形态 `... element null (Found data of type null)`。
- element() 反射视图：getName/getDeclaringClass/静态方法引用皆可；foundType 接受含空格任意串。

### 使用方式
动态拼期望消息（用 Method.toString() 组合，不硬编码）+ 反射视图断言。

### 参考来源
javap 双侧；sun.reflect.annotation 抛出点消息模板（行为观察，未拷贝源码）。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testConstructorMatrix`：唯一构造器 + 参数列表。
- `testMessageExact`：全字锁定（含 null 形态）。
- `testElementView`：element()/foundType() 视图与静态 Method。
- 版本差异（既有已锁）：element 字段 27 侧 transient → 序列化后 null；8 侧非 transient → NotSerializableException。
