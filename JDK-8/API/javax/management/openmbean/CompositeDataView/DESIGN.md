# javax.management.openmbean.CompositeDataView — 测试设计

## 1. API 功能
单方法接口：`CompositeData toCompositeData(CompositeType)`——MXBean 转换钩子，允许自定义类型
自己决定如何变成 CompositeData。

## 2. Java 用户如何使用
返回复合结构的类实现该接口，toCompositeData 里按传入的 CompositeType（MXBean 框架按 getter 算出）
打包字段；经 MBeanServer.getAttribute 即得 CompositeData（**MXBean 真闭环**）。

## 3. 参考来源
- Oracle javadoc：CompositeDataView（MXBean 映射规则）。
- OpenJDK jdk 源码 com.sun.jmx.mbeanserver 转换器（CompositeDataView 路径，思路）。
- 本地实测探针（双侧）：本地 MBeanServer 注册 @MXBean 后 getAttribute 的转换结果。

## 4. 测试用例设计（CompositeDataViewTest.java）
- 白盒：单方法接口、签名 (CompositeType)→CompositeData、无父接口。
- 自定义类 Point 实现：toCompositeData 返回受控 CompositeDataSupport（值可控、传入 type 可控、
  二次调用行为）。
- **MXBean 闭环**：PointMXBean{Point getPoint()} + 本地 newMBeanServer，getAttribute("Point") →
  CompositeDataSupport(x=3,y=4)，type 为按 getter 计算的 CompositeType（typeName==类名、
  item 类型==SimpleType.INTEGER）。

### 版本差异（javap + 实测，JDK-8 vs JDK-27）
- 公开 API 面双侧完全一致（javap -public 对照）。
- 已锁定的精确异常消息（IAE/OpenDataException/InvalidKeyException/KeyAlreadyExistsException/
  InvalidOpenTypeException 及带消息 NPE）双侧逐字相同。
- 无消息型异常双侧类型一致、消息文本不同：JDK-27 NPE 带 helpful message（如
  "Cannot invoke ... because elementType is null"）、CCE 带 module/loader 说明，JDK-8 无；
  此类只断言异常类型。
- 反射深入度差异：对 java.management 内部位做 setAccessible 时 JDK-9+ 抛
  InaccessibleObjectException（模块未 open），JDK-8 可打开；测试对两种路径均接受。
