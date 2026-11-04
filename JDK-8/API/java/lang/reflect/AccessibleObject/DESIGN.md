# java.lang.reflect.AccessibleObject — 测试设计（JDK-8）

## 1. API 功能

`AccessibleObject` 是 Field/Method/Constructor（经 Executable）的公共基类：
- 访问开关：`setAccessible(boolean)`（SecurityException 受权限控制）、
  静态批量 `setAccessible(AccessibleObject[], boolean)`、`isAccessible`。
- 实现 `AnnotatedElement` 全家族注解读取。
- JDK-8 无 `canAccess(Object)` 与 `trySetAccessible()`（均 JDK-9 加入）。

## 2. Java 用户如何使用

反射访问私有/包私有成员的合法入口：框架依赖注入（Spring/Jackson 反序列化
set 私有字段）、ORM 按字段映射、测试工具注入 mock。JDK-8 无模块系统，
`setAccessible(true)` 对 java.lang 私有成员同样生效（与 JDK-9+ 的
InaccessibleObjectException 相反）。

## 3. 参考来源（只列路径）

- Oracle Java SE 8 javadoc：`java/lang/reflect/AccessibleObject.html`。
- OpenJDK 8 源码：`~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/AccessibleObject.java`。
- OpenJDK 回归测试：`~/git/jdk8u-dev/jdk/test/java/lang/reflect/`、`~/git/jdk/test/jdk/java/lang/reflect/AccessibleObject/`。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/AccessibleObject/`。

## 4. 测试用例设计（AccessibleObjectTest.java，9 → 36 断言）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 保留原断言（标志翻转/注解空查询/批量 API） | testAccessibleObject |
| P0 | JDK-8 API 面锁定：canAccess/trySetAccessible → NSME | testApiSurfaceLocked |
| P0 | 私有字段 拒绝→放行→再拒绝 全周期：IAE 消息含 "access a member of class"；反射写普通读可见 | testPrivateFieldDeniedThenAllowed |
| P0 | static final 字段 setAccessible(true) 后 set → IAE "Can not set static final"、值不变 | testStaticFinalFieldSet |
| P0 | 批量 API 混合三种成员 + 标志独立性 | testBatchMixedKinds |
| P1 | @Repeatable 容器语义：byType 展开 2 个、单实例 getAnnotation==null、容器直接存在、isAnnotationPresent 不穿透 | testRepeatableAnnotations |
| P1 | 类型层次：Field/Method/Executable/Constructor 均是子类、实现 AnnotatedElement | testTypeHierarchy |
| P0 | `String.value` setAccessible(true) 在 JDK-8 **成功**（无模块系统） | testPlatformStringField |

## 易错规范细节（已锁入断言）

- JDK-8 无 nestmate：同文件嵌套类的私有字段即可做拒绝负例（JDK-11+ 需非同巢类）。
- `Can not set static final <type> field <cls>.<name> to <value>`：static final 写拒绝
  与 accessible 标志无关（两侧一致）。
- `isAnnotationPresent`/`getAnnotation` 只看**直接存在**的注解，不穿透 @Repeatable 容器；
  `getAnnotationsByType/getDeclaredAnnotationsByType` 才展开。
- `getDeclaredAnnotations()` 对重复注解返回 1 个容器 + 其它注解（不展开）。
