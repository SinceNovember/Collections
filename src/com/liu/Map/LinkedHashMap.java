package com.liu.Map;

import java.util.function.Consumer;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.io.IOException;

public class LinkedHashMap<K,V> extends HashMap<K, V>implements Map<K, V>{
	// @Fields serialVersionUID : TODO
	private static final long serialVersionUID = 3801124242820219131L;

	static class Entry<K,V> extends HashMap.Node<K,V> { //新的结点，继承了原来HashMap节点的内容，然后添加了俩个新的实体变量，before代表之前的实体，after代表之后的实体，。
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }
	transient LinkedHashMap.Entry<K,V> head;//双向链表头节点，transient说明不能被序列化
	transient LinkedHashMap.Entry<K,V> tail;//双向列表尾节点，transient说明不能被序列化
	final boolean accessOrder;//判断是否进行LRU规则排序
	@SuppressWarnings("unused")
	private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {//将双链表p结点放在双链表末尾
        LinkedHashMap.Entry<K,V> last = tail;//将链表尾节点赋值给一个新节点
        tail = p;//将p节点赋值给尾节点
        if (last == null)
            head = p;
        else {
            p.before = last;//将原来的尾节点与新插入在末尾中的节点进行相互链接
            last.after = p;
        }
    }
	//用dst节点替换src节点
	   @SuppressWarnings("unused")
	private void transferLinks(LinkedHashMap.Entry<K,V> src,
			   LinkedHashMap.Entry<K,V> dst) {
	        LinkedHashMap.Entry<K,V> b = dst.before = src.before;//将原本链表上的src的前面一个节点的实体赋值给dst的前节点（将dst指向src指向的节点）
	        LinkedHashMap.Entry<K,V> a = dst.after = src.after;//同上 指向后节点
	        if (b == null)
	            head = dst;
	        else
	            b.after = dst;//前节点指向dst节点
	        if (a == null)
	            tail = dst;
	        else
	            a.before = dst;//后节点指向dst节点
	    }
	   void reinitialize() {//初始化
	        super.reinitialize();
	        head = tail = null;
	    }
	   //创建新节点，并将新节点放在链表末尾
	   Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
	        LinkedHashMap.Entry<K,V> p =
	            new LinkedHashMap.Entry<K,V>(hash, key, value, e);//创建新节点
	        linkNodeLast(p);//节点插入链表尾
	        return p;
	    }
	   Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {//将指定节点所指向的下一个节点设定为指定的下一个节点
	        LinkedHashMap.Entry<K,V> q = (LinkedHashMap.Entry<K,V>)p;
	        LinkedHashMap.Entry<K,V> t =
	            new LinkedHashMap.Entry<K,V>(q.hash, q.key, q.value, next);
	        transferLinks(q, t);
	        return t;
	    }

	    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {//同上，是关于红黑书节点
	        TreeNode<K,V> p = new TreeNode<K,V>(hash, key, value, next);
	        linkNodeLast(p);
	        return p;
	    }

	    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
	        LinkedHashMap.Entry<K,V> q = (LinkedHashMap.Entry<K,V>)p;
	        TreeNode<K,V> t = new TreeNode<K,V>(q.hash, q.key, q.value, next);
	        transferLinks(q, t);
	        return t;
	    }
	    void afterNodeRemoval(Node<K,V> e) { //从链表中去除指定的节点
	        LinkedHashMap.Entry<K,V> p =
	            (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;//将指定节点的前节点与后节点赋给新实例
	        p.before = p.after = null;	 //将p的前后指向空
	        if (b == null)
	            head = a;//将前节点与后节点互相链接跳过指定节点
	        else
	            b.after = a;
	        if (a == null)
	            tail = b;
	        else
	            a.before = b;
	    }
	    void afterNodeInsertion(boolean evict) { // 移除双链表的头结点
	        LinkedHashMap.Entry<K,V> first;
	        if (evict && (first = head) != null && removeEldestEntry(first)) {
	            K key = first.key;
	            removeNode(hash(key), key, null, false, true);
	        }
	    }
	    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
	        return false;
	    }
	    void afterNodeAccess(Node<K,V> e) { // 进行访问放末尾，回掉函数，很多函数（如put）中都会被回调。若访问顺序为true，且访问的对象不是尾结点，将访问的节点放在双链表末尾。
	        LinkedHashMap.Entry<K,V> last;//用于引用尾节点
	        if (accessOrder && (last = tail) != e) {//如果accessOrder为true并且e不为尾部节点
	            LinkedHashMap.Entry<K,V> p =
	                (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;//b引用e的前一个节点，a引用p下一个节点
	            p.after = null;//将p的after清空
	            if (b == null)
	                head = a;
	            else
	                b.after = a;//e的前节点指向后节点
	            if (a != null)
	                a.before = b;//e的后节点指向e的前节点
	            else
	                last = b;
	            if (last == null)
	                head = p;
	            else {
	                p.before = last;//p的前指向原尾节点
	                last.after = p;//原尾节点后指e节点，即将e节点放尾部
	            }
	            tail = p;//将e放尾部节点
	            ++modCount;
	        }
	    }
	    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {//像s输出流写入数据
	        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after) {//写入每个节点的键值
	            s.writeObject(e.key);
	            s.writeObject(e.value);
	        }
	    }
	    public LinkedHashMap(int initialCapacity, float loadFactor) {//构造函数，调用父类构造含糊
	        super(initialCapacity, loadFactor);
	        accessOrder = false;//默认不进行将操作过的节点放末尾操作
	    }
	    public LinkedHashMap(int initialCapacity) {//未指定加载因子，默认0.75
	        super(initialCapacity);
	        accessOrder = false;
	    }
	    public LinkedHashMap() {//容量以及加载因子军默认，16，0.75
	        super();
	        accessOrder = false;
	    }
	    public LinkedHashMap(Map<? extends K, ? extends V> m) {//将Map中的元素放入此Map中
	        super();
	        accessOrder = false;
	        putMapEntries(m, false);
	    }
	    public LinkedHashMap(int initialCapacity,
                float loadFactor,
                boolean accessOrder) {
	    	super(initialCapacity, loadFactor);
	    	this.accessOrder = accessOrder;//指定是否开LRU
	    }
	    public boolean containsValue(Object value) {//从双向链表中查询是否存在此value值
	        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after) {
	            V v = e.value;
	            if (v == value || (value != null && value.equals(v)))
	                return true;
	        }
	        return false;
	    }
	    public V get(Object key) {//通过键获值
	        Node<K,V> e;
	        if ((e = getNode(hash(key), key)) == null)//使用父类获取结点。
	            return null;
	        if (accessOrder)
	            afterNodeAccess(e);//回掉函数，将e放链表末尾
	        return e.value;
	    }
	    public V getOrDefault(Object key, V defaultValue) {//获得到key值内容返回idefaultValue内容，回掉
	        Node<K,V> e;
	        if ((e = getNode(hash(key), key)) == null)
	            return defaultValue;
	        if (accessOrder)
	            afterNodeAccess(e);
	        return e.value;
	    }
	    public void clear() {//清除Map中数据
	        super.clear();
	        head = tail = null;
	    }
	    public Set<K> keySet() {//获取所有key放入Set中
	        Set<K> ks = keySet;
	        if (ks == null) {
	            ks = new LinkedKeySet();
	            keySet = ks;
	        }
	        return ks;
	    }
	    final class LinkedKeySet extends AbstractSet<K> {//Map中有介绍
	        public final int size()                 { return size; }
	        public final void clear()               { LinkedHashMap.this.clear(); }
	        public final Iterator<K> iterator() {//迭代器
	            return new LinkedKeyIterator();
	        }
	        public final boolean contains(Object o) { return containsKey(o); }
	        public final boolean remove(Object key) {
	            return removeNode(hash(key), key, null, false, true) != null;
	        }
	        public final Spliterator<K> spliterator()  {
	            return Spliterators.spliterator(this, Spliterator.SIZED |
	                                            Spliterator.ORDERED |
	                                            Spliterator.DISTINCT);
	        }
	        public final void forEach(Consumer<? super K> action) {
	            if (action == null)
	                throw new NullPointerException();
	            int mc = modCount;
	            for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
	                action.accept(e.key);
	            if (modCount != mc)
	                throw new ConcurrentModificationException();
	        }
	    }
	    public Collection<V> values() {//获取所有Value放入Collection中
	        Collection<V> vs = values;
	        if (vs == null) {
	            vs = new LinkedValues();
	            values = vs;
	        }
	        return vs;
	    }
	    final class LinkedValues extends AbstractCollection<V> {
	        public final int size()                 { return size; }
	        public final void clear()               { LinkedHashMap.this.clear(); }
	        public final Iterator<V> iterator() {
	            return new LinkedValueIterator();
	        }
	        public final boolean contains(Object o) { return containsValue(o); }
	        public final Spliterator<V> spliterator() {
	            return Spliterators.spliterator(this, Spliterator.SIZED |
	                                            Spliterator.ORDERED);
	        }
	        public final void forEach(Consumer<? super V> action) {
	            if (action == null)
	                throw new NullPointerException();
	            int mc = modCount;
	            for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
	                action.accept(e.value);
	            if (modCount != mc)
	                throw new ConcurrentModificationException();
	        }
	    }
	    public Set<Map.Entry<K,V>> entrySet() {//获取所有Entry实体
	        Set<Map.Entry<K,V>> es;
	        return (es = entrySet) == null ? (entrySet = new LinkedEntrySet()) : es;
	    }
	    final class LinkedEntrySet extends AbstractSet<Map.Entry<K,V>> {
	        public final int size()                 { return size; }
	        public final void clear()               { LinkedHashMap.this.clear(); }
	        public final Iterator<Map.Entry<K,V>> iterator() {
	            return new LinkedEntryIterator();
	        }
	        public final boolean contains(Object o) {
	            if (!(o instanceof Map.Entry))
	                return false;
	            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
	            Object key = e.getKey();
	            Node<K,V> candidate = getNode(hash(key), key);
	            return candidate != null && candidate.equals(e);
	        }
	        public final boolean remove(Object o) {
	            if (o instanceof Map.Entry) {
	                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
	                Object key = e.getKey();
	                Object value = e.getValue();
	                return removeNode(hash(key), key, value, true, true) != null;
	            }
	            return false;
	        }
	        public final Spliterator<Map.Entry<K,V>> spliterator() {
	            return Spliterators.spliterator(this, Spliterator.SIZED |
	                                            Spliterator.ORDERED |
	                                            Spliterator.DISTINCT);
	        }
	        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
	            if (action == null)
	                throw new NullPointerException();
	            int mc = modCount;
	            for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
	                action.accept(e);
	            if (modCount != mc)
	                throw new ConcurrentModificationException();
	        }
	    }
	    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
	        if (function == null)
	            throw new NullPointerException();
	        int mc = modCount;
	        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after)
	            e.value = function.apply(e.key, e.value);
	        if (modCount != mc)
	            throw new ConcurrentModificationException();
	    }
	    abstract class LinkedHashIterator {//迭代器内容实现抽象类
	        LinkedHashMap.Entry<K,V> next;//下一个Linked节点
	        LinkedHashMap.Entry<K,V> current;//当前Linked节点
	        int expectedModCount;

	        LinkedHashIterator() {//初始化
	            next = head;
	            expectedModCount = modCount;
	            current = null;
	        }

	        public final boolean hasNext() {//下一个是否存在
	            return next != null;
	        }

	        final LinkedHashMap.Entry<K,V> nextNode() {//下一个节点
	            LinkedHashMap.Entry<K,V> e = next;
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            if (e == null)
	                throw new NoSuchElementException();
	            current = e;
	            next = e.after;
	            return e;
	        }

	        public final void remove() {
	            Node<K,V> p = current;
	            if (p == null)
	                throw new IllegalStateException();
	            if (modCount != expectedModCount)
	                throw new ConcurrentModificationException();
	            current = null;
	            K key = p.key;
	            removeNode(hash(key), key, null, false, false);
	            expectedModCount = modCount;
	        }
	    }

	    final class LinkedKeyIterator extends LinkedHashIterator//key的迭代器实现
	        implements Iterator<K> {
	        public final K next() { return nextNode().getKey(); }
	    }

	    final class LinkedValueIterator extends LinkedHashIterator//value迭代器实现
	        implements Iterator<V> {
	        public final V next() { return nextNode().value; }
	    }

	    final class LinkedEntryIterator extends LinkedHashIterator//Entry迭代器实现
	        implements Iterator<Map.Entry<K,V>> {
	        public final Map.Entry<K,V> next() { return nextNode(); }
	    }


	    

	
	
}
