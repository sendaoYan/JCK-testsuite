# java.lang.reflect.AccessibleObject — 测试设计（JDK-27）

## 1. API 功能
`AccessibleObject` 是 Field/Method/Constructor 的公共基类（protected 构造器）：
- 访问开关：`setAccessible(boolean)`、静态批量 `setAccessible(AccessibleObject[], boolean)`、`isAccessible()`、`trySetAccessible()`、`canAccess(Object)`。
- 注解契约（实现 `AnnotatedElement`）：`isAnnotationPresent/getAnnotation/getAnnotations/getAnnotationsByType/getDeclaredAnnotation/getDeclaredAnnotationsByType/getDeclaredAnnotations`。
- 子类继承后可直接以基类引用统一操作 Field/Method/Constructor。

## 2. Java 用户如何使用
对私有成员 `ao.setAccessible(true)`（或批量/`trySetAccessible` 探测）→ 之前抛 `IllegalAccessException` 的 `get/invoke/newInstance` 变为可用；JDK-9+ 模块系统下对未 open 的包 `setAccessible(true)` 抛 `InaccessibleObjectException`、`trySetAccessible()` 返回 false。`canAccess(obj)` 按调用者视角查询：实例成员需对应类型实例（null 抛 IAE）、静态成员必须传 null（非 null 抛 IAE）。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/AccessibleObject/`（CanAccessTest 等用例）：`canAccess` 对 null/非 null obj 的校验与 `trySetAccessible` 语义。
- Spring `ReflectionUtils.makeAccessible(AccessibleObject)`：以基类引用统一打开可达性（与场景 3 批量操作同构）。
- Commons Lang `MemberUtils`/`FieldUtils.removeFinalSuppression`：批量处理 `AccessibleObject[]`。
- JDK 源码 `AccessibleObject.setAccessible(AccessibleObject[], boolean)` 被 `java.util.Objects` 等内部批处理使用（`~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/AccessibleObject.java`）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AccessibleObjectTest.testBlackBox`，顶层类 `AccessibleSeparate` 提供跨 nest 私有成员）：
1. `canAccess`：public 实例字段 true；静态成员 null true、非 null `IllegalArgumentException`；实例成员 null/错型 `IllegalArgumentException`。
2. 跨 nest 私有成员 `canAccess` false → `trySetAccessible()` true → `canAccess` true → `setAccessible(false)` 复原。
3. 批量 `setAccessible(数组, true/false)` 覆盖 Method+Constructor+Field 混合成员。
4. `@Deprecated` 方法经基类注解契约 7 种查询 + 缺失注解两查询。
5. 行为闭环：`get` 前置 `IllegalAccessException` → `setAccessible(true)` 后 get/set 往返。
6. 未 open 模块：`String.class.getDeclaredField("value")` 上 `setAccessible(true)` 抛 `InaccessibleObjectException`、`trySetAccessible()` false。
白盒保留原有开关断言 + 类层次/接口检查。产出：`AccessibleObjectTest.java`（43 断言通过）。

## 深化批次（2026-11-05）

新增 7 个 test 方法（45 → 72 断言）：

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | API 面锁定：canAccess/trySetAccessible 存在（8 侧为 NSME） | testApiSurfaceLocked27 |
| P0 | canAccess 正路径（实例配接收者 true、静态 null true）+ 三类 IAE 消息片段（"null object for"/"is not an instance of"/"non-null object for"） | testCanAccessMessages |
| P0 | trySetAccessible 失败路径（String.value → false 不抛、标志不动）、成功路径（自身类私有 true）与幂等 | testTrySetAccessiblePaths |
| P0 | 私有字段 拒绝→放行→再拒绝（IAE "access a member of class"，需非同巢类） | testPrivateFieldDeniedThenAllowed |
| P0 | static final 写拒绝 "Can not set static final"、值不变 | testStaticFinalFieldSet |
| P1 | 批量 API 含 Executable 的混合成员 + 标志独立 | testBatchMixedKinds |
| P1 | @Repeatable 容器语义（byType 展开/getAnnotation null/isAnnotationPresent 不穿透） | testRepeatableAnnotations |

### 锁定的版本差异（本批实测）
- canAccess IAE 消息（JDK-27）：实例成员传 null → "null object for <方法签名>"；静态成员传非 null → "non-null object for <签名>"；类型错 → "object of type X is not an instance of Y"。
- `setAccessible(true)` 拒绝消息：JDK-27 为 `class X cannot access a member of class Y with private access`，JDK-8 为 `Class X can not access a member of class Y with modifiers "private"`——公共子串 "access a member of class"。
- `String.value`：JDK-27 setAccessible 抛 InaccessibleObjectException / trySetAccessible 返回 false；JDK-8 直接成功。
