# java.beans.beancontext.BeanContextServicesSupport.BCSSChild — 设计案

## 1. API 功能
BCSSChild 嵌套类（extends BeanContextSupport.BCSChild），记录子 Bean 的服务请求（包私有构造器）。

## 2. Java 用户如何使用
不直接使用；BeanContextServicesSupport 内部创建。

## 3. 开源代码实际用法调研
JDK 源码 `BeanContextServicesSupport.java` 内部类。

## 4. 测试用例设计 + 自动化实现
白盒：extends BCSChild + public。黑盒（`testBlackBox`）：子类暴露 createBCSChild 间接取得 BCSSChild 实例。

## 5. 深化补充（beansJ 批次，58 断言）
原有 testWhiteBox/testLegacyBlackBox 断言全部保留，追加：完整结构断言（protected 非 static 内部类、Serializable + 父类、UID=-3263851306889194873、字段恰 4 个且 bookkeeping 两字段 transient、方法恰 5 个且包私有非 static、usingService 6 参 + TooManyListenersException/UnsupportedOperationException、构造器唯一包私有 3 参、内嵌恰 BCSSCServiceClassRef/BCSSCServiceRef 两个包私有引用类）；实例行为（CapturingBCSS 捕获）：每 add 一个精确类实例、成员间互异、re-add 换新、独立序列化 round-trip 类保真；per-child 账目隔离：remove 单成员恰释放该成员引用（provider 计数）。
易错点：元素静态类型为 protected BCSChild，外部代码须先 widening 到 Object 再 getClass()；JDK-27 上对 java.desktop 内部字段 setAccessible 被模块系统拒绝（InaccessibleObjectException），观察一律走子类覆写路径。@test 头原有 `@key headful` 行已删除。
