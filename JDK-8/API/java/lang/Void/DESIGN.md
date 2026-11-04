# java.lang.Void 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「读一下 Void.TYPE」的冒烟。重点补：白盒结构（final、非接口、
> 唯一成员 `TYPE` 为 `static final Class<Void>`、无公开构造器），以及 `Void.TYPE == void.class` 的
> 基本类型恒等。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **final 类**：`java.lang.Void` 是 `void` 基本类型的包装占位类。
- **唯一成员**：`public static final Class<Void> TYPE`（恒等于 `void.class`）。
- **无公开构造器**（不可实例化）。

## 2. Java 用户如何使用

- 反射/泛型中表示 `void` 返回类型：`Void.TYPE`、`void.class`。
- `Method.getReturnType()` 对 void 方法返回 `Void.TYPE`；`invoke` 返回 `null`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Void.html`）：`TYPE` 字段、不可实例化。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Void.java`：private 构造器 +
  `TYPE = Class.getPrimitiveClass("void")`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Void`。

## 4. 测试用例设计（映射到 VoidTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、非接口、TYPE 存在且 static final 且类型为 Class、无公开构造器 | testWhiteBox |
| P0 | TYPE == void.class、名称 "void"、isPrimitive、与 Void.class 区分 | testType |

## 关键规范细节（易错，已锁入断言）

- **`Void.TYPE` 是 `Class<Void>` 且恒等于 `void.class`**，但 `Void.class` 是另一个对象（包装类 Class）。
- **`Void` 无公开构造器**（`new Void()` 编译不过；反射 `getConstructors().length == 0`）。

## 注意事项

- headless 安全，不加 `@key`；结果全确定。
