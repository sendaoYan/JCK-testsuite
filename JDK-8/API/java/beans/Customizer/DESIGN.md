# java.beans.Customizer — 设计案（JDK-8）

## 一、API 功能
`java.beans.Customizer` 是 Bean 定制器接口（JavaBeans 规范的编辑器协议），仅 3 个抽象方法、无常量、无默认方法：
- `setObject(Object)`：注入被定制 bean（允许 null，接口不做任何校验）。
- `addPropertyChangeListener(PropertyChangeListener)` / `removePropertyChangeListener(...)`：注册/注销编辑事件监听器——定制器在用户修改属性时须发 `PropertyChangeEvent`（实现方自持 `PropertyChangeSupport`，接口本身不携带支持类）。
javadoc 约定实现应为 GUI 组件（通常经 `BeanDescriptor.getCustomizerClass()` 关联）；本测试 headless，只测接口机制，不继承 Component。

## 二、使用方式
工具箱双击 bean 弹出定制面板：`Introspector.getBeanInfo(Foo.class).getBeanDescriptor().getCustomizerClass()` 取定制器类 → 实例化 → `setObject(bean)` → 面板编辑时 fire PropertyChangeEvent → 工具回写属性并持久化。

## 三、参考来源
- Oracle javadoc（Java SE 8，java.beans.Customizer）：三方法协议、"a customizer is expected to be a GUI component"、"fires PropertyChangeEvent when the user changes the target bean"。
- JavaBeans 规范 1.01-A（§9.3 Customizers、§5.4 PropertyChangeEvent 通道）。
- OpenJDK 8 `Customizer.java` 声明与 `test/jdk/java/beans` 回归思路（仅思路，代码自写）。

## 四、测试用例设计 + 易错规范细节
测试文件 `CustomizerTest.java`（64 断言）：
- `testWhiteBoxStructure`：接口、无超类/超接口/泛型/注解；**恰好 3 个声明方法**且成员集恰为 `setObject(Object)` / `add/removePropertyChangeListener(PropertyChangeListener)`；全部 public abstract、void 返回、非 default、无异常声明、无方法注解。
- `testSetObjectContract`（嵌套 `BasicCustomizer`）：初值 null、`setObject(null)` 被接受、记录入参**同一性**、last-set-wins、接受任意类型。
- `testPropertyChangeWiring`：定制器自有 PropertyChangeSupport 的完整接线——事件 source==定制器、name/old/new 逐字段；双监听器；remove 后不再通知；重复 remove no-op；移除从未注册的监听器无害；**同一监听器注册两次 → 每次 fire 通知两次，单次 remove 只注销一条**（ArrayList.remove 单次删除语义，JDK-8/JDK-27 实测一致）；old==new 被 PCS 抑制。
- `testIntrospectorIntegration`：显式 `PlainBeanBeanInfo.getBeanDescriptor()` 发布 `BasicCustomizer.class` → `Introspector.getBeanInfo().getBeanDescriptor().getCustomizerClass()` 精确取回；单参 `BeanDescriptor` 的 customizerClass 为 null。
- `testCrossTypeAssignability`：非 EventListener/Serializable，与 DesignMode/Visibility/BeanContext 无继承关系；`!Component.isAssignableFrom(BasicCustomizer)`（记录 headless 取舍）。
- 易错点：①接口不要求也不提供 PCS，"fire 事件" 是**实现义务**而非接口能力——断言必须落在实现侧；②PCS 重复注册是两条独立记录；③customizer 类须经 public 嵌套类暴露给 BeanDescriptor（Introspector 要求可访问）。
