# javax.management.openmbean.CompositeDataView — 测试设计

## 1. API 功能
标记接口，指示实现类提供 CompositeData 视图：`CompositeData toCompositeData(CompositeType ct)`。
含类型转换帮助。实现类可把自身转成指定 CompositeType 的 CompositeData。

## 2. Java 用户如何使用
自定义类实现 CompositeDataView，暴露 toCompositeData(CompositeType)，MXBean 映射时据此转 open data。
链路：自定义类 implements CompositeDataView → toCompositeData(ct) → CompositeDataSupport。

## 3. 开源代码实际用法调研
- JDK 自用：MXBean 映射检查类型是否 implements CompositeDataView 以提供自定义 open data 视图
  （`~/git/jdk25u-dev/src/java.management/share/classes/com/sun/jmx/mbeanserver/`）。
- JDK 测试：`~/git/jdk25u-dev/test/jdk/javax/management/openmbean/CompositeDataViewTest.java` 覆盖契约。
- 来源为 JDK 源码与 test/jdk（web 不可用）。

## 4. 测试用例设计（CompositeDataViewTest.java）
- 白盒：public interface、单一抽象方法 toCompositeData(CompositeType) 存在。
- 黑盒：匿名/具名实现 toCompositeData 返回 CompositeDataSupport；断言返回结构与字段（轻行为）。
