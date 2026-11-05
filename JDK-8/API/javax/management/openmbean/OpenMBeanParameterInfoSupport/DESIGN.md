# javax.management.openmbean.OpenMBeanParameterInfoSupport — 测试设计

## 1. API 功能
OpenMBeanParameterInfo 的可序列化实现（extends MBeanParameterInfo）。构造形态：
(name,desc,openType)、泛型 <T>(name,desc,OpenType<T>,T defaultValue)、(…,T min,T max)、
(…,T[] legalValues)、(name,desc,openType,Descriptor)。构造期校验顺序（源码级）：
openType null → IllegalArgumentException("openType cannot be null")；
name/desc null → IllegalArgumentException("name/description cannot be null")；
default 错型 → OpenDataException("defaultValue's class does not match openType…")；
legal 与 min/max 互斥 → IllegalArgumentException("cannot have both legalValues and min/max"）；
min>max → IllegalArgumentException("minValue cannot be greater than maxValue")。
Descriptor 形态自动派生 defaultValue/minValue/maxValue/legalValues/openType 字段。

## 2. Java 用户典型用法
描述操作参数约束并交给 OpenMBeanOperationInfoSupport/ConstructorInfoSupport；equals/hashCode
参与 Info 层集合比较；序列化用于远程 MBeanServer 传输。

## 3. 参考来源
- Oracle javadoc 同名类；OpenJDK jdk8u-dev/jdk25u 源码（校验消息原文逐条核对）。
- OpenJDK test/jdk/javax/management/openmbean/OpenMBeanParameterInfoSupportTest（思路）。

## 4. 测试用例设计 + 易错细节（OpenMBeanParameterInfoSupportTest，批 G：148→185 断言）
- 构造矩阵：五形态全走 getter；plain 形态 has* 全 false、getLegalValues()==null。
- **负例精确异常表**（OpenDataException 为 checked，其余 IAE unchecked）：
  openType null/name null/desc null → IAE 精确消息；错型 default（用 **raw OpenType** 绕过泛型
  编译期拦截，OpenType<?> 会 CAP#1 失败）→ OpenDataException；min>max、legal+min 互斥 → IAE；
  default 不在 [min,max]、default 不在 legal、legal 元素错型 → OpenDataException。
- equals/hashCode：default/min/max/legal 逐项参与、desc 不参与；isValue(null) 有 default 时 true。
- 序列化：round-trip equals、SUID 锁定（两侧一致）。
- Descriptor 形态：约束自动写入 descriptor 字段、不可变、原 descriptor 自定义字段保留；
  descriptor 中错型 defaultValue → IllegalArgumentException（OpenDataException 被包装、消息一致）。
- 版本差异：两侧零差异（探测 diff 为空）。
