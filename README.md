# Collections
关于集合的源码分析<br>
Map集合源码分析:<a href="https://github.com/SinceNovember/Collections/blob/master/src/com/liu/Map/Map.md">fenxi</a>
## `ArrayList与LinkList`<br>
### 什么是ArrayList<br>
可以简单的认为是一个动态数组；实际上ArrayList就是用数组实现的，长度不够时，调用Arrays.copyOf方法，拷贝当前数组到一个新的长度更大的数组；<br>
#### ArrayList特点<br>
随机访问速度快，插入和移除性能较差(数组的特点)；<br>
支持null元素；<br>
有顺序；<br>
元素可以重复；<br>
线程不安全；<br>
#### ArrayList接口及方法<br>
##### Iterable接口<br>
实现此接口以便支持foreach语法.<br>
##### Collection接口<br>
int `size()`方法：<br>
返回集合的大小，在ArrayList类中有一个int类型的size私有属性，当调用size方法时，直接返回该属性；<br>
boolean `isEmpty()`方法：<br>
判断集合是否为空，在ArrayList中，通过判断size == 0来判断集合是否为空；<br>
boolean `contains(Object o)`方法：<br>
判断集合是否含有对象o，在ArrayList中，通过判断indexOf(o) >= 0来判断是否含有o对象；<br>
查看indexOf(o)方法，代码如下，主要功能是返回元素第一次出现时的下标索引，所以当下标索引大于等于0时，表示集合中存在该元素：<br>
`Iterator<E> iterator()`方法：<br>
返回一个迭代器对象，用于遍历集合，事实上，ArrayList类里有两个内部类ArrayList.Itr和ArrayList.ListItr，分别对应Iterator迭代器和ListIterator迭代器，后者比前者功能更加强大；从ArrayList.ListItr继承自ArrayList.Itr就可以看出来，ListIterator迭代器支持更多的操作，如判断前面还有没有元素，即hasPrevious()方法，等；<br>
Object[] `toArray()`方法：<br>
将集合ArrayList转换成`Object`数组,有时候需要用到数组的一些api时，可以使用该方法，注意返回的结果是Object类型的数组，如果想返回指定类型的数组，可以使用以下方法，`<T>T[]toArray(T[] a)`;<br>
`<T> T[] toArray(T[] a)`方法：<br>
集合转数组，返回指定类型的数组，注意入参T[] a需要指定数组存储的空间，返回值为指定类型的数组；举个例子，假如有一个Integer类型的集合，如果想把它转换成Integer类型的数组，可以这样写：Integer[] arr = list.toArray(new Integer[list.size()]);<br>
`boolean add(E e)`方法：<br>
在集合最后面增加一个元素，在ArrayList中，其实现就是在其内部数组后面增加一个元素，不过要先保证内部数组长度足够<br>
`boolean remove(Object o)`方法：<br>
在集合中移除对象o，在ArrayList中，其实现较add方法复杂，涉及空对象判断，equals比较，数组移动等，性能相对较差；<br>
`boolean containsAll(Collection<?> c)`方法：<br>
判断是否包含集合c中的所有元素，在ArrayList中，其实现方法是遍历集合c中的每一个元素，调用contains方法，判断集合是否包含该元素，只要有一个不包含就返回false.<br>
`boolean addAll(Collection<? extends E> c)`方法：<br>
将集合c中的所有元素加到目标集合中去，在ArrayList中，其实现是先将集合c转换成数组，然后通过数组拷贝实现；<br>
`boolean removeAll(Collection<?> c)`方法：<br>
移除目标集合中含有‘集合c中元素’的所有元素，在ArrayList中，最终还是操作数组，性能相对较差；<br>
`boolean retainAll(Collection<?> c)`方法：<br>
移除目标集合中‘不包含集合c中元素’的所有元素，在ArrayList中removeAll方法和retainAll方法都是通过调用ArrayList的batchRemove方法来实现的，后续详细了解该方法的实现；<br>
`void clear()`方法：<br>
移除目标集合中的所有元素，在ArrayList中，就是将其内部数组所有元素赋null；<br>
`boolean equals(Object o)和int hashCode()`方法<br>
在ArrayLisy中，上面两个方法都被重写，equals方法依次取出集合中的所有元素进行比较，通过元素的equals方法，判断是否相等，全部相等返回true；<br>
hashCode方法的计算是通过所有元素的hashCode计算得到；顺便说下hashcode，在java中随处可见，一般用在HashMap, Hashtable, HashSet等等中，可用于减少equals方法的调用，快速访问元素等，其实就是散列表的概念，如比较元素先比较其hashcode，如果hashcode不相等，那么这两个元素肯定不相等，也就不用调用其equals方法了；<br>
##### List接口<br>
除了Collection中定义的方法为，该接口增加了以下方法<br>
`boolean addAll(int index, Collection<? extends E> c)`;<br>
在ArrayList中，该方法是在指定位置处增加一个集合中的所有元素，该操作涉及数组移动；<br>
`E get(int index);`<br>
返回下标为index的元素；<br>
`E set(int index, E element)`;<br>
改变下标为index的元素的值<br>
`void add(int index, E element)`;<br>
在下标为index的地方插入元素element，该操作涉及数组移动；<br>
`E remove(int index)`;<br>
移除下标为index的元素，该操作涉及数组移动；<br>
`int indexOf(Object o)`;<br>
返回元素o的最小下标，通过调用o的equals方法与集合中的元素进行比较；<br>
`int lastIndexOf(Object o)`;<br>
返回元素o的最大下标，通过调用o的equals方法与集合中的元素进行比较；<br>
`ListIterator<E> listIterator()`;<br>
返回listIterator迭代器，该迭代器支持向前操作；<br>
`ListIterator<E> listIterator(int index)`;<br>
返回listIterator迭代器，从特定的位置开始，该迭代器支持向前操作；<br>
`List<E> subList(int fromIndex, int toIndex)`;<br>
返回下标在fromIndex和toIndex之间的元素集合；<br>
##### RandomAccess, Cloneable, java.io.Serializable接口
这三个接口是标识接口，里面都是空的；<br>
RandomAccess标识其支持快速随机访问；<br>
Cloneable标识其支持对象复制；<br>
Serializable标识其可序列化；<br>
##### AbstractCollection类<br>
大部分方法前面已经说明过了，不过该类下的contains方法、toArray方法等，遍历的时候都是使用更加通用的迭代器方式进行遍历；<br>
##### AbstractList类<br>
大部分方法前面已经说明过了，不过该类中有两个私有内部类Itr和ListItr，对应的分别是两个迭代器；<br>
##### ArrayList类<br>
ArrayList的具体实现<br>
成员属性：<br>
private static final int DEFAULT_CAPACITY = 10;//初始容量<br>
private static final Object[] EMPTY_ELEMENTDATA = {};//空ArrayList实例共享的一个空数组<br>
private transient Object[] elementData; //真正存储ArrayList中的元素的数组；<br>
private int size;//存储ArrayList的大小，注意不是elementData的长度；<br>
除了其父接口定义的方法外，该类增加了以下方法<br>
`public ArrayList(int initialCapacity)`：<br>
构造函数，指定初始大小<br>
`public ArrayList()`<br>
构造函数，使用共享的EMPTY_ELEMENTDATA空数组<br>
`public ArrayList(Collection<? extends E> c)`<br>
构造函数，通过集合初始化ArrayList<br>
`public void trimToSize()`<br>
节省空间用的，ArrayList是通过数组实现的，大小不够时，增加数组长度，有可能出现数组长度大于ArrayList的size情况；<br>
`public void ensureCapacity(int minCapacity)`<br>
保证ArrayList能容纳minCapacity个元素；<br>
### 什么是LinkedList<br>
List接口的链表实现，并提供了一些队列，栈，双端队列操作的方法；<br>
### LinkedList特点<br>
与ArrayList对比，LinkedList插入和删除操作更加高效，随机访问速度慢；<br>
可以作为栈、队列、双端队列数据结构使用；<br>
非同步，线程不安全；<br>
与ArrayList、Vector一样，LinkedList的内部迭代器存在“快速失败行为”；<br>
支持null元素、有顺序、元素可以重复；
#### LinkedList接口及方法<br>
关于Iterable接口、Collection接口、List接口、 Cloneable、 java.io.Serializable接口、AbstractCollection类、AbstractList类的相关说明，在介绍ArrayList的时候，已经有了个大概说明，这里将主要了解下Queue接口、Deque接口、AbstractSequentialList类以及LinkedList类；<br>
##### Queue接口<br>
`boolean add(E e)`;<br>
将对象e插入队列尾部，成功返回true，失败（没有空间）抛出异常IllegalStateException；<br>
`boolean offer(E e)`;<br>
将对象e插入队列尾部，成功返回true，失败（没有空间）返回false；<br>
`E remove()`;<br>
获取并移除队列头部元素，如果队列为空，抛出NoSuchElementException异常；<br>
`E poll()`;<br>
获取并移除队列头部元素，如果队列为空，返回null；<br>
`E element()`;<br>
获取但不移除队列头部元素，如果队列为空，抛出NoSuchElementException异常；<br>
`E peek()`;<br>
获取但不移除队列头部元素，如果队列为空，返回null；<br>
##### Deque接口<br>
双端队列接口，继承队列接口，支持在队列两端进行入队和出队操作；<br>
除了Collection接口Queue接口中定义的方法外，Deque还包括以下方法<br>
`void addFirst(E e)`;<br>
将对象e插入到双端队列头部，容间不足时，抛出IllegalStateException异常；<br>
`void addLast(E e)`;<br>
将对象e插入到双端队列尾部，容间不足时，抛出IllegalStateException异常；<br>
`boolean offerFirst(E e)`;<br>
将对象e插入到双端队列头部<br>
`boolean offerLast(E e)`;<br>
将对象e插入到双端队列尾部；<br>
`E removeFirst()`;<br>
获取并移除队列第一个元素，队列为空，抛出NoSuchElementException异常；<br>
`E removeLast()`;<br>
获取并移除队列最后一个元素，队列为空，抛出NoSuchElementException异常；<br>
`E pollFirst()`;<br>
获取并移除队列第一个元素，队列为空，返回null；<br>
`E pollLast()`;<br>
获取并移除队列最后一个元素，队列为空，返回null；<br>
`E getFirst()`;<br>
获取队列第一个元素，但不移除，队列为空，抛出NoSuchElementException异常；<br>
`E getLast()`;<br><br>
获取队列最后一个元素，但不移除，队列为空，抛出NoSuchElementException异常；<br>
`E peekFirst()`;<br>
获取队列第一个元素，队列为空，返回null；<br>
`E peekLast()`;<br>
获取队列最后一个元素，队列为空，返回null；<br>
`boolean removeFirstOccurrence(Object o)`<br>
移除第一个满足 (o==null ? e==null : o.equals(e)) 的元素<br>
`boolean removeLastOccurrence(Object o)`;<br>
移除最后一个满足 (o==null ? e==null : o.equals(e)) 的元素<br>
`void push(E e)`;<br>
将对象e插入到双端队列头部;<br>
`E pop()`;<br>
移除并返回双端队列的第一个元素;<br>
`Iterator descendingIterator()`;<br>
双端队列尾部到头部的一个迭代器;<br>
 ##### AbstractSequentialList类<br>
 一个抽象类，基于迭代器实现数据的随机访问,以下方法的含义, 之前也说过，简单地说，就是数据的随机存取（利用了一个索引index）；<br>
`public E get(int index)`<br>
`public E set(int index, E element)`<br>
`public void add(int index, E element)`<br>
`public E remove(int index)`<br>
`public boolean addAll(int index, Collection<? extends E> c)`<br>
##### AbstractSequentialList类<br>
 一个抽象类，基于迭代器实现数据的随机访问,以下方法的含义, 之前也说过，简单地说，就是数据的随机存取（利用了一个索引index）；<br>
`public E get(int index)`<br>
`public E set(int index, E element)`<br>
`public void add(int index, E element)`<br>
`public E remove(int index)`<br>
`public boolean addAll(int index, Collection<? extends E> c)`<br>
##### LinkedList类<br>
LinkedList中有两个关键成员属性，队头结点和队尾结点：<br>
`transient Node<E> first``;  //队头节点<br>
transient Node<E> last`;  //队尾节点<br>
`LinkedList的节点内部类`<br>
`poll方法,出队操作`<br>
`public E get(int index)`方法，随机访问方法<br>
 ### ArrayList与LinkList的区别<br>
1.ArrayList是实现了基于动态数组的数据结构，LinkedList基于链表的数据结构。 <br>
2.对于随机访问get和set，ArrayList觉得优于LinkedList，因为LinkedList要移动指针。 <br>
3.对于新增和删除操作add和remove，LinedList比较占优势，因为ArrayList要移动数据。 <br>
