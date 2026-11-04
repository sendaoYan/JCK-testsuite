# javax.management.openmbean.CompositeDataInvocationHandler — 测试设计

## 1. API 功能
InvocationHandler 实现：`CompositeDataInvocationHandler(CompositeData)` 把接口 getter（getX/isX）
映射到 CompositeData 字段（X→x 大小写两探）；getCompositeData 返回原实例。
**javap 双侧确认无 setCompositeData、不实现 Serializable**。

## 2. Java 用户如何使用
从 MXBean 代理拿到的 CompositeData 反向恢复成强类型接口（JMX.newMXBeanProxy 内部即此机制）。

## 3. 参考来源
- Oracle javadoc：CompositeDataInvocationHandler（方法名→item 名规则、异常）。
- OpenJDK jdk 源码同类的 MXBean proxy 机制（思路）。
- 本地实测探针（双侧）。

## 4. 测试用例设计（CompositeDataInvocationHandlerTest.java）
- 正例：int/String/boolean getter 精确取值（isActive→active）、getCompositeData==同一实例、
  直接 invoke 返回裸值、一个 handler 供两个接口共享、Object 方法路由（toString=="Proxy["+cd+"]"、
  equals/hashCode 委托数据、equals(null)/equals(non-proxy) 不抛）、换新 handler 即新数据。
- 负例精确表：null 构造参数 → IAE 消息恰为 "compositeData"；非 getter 方法 → IAE
  "Method is not getter: doSomething"；getter 无对应字段 → IAE "No CompositeData item Missing or
  missing to match getMissing"；getter 返回类型与字段不符 → ClassCastException（类型断言）。

### 版本差异（javap + 实测，JDK-8 vs JDK-27）
- 公开 API 面双侧完全一致（javap -public 对照）。
- 已锁定的精确异常消息（IAE/OpenDataException/InvalidKeyException/KeyAlreadyExistsException/
  InvalidOpenTypeException 及带消息 NPE）双侧逐字相同。
- 无消息型异常双侧类型一致、消息文本不同：JDK-27 NPE 带 helpful message（如
  "Cannot invoke ... because elementType is null"）、CCE 带 module/loader 说明，JDK-8 无；
  此类只断言异常类型。
- 反射深入度差异：对 java.management 内部位做 setAccessible 时 JDK-9+ 抛
  InaccessibleObjectException（模块未 open），JDK-8 可打开；测试对两种路径均接受。
