# javax.xml.stream.util.XMLEventAllocator — 设计案

## 1. API 功能
事件分配器接口：allocate(XMLStreamReader)→XMLEvent、allocate(XMLStreamReader, XMLEventConsumer)、
newInstance()。经 XMLInputFactory 属性 "javax.xml.stream.allocator" 安装。

## 2. Java 用户如何使用
自定义实现传入 XMLInputFactory.setProperty("javax.xml.stream.allocator", alloc)，随后
createXMLEventReader 产出的每个事件都来自分配器；或直接对已定位的 XMLStreamReader 调 allocate。

## 3. 开源代码实际用法调研
JDK 内建分配器 com.sun.xml.internal.stream.events.XMLEventAllocatorImpl（XMLEventReaderImpl 内使用）；
本仓库 XMLEventAllocatorTest。

## 4. 测试用例设计 + 易错细节（契约深化，testDeep()）
- 发现机制闭环（双侧实测生效）：setProperty 不抛异常且 getProperty 返回同一实例；
  createXMLEventReader 后 7 个事件（<r><c>x</c></r>）全部为分配器返回的同一 marker 实例（== 判定），
  allocate 恰被调用 7 次（每事件一次，含 START/END_DOCUMENT）。
- 直接调用：allocate(定位到 START_ELEMENT 的 reader) 返回自定义事件；
  allocate(reader, consumer) 把事件喂给 consumer（与 XMLEventConsumer 联动闭环）。
- newInstance()：返回可独立工作的新实例（!= 原实例，allocate 行为一致）。
- 负例：setProperty 传非 XMLEventAllocator 的 String —— setProperty 本身不抛（存入后惰性转型），
  createXMLEventReader 时抛 ClassCastException（消息含 XMLEventAllocator）。
