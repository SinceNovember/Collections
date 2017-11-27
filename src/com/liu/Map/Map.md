## Map源码分析
### Map关系图
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/Map.jpg)
## `HashMap`
### 什么`HashMap`
  基于哈希表的一个Map接口实现，存储的对象是一个键值对对象(Node实现(Map.Entry(key,value)接口),它根据键的hashCode值存储数据，大多数情况下可以直接定位到它的值，因而具有很快的访问速度，但遍历顺序却是不确定的。 HashMap最多只允许一条记录的键为null，允许多条记录的值为null。HashMap非线程安全，即任一时刻可以有多个线程同时写HashMap，可能会导致数据的不一致。如果需要满足线程安全，可以用 Collections的synchronizedMap方法使HashMap具有线程安全的能力，或者使用ConcurrentHashMap。<br>
### HashMap特点
1.底层实现是 链表数组，JDK 8 后又加了 红黑树
2.实现了 Map 全部的方法
3.key 用 Set 存放，所以想做到 key 不允许重复，key 对应的类需要重写 hashCode 和 equals 方法
4.允许空键和空值（但空键只有一个，且放在第一位，下面会介绍）
5.元素是无序的，而且顺序会不定时改变
6.插入、获取的时间复杂度基本是 O(1)（前提是有适当的哈希函数，让元素分布在均匀的位置）
7.遍历整个 Map 需要的时间与 桶(数组) 的长度成正比（因此初始化时 HashMap 的容量不宜太大）
8.两个关键因子：初始容量、加载因子
### 内部实现
从结构实现来讲，HashMap是数组+链表+红黑树（JDK1.8增加了红黑树部分）实现的，如下如所示。
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/3.png)
###  分析HashMap的put方法
HashMap的put方法执行过程可以通过下图来理解:
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/5.png)
1.判断键值对数组table[i]是否为空或为null，否则执行resize()进行扩容；
2.根据键值key计算hash值得到插入的数组索引i，如果table[i]==null，直接新建节点添加，转向⑥，如果table[i]不为空，转向③；
3.判断table[i]的首个元素是否和key一样，如果相同直接覆盖value，否则转向④，这里的相同指的是hashCode以及equals；
4.判断table[i] 是否为treeNode，即table[i] 是否是红黑树，如果是红黑树，则直接在树中插入键值对，否则转向⑤；
5.遍历table[i]，判断链表长度是否大于8，大于8的话把链表转换为红黑树，在红黑树中执行插入操作，否则进行链表的插入操作；遍历过程中若发现key已经存在直接覆盖value即可；
6.插入成功后，判断实际存在的键值对数量size是否超多了最大容量threshold，如果超过，进行扩容。
### 扩容机制分析
  扩容(resize)就是重新计算容量，向HashMap对象里不停的添加元素，而HashMap对象内部的数组无法装载更多的元素时，对象就需要扩大数组的长度，以便能装入更多的元素。当然Java里的数组是无法自动扩容的，方法是使用一个新的数组代替已有的容量小的数组，就像我们用一个小桶装水，如果想装更多的水，就得换大水桶。
  它同一位置上新元素总会被放在链表的头部位置；这样先放在一个索引上的元素终会被放到Entry链的尾部(如果发生了hash冲突的话），这一点和Jdk1.8有区别，下文详解。在旧数组中同一条Entry链上的元素，通过重新计算索引位置后，有可能被放到了新数组的不同位置上。
下面举个例子说明下扩容过程。假设了我们的hash算法就是简单的用key mod 一下表的大小（也就是数组的长度）。其中的哈希桶数组table的size=2， 所以key = 3、7、5，put顺序依次为 5、7、3。在mod 2以后都冲突在table[1]这里了。这里假设负载因子 loadFactor=1，即当键值对的实际大小size 大于 table的实际大小时进行扩容。接下来的三个步骤是哈希桶数组 resize成4，然后所有的Node重新rehash的过程。
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/6.png)<br>
  下面我们讲解下JDK1.8做了哪些优化。经过观测可以发现，我们使用的是2次幂的扩展(指长度扩为原来2倍)，所以，元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。看下图可以明白这句话的意思，n为table的长度，图（a）表示扩容前的key1和key2两种key确定索引位置的示例，图（b）表示扩容后key1和key2两种key确定索引位置的示例，其中hash1是key1对应的哈希与高位运算结果。
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/7.png)<br>
元素在重新计算hash之后，因为n变为2倍，那么n-1的mask范围在高位多1bit(红色)，因此新的index就会发生这样的变化：
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/10.png)<br>
因此，我们在扩充HashMap的时候，不需要像JDK1.7的实现那样重新计算hash，只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”，可以看看下图为16扩充为32的resize示意图：
![](https://github.com/SinceNovember/Collections/blob/master/extendsimages/8.png)<br>
这个设计确实非常的巧妙，既省去了重新计算hash值的时间，而且同时，由于新增的1bit是0还是1可以认为是随机的，因此resize的过程，均匀的把之前的冲突的节点分散到新的bucket了。这一块就是JDK1.8新增的优化点。<br>
### Map接口方法
`int size()`：获取Map中Entry的长度。<br>
`boolean isEmpty()`;判断Map是否为空。<br>
`boolean containsKey(Object key)`;判断指定的key在Map中是否存在。<br>
`boolean containsValue(Object value)`;判断指定的value在Map中是否存在。<br>
`V get(Object key)`;通过指定的获取相应的值。<br>
`V put(K key, V value)`;添加Entry实体元素。<br>
`V remove(Object key)`;移除指定key的节点。<br> 
`void putAll(Map <? extends K,? extends V> m)`;将Map结合元素全部添加到此Map中。<br> 
`void clear()`;将Map中的元素全部清空。<br>
`boolean replace(K key, V oldValue, V newValue)`;将指定的key-value实体，的值替换为指定值。<br>
`Set<K> keySet();`返回Map中所有的key值存放到一个set中并返回。<br>
` Collection<V> values();`返回Map中所有的value值存放到一个Collection中并返回。<br>
`Set<Map.Entry<K, V>> entrySet();`返回Map中所有的Entry值存放到一个Set中并返回。<br>
 keySet与values以及entrySet中有个迭代器方法，其中包含：<br>
`hashNext()`;判断是否还有下一个实体，返回布尔值。<br>
`next()`;返回下一个实体。<br>
