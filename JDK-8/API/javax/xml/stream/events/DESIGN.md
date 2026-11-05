# javax.xml.stream.events — 目录级设计案

## 范围
14 个事件类型目录：XMLEvent（根接口）+ StartElement/EndElement/Characters/Comment/ProcessingInstruction/
StartDocument/EndDocument/Namespace/Attribute/EntityReference/EntityDeclaration/DTD/NotationDeclaration。
events 子包无 package-info 测试。

## 统一测试方法
1. 两条实例化路径全部覆盖：XMLEventFactory 工厂形态矩阵 + XMLEventFactory 解析固定 XML 获得真实事件
   （XML 全部内嵌字符串，含 DTD 内部子集形态）。
2. 事件码精确断言（XMLStreamConstants 15 常量：1..8、9=ENTITY_REFERENCE、10=ATTRIBUTE、11=DTD、12=CDATA、
   13=NAMESPACE、14=NOTATION_DECLARATION、15=ENTITY_DECLARATION）。
3. isXxx 谓词族每事件恰一个真；asXxx 窄化：类型正确返回 this，错误类型抛 ClassCastException。
4. writeAsEncodedUnicode 输出串锁定（含转义矩阵与 impl 括号形态 quirk）。
5. 负例：未声明前缀属性、未解析实体引用、错误状态 getElementText 等异常路径。

## 已锁定的 JDK 实现怪癖（JDK-8 与 JDK-27 双侧一致）
- Reader 输入解析的 StartDocument.getCharacterEncodingScheme()=null（即使 decl 声明 encoding；InputStream 输入才有值）。
- 解析的 CDATA 事件 isCData()=false（默认分配器不保留 CDATA 标志；只有 createCData 为 true）。
- 工厂 createAttribute/createNamespace 的 isSpecified()=false；解析侧属性 isSpecified()=true。
- 工厂 createStartElement 不把传入的 Namespace 迭代器接入 getNamespaceURI/getNamespaceContext（均 null）。
- 工厂与解析的 EndElement.getNamespaces() 恒空（不保留 out-of-scope 命名空间，工厂侧直接吞掉迭代器）。
- 编码输出使用 ['ns']:prefix:local 括号形态（QName.toString 风格），默认命名空间写成 xmlns:=。
- 子元素间空白报 CHARACTERS(4) 而非 SPACE(6)；预定义实体/字符引用切分为多个 CHARACTERS 事件。

## 双侧差异（各自锁定）
- 解析无 standalone 声明的文档：StartDocument.standaloneSet() JDK-8=true（与规范不符）、JDK-27=false（规范正确）。
  StartDocumentTest 用 SA_SET_WHEN_ABSENT 常量参数化，两侧仅此一行不同。
