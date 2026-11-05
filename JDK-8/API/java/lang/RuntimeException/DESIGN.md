# java.lang.RuntimeException 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「new 一个异常」的冒烟。重点补：白盒结构（父类 Exception、非接口、
> 四公共构造器存在）、非受检层级（Exception→Throwable）、四构造器消息/原因传播、protected 4 参构造器
> （enableSuppression/writableStackTrace 语义）。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.RuntimeException` 继承 `Exception`（并间接 `Throwable`），是非受检异常的根。
- **构造器（4 公共 + 1 protected）**：`()`、`(String)`、`(String,Throwable)`、`(Throwable)`，
  以及 protected `(String,Throwable,boolean,boolean)`。

## 2. Java 用户如何使用

- 自定义非受检异常继承 `RuntimeException`，抛出无需 `throws` 声明。
- `NullPointerException`、`IllegalArgumentException`、`IllegalStateException` 均继承 `RuntimeException`。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/RuntimeException.html`）：四公共构造器。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/RuntimeException.java`。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/RuntimeException`。

## 4. 测试用例设计（映射到 RuntimeExceptionTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：父类 Exception、非接口、四公共构造器 | testWhiteBox |
| P0 | 非受检层级（Exception 且 Throwable） | testHierarchy |
| P0 | 四构造器消息/原因传播 | testConstructors |
| P0 | protected 4 参构造器：enableSuppression/writableStackTrace 语义 | testBlackBox |

## 关键规范细节（易错，已锁入断言）

- **`(Throwable)` 构造器的 `getMessage()` == `cause.toString()`**。
- **protected 4 参构造器**：`enableSuppression=false` 时 `addSuppressed` 静默忽略；
  `writableStackTrace=false` 时 `setStackTrace` 后栈仍为空。

## 注意事项

- headless 安全，不加 `@key`；结果确定。

## 契约深化（2026-11，lcO1 批）

双侧 16/16 → 69/69。与 Exception 同款家族矩阵 + 层级特化：RuntimeException instanceof Exception 且 instanceof Throwable、is not Error（Class.isAssignableFrom 负例，避开 javac 不相容 instanceof 编译错）；直接父类精确 == Exception.class；SUID -7034897190745766939L（双侧一致锁定）；null 形态构造、initCause/addSuppressed 精确消息、顶帧精确、printStackTrace 全格式、序列化 round-trip。
