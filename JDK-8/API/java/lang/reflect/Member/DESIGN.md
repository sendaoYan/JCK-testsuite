# java.lang.reflect.Member — 测试设计（JDK-8）

## 1. API 功能

`Member` 是 Field/Method/Constructor（后两者经 `Executable`）实现的公共接口：
- 常量：`PUBLIC == 0`、`DECLARED == 1`——它们是 `Class.getFields/getMethods/
  getConstructors`（PUBLIC 通道）与 `getDeclaredFields/...`（DECLARED 通道）的语义开关。
- 查询：`getDeclaringClass/getName/getModifiers/isSynthetic`。
- JDK-8 无 `accessFlags()` 默认方法（JDK 20 才加入）。

## 2. Java 用户如何使用

以统一视图遍历类成员：文档生成、依赖分析、序列化框架（按修饰符过滤）、
安全审计（找 synthetic/bridge 成员）。PUBLIC/DECLARED 两个常量即
"沿继承链的公开成员" vs "本类声明的全部成员（含私有）" 的等价类划分。

## 3. 参考来源（只列路径）

- Oracle Java SE 8 javadoc：`java/lang/reflect/Member.html`。
- OpenJDK 8 源码：`~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/Member.java`。
- OpenJDK 回归测试：`~/git/jdk8u-dev/jdk/test/java/lang/reflect/`。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/`。

## 4. 测试用例设计（MemberTest.java，7 → 32 断言）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 保留原结构断言（Sample.f 的声明类/名字/修饰符/常量值） | testMember |
| P0 | 常量驱动查找语义：PUBLIC 通道见继承公有字段、DECLARED 通道含私有且不含继承；继承方法声明类==基类 | testConstantsDriveLookups |
| P0 | 字段视图：`CONST` 修饰符==PUBLIC\|STATIC\|FINAL(0x19)、接口常量隐式 public static final、private/protected 精确 | testFieldViews |
| P0 | synthetic 成员：泛型子类 bridge 方法、内部类 `this$0` 字段 | testSyntheticMembers |
| P1 | 三种成员的 instanceof 分派、构造器名==类名、toString 含名字、Member 视图 equals 反射对象 | testKindsAndDispatch |
| P1 | JDK-8 API 面锁定：`Member.class.getMethod("accessFlags")` → NSME | testApiSurfaceLocked |

## 易错规范细节（已锁入断言）

- `Member.PUBLIC/DECLARED` 是**查找通道**常量，与修饰符位（`Modifier.PUBLIC`=0x1）无关。
- 接口常量字段隐式 `public static final`，getModifiers()==0x19。
- 内部类 `this$0`：JDK-8 javac 总是生成且 synthetic；**JDK-22+ javac 在内部类未用外围实例时会整体省略该字段**（27 侧夹具因此让内部类真正引用外围状态）。
- bridge 方法在每个覆写子类各有一份（JDK-8/27 javac 均如此），声明类是该子类。
