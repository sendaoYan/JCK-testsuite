# javax.management.openmbean.CompositeData — 测试设计

## 1. API 功能
复合数据接口：getCompositeType/get(String)/getAll/containsKey/containsValue/values/
equals/hashCode/toString 共 **9 个声明方法**（**无 keySet**——那是 CompositeType 的能力）。

## 2. Java 用户如何使用
MXBean 复合返回值的运行时形态；自定义实现（如 CompositeDataView.toCompositeData 的返回）或
直接用 CompositeDataSupport。

## 3. 参考来源
- Oracle javadoc：CompositeData（getAll(null/空) 返回空数组的契约、equals 建议）。
- OpenJDK test/jdk/javax/management/openmbean/CompositeDataStringTest.java（toString 思路借鉴）。
- 本地实测探针（双侧）。

## 4. 测试用例设计（CompositeDataTest.java）
- 白盒：接口形状锁定（9 方法精确集合、无 keySet、不 extends Serializable、无父接口）。
- 匿名实现（自带受控行为）+ 参考实现 CompositeDataSupport 双轨实证：get/getAll 顺序、
  getAll(null)==空数组、containsKey/containsValue、values、equals/hashCode 顺序无关、toString。

### 版本差异（javap + 实测，JDK-8 vs JDK-27）
- 公开 API 面双侧完全一致（javap -public 对照）。
- 已锁定的精确异常消息（IAE/OpenDataException/InvalidKeyException/KeyAlreadyExistsException/
  InvalidOpenTypeException 及带消息 NPE）双侧逐字相同。
- 无消息型异常双侧类型一致、消息文本不同：JDK-27 NPE 带 helpful message（如
  "Cannot invoke ... because elementType is null"）、CCE 带 module/loader 说明，JDK-8 无；
  此类只断言异常类型。
- 反射深入度差异：对 java.management 内部位做 setAccessible 时 JDK-9+ 抛
  InaccessibleObjectException（模块未 open），JDK-8 可打开；测试对两种路径均接受。
