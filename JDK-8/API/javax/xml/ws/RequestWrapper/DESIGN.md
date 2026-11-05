# javax.xml.ws.RequestWrapper 测试设计

## 1. API 功能

`javax.xml.ws.RequestWrapper` 标注于 SEI 方法（`ElementType.METHOD`），为 document/literal wrapped 风格指定请求包装 bean 的元数据：`localName`（包装元素本地名）、`targetNamespace`（命名空间）、`className`（包装 bean 全限定类名）、`partName`（WSDL part 名）。wsimport 生成代码与 wsgen 生成 WSDL 时消费这些值；运行时反射亦可回读。

## 2. 使用方式

```java
@WebService(name = "EchoPort", targetNamespace = "urn:t")
interface EchoSei {
    @RequestWrapper(localName = "echo", targetNamespace = "urn:t",
                    className = "com.example.EchoRequestBean", partName = "parameters")
    String echo(String s);
}
```
本测试以 SEI 三形态（全成员 / 裸标注 / 未标注）做反射回读：全成员四元组逐项精确；裸标注经实例回读四成员均 `""`；未标注 null。

## 3. 参考来源

- Oracle javadoc（Java SE 8）: docs.oracle.com/javase/8/docs/api/javax/xml/ws/RequestWrapper.html
- JSR 224 (JAX-WS 2.2) §7.9（wrapper 注解）
- OpenJDK 8u RI（JAX-WS 2.2 API 注解源码）
- 本仓库测试：`JDK-8/API/javax/xml/ws/RequestWrapper/RequestWrapperTest.java`

## 4. 测试用例设计 + 易错规范细节

维度清单：
- 结构/元注解：成员集合恰 `{localName, targetNamespace, className, partName}` 四个，全为 String、public abstract、全带默认；RUNTIME / Target 恰 {METHOD} / Documented / 非 Inherited 非 Repeatable / 声明元注解恰 3。
- 默认值：逐成员 `getDefaultValue()` 均为空串。
- 回读：全成员四元组、裸标注实例默认值、equals/hashCode/toString、未标注方法 null、类级不适用。

易错规范细节：
1. **成员是 4 个，不是 3 个**：`partName` 常被遗漏（不少资料只列 localName/targetNamespace/className）；断言 declaredMethods.length == 4。
2. 默认值全部空串 `""`（不是 null）；省略成员时实例访问自动走默认。
3. 注解消费发生在工具链（wsimport/wsgen）层，纯运行时无消费者——本包只做反射闭环，不必（也不能）断言 WSDL 生成行为。
4. 与 `@ResponseWrapper` 同方法共存合法，各自成员独立；本地名习惯上为 `操作名` 与 `操作名+"Response"`，但规范不强制——不要把命名习惯当断言（除非在自己标注的辅助类上回读自己写的值）。
