# javax.imageio.metadata.IIOMetadataFormatImpl — DESIGN

## 1. API 功能
`IIOMetadataFormatImpl` 是 `IIOMetadataFormat` 接口的可复用抽象实现，插件作者继承它以声明原生元数据格式。方法分组：
- 常量/工厂：`public static final String standardMetadataFormatName`（`"javax_imageio_1.0"`）、`static IIOMetadataFormat getStandardFormatInstance()`。
- 构造器：`(String rootName, int childPolicy)`（不可为 REPEAT）、`(String rootName, int minChildren, int maxChildren)`（REPEAT）。
- 受保护构建方法：`addElement(...)` 两种、`addChildElement`、`removeElement`、`addAttribute(...)` 五种（arbitrary/enumeration/range/list/boolean）、`removeAttribute`、`addObjectValue(...)` 四种（arbitrary/enumeration/range/list）、`removeObjectValue`、`setResourceBaseName/getResourceBaseName`。
- 查询（实现接口）：`getRootName`、`getChildPolicy`、`getChildNames`、`getElementMin/MaxChildren`、`getElementDescription`、`getAttributeNames/ValueType/DataType/...`、`getObjectValueType/Class/...`。
- 抽象：`canNodeAppear(String, ImageTypeSpecifier)` 留给子类实现。

## 2. Java 用户如何使用
插件作者继承并构造时用受保护的 `add*` 方法声明格式，用户则通过 `getMetadataFormat(name)` 或 `getStandardFormatInstance()` 拿到实例后查询：
```java
class MyFormat extends IIOMetadataFormatImpl {
  MyFormat() { super("root", CHILD_POLICY_SEQUENCE);
    addElement("item", "root", CHILD_POLICY_REPEAT, 1, 4);
    addAttribute("item", "name", DATATYPE_STRING, true, "unnamed");
    addObjectValue("item", Integer.class, true, 1); }
  public boolean canNodeAppear(String e, ImageTypeSpecifier t) { return true; }
}
```
标准格式：`IIOMetadataFormat f = IIOMetadataFormatImpl.getStandardFormatInstance();`

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码 + test/jdk）
- `src/java.desktop/share/classes/javax/imageio/metadata/IIOMetadataFormatImpl.java:75`：类定义 `implements IIOMetadataFormat`；`:168`/`:204` 两个构造器校验 rootName、childPolicy、min/max 并抛 `IllegalArgumentException`。
- 同文件 `:1230`：`createStandardFormat()` 惰性创建 `new StandardMetadataFormat()`；`:1244` `getStandardFormatInstance()` 返回单例。
- `src/java.desktop/share/classes/com/sun/imageio/plugins/common/StandardMetadataFormat.java`：标准 `javax_imageio_1.0` 格式实现。
- `test/jdk/javax/imageio/metadata/IIOMetadataFormatImplTest.java`（bug 4403350/4403352/4436995/4438977）：内联子类覆盖受保护 `addAttribute`，验证 `getElementMin/MaxChildren`、`getAttributeMin/MaxValue`、`getAttributeDescription`、`getAttributeEnumerations`。
- `test/jdk/javax/imageio/metadata/GetChildNames.java`：验证 `getChildNames` 对 `CHILD_POLICY_EMPTY` 节点返回 null。

## 4. 测试用例设计 + 自动化实现（`IIOMetadataFormatImplTest.testBlackBox`）
- 内联 `TestImpl` 声明完整结构：SEQUENCE 根、REPEAT item、EMPTY meta、`addChildElement` 复用、三种属性（string/boolean）与三种对象值（arbitrary/enumeration/list），随后查询全部 getter 验证。
- 变更方法：`removeAttribute`、`addAttribute(list)`、`removeElement` 后观察查询结果。
- 资源描述：无 ResourceBundle 时 `getElementDescription/getAttributeDescription` 返回 null。
- 标准格式单例：`getStandardFormatInstance()` 非空、root 名正确、两次调用同一实例。
- 构造器契约异常：null root、`(root,policy)` 传 REPEAT、负 min、min>max 均抛 `IllegalArgumentException`。
- 查询契约异常：非 REPEAT 取 min/max children、非枚举取 enumerations、未知属性/元素均抛 `IllegalArgumentException`。
