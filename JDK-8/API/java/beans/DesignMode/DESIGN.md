# java.beans.DesignMode — 设计案（JDK-8）

## 一、API 功能
`java.beans.DesignMode` 是设计期标记协议接口（JavaBeans 1.1 引入）：常量 `PROPERTYNAME = "designTime"` + 两个抽象方法 `setDesignTime(boolean)` / `isDesignTime()`。接口本身**无状态**——由实现自持 flag；`BeanContext extends DesignMode`，beancontext 实现各自维护。与 `Beans.setDesignTime` 的全局（ThreadGroupContext）开关**相互独立**（实测 jdk8u372 与 jdk-27.0.1 一致：`new BeanContextSupport()` 不从 Beans 读种子值，context 改 flag 也不写回 Beans）。

## 二、使用方式
设计工具查询嵌套 bean：`if (child instanceof DesignMode && ((DesignMode) child).isDesignTime()) ...`；BeanContext 容器实现本接口向成员暴露"当前容器是否设计期"；监听方按 `PROPERTYNAME`（"designTime"）注册属性变更。

## 三、参考来源
- Oracle javadoc（Java SE 8，java.beans.DesignMode / Beans.isDesignTime）。
- JavaBeans 规范 1.01-A + java.beans.beancontext 包规范（BeanContext 的 designTime 协议）。
- OpenJDK 8 `DesignMode.java`、`BeanContextSupport.java`（designTime 字段为容器本地值）与 `test/jdk/java/beans` 回归思路。

## 四、测试用例设计 + 易错规范细节
测试文件 `DesignModeTest.java`（51 断言）：
- `testWhiteBoxStructure`：接口、无超接口/泛型/注解、**恰好 2 个声明方法**（成员集恰为 isDesignTime/setDesignTime）；签名精确（`setDesignTime(boolean)→void`、`isDesignTime()→boolean`）；全部 public abstract、非 default、无异常、无注解；`PROPERTYNAME` public static final String == "designTime"，且是唯一声明字段。
- `testImplementationRoundTrip`（嵌套 `RecordingDesignMode`）：初值 false、true/false 存取、幂等、序列 set 后 **last-set-wins**、两实例状态互不影响。
- `testInterfaceIsStateless`：`Beans.setDesignTime(true)`（finally 恢复）不影响已存在/新建实现的 flag——证明接口不携带状态。
- `testBeanContextIntegration`：`BeanContextSupport instanceof DesignMode`；context 自身 flag 往返；context→Beans、Beans→context **双向不传播**（探针两侧一致）。
- `testCrossTypeAssignability`：与 Customizer/Visibility 无关、非 Serializable/EventListener、实现类可赋值给接口。
- 易错点：①易误以为 Beans.setDesignTime 会同步到 BeanContextSupport——不会，两者是独立状态域；②接口无默认方法（Java 8 时代接口），反射 `isDefault()` 断言防 JDK-27 侧悄悄加 default；③断言默认值 false 前须确认本 JVM 内无先前测试污染（本文件内 finally 恢复）。
