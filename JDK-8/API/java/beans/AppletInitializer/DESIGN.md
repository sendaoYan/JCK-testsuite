# java.beans.AppletInitializer — 设计案（JDK-8 ONLY）

> **版本差异（已锁定）**：`java.beans.AppletInitializer` 仅存在于 JDK-8；JDK-9 起 Applet 体系废弃、该接口被**删除**。JDK-27 侧仓库无此目录，无需镜像。

## 一、API 功能
Applet-Bean 初始化回调接口，仅 2 个抽象方法：
- `initialize(java.applet.Applet newAppletBean, java.beans.beancontext.BeanContext bctx)`：`Beans.instantiate(cl, name, ctx, initializer)` 实例化出的 bean 为 Applet 时回调，用于注入 AppletStub/上下文。
- `activate(java.applet.Applet newAppletBean)`：applet 需被激活（等价 start）时回调。
JDK-8 中**非 deprecated**（无 @Deprecated）；不继承任何接口（非 Serializable/EventListener）。它是 JDK-8 独有 4 参 `Beans.instantiate` 重载的参数类型。

## 二、使用方式
`Beans.instantiate(loader, "MyAppletBean", beanContext, new MyAppletInitializer())`：instantiate 完成反射实例化后，若结果是 `java.applet.Applet` 则依次回调 `initialize(applet, ctx)` 与（按环境）`activate(applet)`。Applet 本身 extends java.awt.Panel（AWW 组件，headless 下不实例化）。

## 三、参考来源
- Oracle javadoc（Java SE 8，java.beans.AppletInitializer、java.applet.Applet、Beans.instantiate 4 参重载）。
- JavaBeans 规范 1.01-A（Applet 作为 Bean 的激活协议）；Java SE 8 与 Java SE 9+ 迁移说明（Applet/API 移除）。
- OpenJDK 8 `AppletInitializer.java` 声明与 `Beans.java` 中 initializer 调用点（仅思路，代码自写）。

## 四、测试用例设计 + 易错规范细节
测试文件 `AppletInitializerTest.java`（54 断言，headless 安全——**从不构造 Applet 实例**，applet 实参一律传 null，类型仅经反射核验）：
- `testWhiteBoxStructure`：Class.forName 成功且与编译期类对象同一（两次查找同一 Class）；public 接口、无超类/超接口/泛型/注解；**无 @Deprecated**（`getDeclaredAnnotation(Deprecated.class)==null` 且注解数组空）；恰好 2 个声明方法、成员集恰为 initialize/activate；参数类型**精确**为 `java.applet.Applet` + `java.beans.beancontext.BeanContext`（getDeclaredMethod 精确签名 + 错参 NoSuchMethodException 负例）；void 返回、public abstract、非 default、无异常声明、无方法注解；`java.applet.Applet` 存在性锚点（public 类、extends java.awt.Panel、Serializable）。
- `testRecordingImplementation`（嵌套 `RecordingInitializer`）：计数器从 0 起；`initialize(null, null)` 与 `activate(null)` 均被接受并记录（null 合法）；`initialize(null, ctx)` 记录 **ctx 同一性**（真实 BeanContextSupport 实例）；last-invocation-wins；两实例计数独立。
- `testAssignability`：接口与 BeanContext/Customizer/Visibility/DesignMode/Serializable/EventListener 均无继承关系；`DualImpl` 同时实现 AppletInitializer + Customizer（双协议共存）且 Customizer 侧可用；`Beans` 的 4 参 instantiate 重载经 `getDeclaredMethod(..., AppletInitializer.class)` 锚定（JDK-8 独有，JDK-27 侧在 BeansTest 断言其不存在）。
- 易错点：①headless 下 `new Applet()` 属 AWT 组件创建，本测试规避（null 实参即可覆盖计数/同一性契约）；②接口是**两个**抽象方法，不是函数式接口，不能用 lambda（记录于案，不作编译期断言）；③JDK-9+ 侧若误建此文件会编译失败——该目录仅 JDK-8 存在。
