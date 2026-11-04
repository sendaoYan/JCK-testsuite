# java.beans.Visibility — 设计案（JDK-8）

## 一、API 功能
`java.beans.Visibility` 是 GUI 需求协商协议接口（4 个抽象方法，无常量、无默认方法）：
- `needsGui()`：bean **内在是否**需要 GUI（不随 dontUseGui/okToUseGui 变化）。
- `dontUseGui()`：通知 bean 停止/避免使用 GUI；`okToUseGui()`：通知可以恢复。
- `avoidingGui()`：当前是否处于"避开 GUI"状态。
语义（javadoc）：`avoidingGui` 仅在 `dontUseGui()` 之后为 true、`okToUseGui()` 后复位；`needsGui()==true` 的 bean 也可以被要求 dontUseGui（此时仍在但隐藏）。`BeanContext extends Visibility`，故 `BeanContextSupport` 实现本接口。

## 二、使用方式
无头环境/服务器容器查询嵌套 bean：`needsGui()` 决定是否允许加入；环境变化时对全体成员广播 `dontUseGui()/okToUseGui()`；序列化前用 `avoidingGui()` 决定是否剥离 GUI 部分。

## 三、参考来源
- Oracle javadoc（Java SE 8，java.beans.Visibility；BeanContext.needsGui 协作说明）。
- JavaBeans 规范 1.01-A §11（Visibility 协议）与 java.beans.beancontext 规范。
- OpenJDK 8 `Visibility.java`、`BeanContextSupport.java`（needsGui/avoidingGui 实现）与 `test/jdk/java/beans` 回归思路。

## 四、测试用例设计 + 易错规范细节
测试文件 `VisibilityTest.java`（68 断言）：
- `testWhiteBoxStructure`：接口、无超接口/泛型/注解/字段；**恰好 4 个声明方法**且成员集精确匹配四方法协议；逐方法签名（needsGui():boolean、dontUseGui():void、okToUseGui():void、avoidingGui():boolean，均无参）；全部 public abstract、非 default、无异常、无注解。
- `testGuiBeanMatrix`（嵌套 `GuiBean`，needsGui 恒 true）：初始 avoidingGui false；dontUseGui 后 avoidingGui true 且 **needsGui 仍 true**（内在需求不因禁用而消失）；okToUseGui 复位；重复 dontUseGui 幂等。
- `testHeadlessBeanMatrix`（嵌套 `HeadlessBean`，needsGui 恒 false）：初始 avoiding false（未被要求前不算"避开"）；dontUseGui 置位、okToUseGui 复位；needsGui 全程 false 不变量。
- `testInstanceIndependence`：avoiding 状态按实例隔离。
- `testBeanContextIntegration`：`BeanContextSupport instanceof Visibility` + Class 可赋值断言；两种 bean 加入 context（contains/size）后各自 Visibility 语义**原样保留**（嵌套中 dontUseGui 仍生效、membership 不受可见性迁移影响）。
- `testCrossTypeAssignability`：与 DesignMode/Customizer 无关、非 Serializable/EventListener、两个实现类可赋值给接口。
- 易错点：①needsGui 与 avoidingGui 是两个正交维度（"需要"vs"被禁"），容易写成互斥断言；②`BeanContext.add` 接受任意 Object 成员（含非 BeanContextChild），实测两侧一致；③JDK-27 侧 BeanContextSupport 已 deprecated-for-removal 但仍可实例化，测试继续覆盖并加 `@SuppressWarnings("removal")`。
