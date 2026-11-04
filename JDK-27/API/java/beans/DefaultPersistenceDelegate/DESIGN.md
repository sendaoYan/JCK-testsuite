# java.beans.DefaultPersistenceDelegate 测试设计案

## 1. API 功能
PersistenceDelegate 的默认具体实现。构造器 `()`（空构造器路径：无参构造 + 属性 diff）与 `(String[] constructorPropertyNames)`（按属性名顺序取 getter 值作构造器实参；null 数组视为空）。属性差异通过与"新实例"逐属性比较决定是否发 setter 语句；PropertyDescriptor 带 `transient=TRUE` 属性值时跳过；public 非 final/static/transient 实例字段也参与持久化。

## 2. 使用方式
满足 JavaBean 约定的类由 XMLEncoder 自动套用；不可变类/无空构造器类注册 `new DefaultPersistenceDelegate(new String[]{"a","b"})`（无需 @ConstructorProperties 注解）。

## 3. 参考来源
- Oracle javadoc：Java SE 8 (`java/beans/DefaultPersistenceDelegate.html`, docs.oracle.com/javase/8/docs/api) 与 JDK 27 (docs.oracle.com/en/java/javase/27/docs/api/java.desktop/java/beans/DefaultPersistenceDelegate.html)。
- JavaBeans 规范 1.01-A（属性/事件/方法命名约定；持久化委托模型见规范第 5 章及 XMLEncoder javadoc）。
- OpenJDK 源码与回归思路（借鉴语义，代码自写）：`jdk8u-dev/jdk/src/share/classes/java/beans/{PersistenceDelegate,DefaultPersistenceDelegate,Encoder,XMLEncoder,XMLDecoder}.java`；`jdk/test/jdk/java/beans/XMLDecoder`、`jdk/test/jdk/java/beans/XMLEncoder`（round-trip/异常路由回归）；`jdk/test/jdk/java/beans/Statement`（默认 ExceptionListener 打印 "Continuing ..." 的行为）。
- 探针实证：本批所有不确定行为先在 jdk8u372-b07 与 jdk-27.0.1 双侧跑探针定案（见负例表）。

## 4. 测试用例设计（DefaultPersistenceDelegateTest，JDK-8 PASS 59 / JDK-27 PASS 58，旧均 3）
| 测试方法 | 覆盖点 |
|---|---|
| testWhiteBox | extends PersistenceDelegate、具体类、两构造器 public |
| testBlackBox | 保留旧版：两构造器实例化 instanceof |
| testConstructorNameArrays | null 名单数组受容且行为等同无参委托；空数组受容 |
| testDiffingEmission | 全默认值 bean → XML **无** `void property`；改 x/s → 各自出现；只改 x → s 被抑制 |
| testSetterNotInvokedWhenEqual | 解码侧 setter 计数：默认值 0 次、改动属性恰 1 次（克隆环境执行与解码执行分开计数） |
| testConstructorPropertyNames | {"a","b"} 路径：构造实参 `<int>5</int>`/`<string>five</string>`，无 property 语句，round-trip |
| testConstructorOnlyBean | 无空构造器、无 setter 的类经名单构造器仍完整 round-trip |
| testUnknownPropertyName | "nope" → 首个异常 IllegalStateException（消息含属性名）+ discarding 包装，输出无 object |
| testNullPropertyName | 名单含 null → 首个异常 IllegalArgumentException("Property name is null") |
| testMissingNullaryConstructor | NoDefault → 首个异常 InstantiationException（cause NoSuchMethodException）+ discarding |
| testTransientPropertySkipped | 显式 BeanInfo 标 transient 的属性不落盘、解码保持默认值；未标属性正常 |
| testPublicFieldPersistence | public 字段经 method="getField"/"set" 落盘并 round-trip |
| testMutatesToSemantics | 无名单：同类即 true；有名单+声明 equals（String）：不等即 false；有名单+未声明 equals：同类 true；有名单+null 旧值 → NPE |
| testDefaultDelegateRegistration | 未知类型映射到 DPD；注册自定义后可查询；setPersistenceDelegate(cls,null) 回退默认 |
| testNamesArrayCopied | 构造后外部改名单数组不影响委托（内部 clone） |

## 易错规范细节
- setter 计数类测试必须把静态计数器在 encode 与 decode 之间清零——编码器在克隆环境里也会执行 setter。
- 显式 BeanInfo 类必须是**同级嵌套类**（如 `FooBeanInfo` 与 `Foo` 同层）；嵌进 bean 内部类 Introspector 找不到，transient 标记失效（实测）。
- 名单路径的三连异常（IllegalStateException → NoSuchMethodException → Exception("XMLEncoder: discarding...")）都进 listener，断言只锚第一个。

## 版本差异（实测）
- 无公开 API 差异；仅 JDK-27 侧旧文件保留了原 whitebox 断言集合，新块两侧一致。

## 附：本批之前的旧设计案（原样保留）
# java.beans.DefaultPersistenceDelegate — 设计案

## 1. API 功能
默认持久化委托（extends PersistenceDelegate）。构造器 `()` 与 `(String[] constructorPropertyNames)`；用默认构造器 + 属性 setter 重建对象。

## 2. Java 用户如何使用
XMLEncoder 对满足 JavaBean 约定的类自动使用此委托。

## 3. 开源代码实际用法调研
JDK 源码 `DefaultPersistenceDelegate.java`；XMLEncoder 默认委托。

## 4. 测试用例设计 + 自动化实现
白盒：extends PersistenceDelegate。黑盒（`testBlackBox`）：两个构造器实例化。
