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

	static class Entry<K,V> extends HashMap.Node<K,V> { //�µĽ�㣬�̳���ԭ��HashMap�ڵ�����ݣ�Ȼ������������µ�ʵ�������before����֮ǰ��ʵ�壬after����֮���ʵ�壬��
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }
	transient LinkedHashMap.Entry<K,V> head;//˫������ͷ�ڵ㣬transient˵�����ܱ����л�
	transient LinkedHashMap.Entry<K,V> tail;//˫���б�β�ڵ㣬transient˵�����ܱ����л�
	final boolean accessOrder;//�ж��Ƿ����LRU��������
	@SuppressWarnings("unused")
	private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {//��˫����p������˫����ĩβ
        LinkedHashMap.Entry<K,V> last = tail;//������β�ڵ㸳ֵ��һ���½ڵ�
        tail = p;//��p�ڵ㸳ֵ��β�ڵ�
        if (last == null)
            head = p;
        else {
            p.before = last;//��ԭ����β�ڵ����²�����ĩβ�еĽڵ�����໥����
            last.after = p;
        }
    }
	//��dst�ڵ��滻src�ڵ�
	   @SuppressWarnings("unused")
	private void transferLinks(LinkedHashMap.Entry<K,V> src,
			   LinkedHashMap.Entry<K,V> dst) {
	        LinkedHashMap.Entry<K,V> b = dst.before = src.before;//��ԭ�������ϵ�src��ǰ��һ���ڵ��ʵ�帳ֵ��dst��ǰ�ڵ㣨��dstָ��srcָ��Ľڵ㣩
	        LinkedHashMap.Entry<K,V> a = dst.after = src.after;//ͬ�� ָ���ڵ�
	        if (b == null)
	            head = dst;
	        else
	            b.after = dst;//ǰ�ڵ�ָ��dst�ڵ�
	        if (a == null)
	            tail = dst;
	        else
	            a.before = dst;//��ڵ�ָ��dst�ڵ�
	    }
	   void reinitialize() {//��ʼ��
	        super.reinitialize();
	        head = tail = null;
	    }
	   //�����½ڵ㣬�����½ڵ��������ĩβ
	   Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
	        LinkedHashMap.Entry<K,V> p =
	            new LinkedHashMap.Entry<K,V>(hash, key, value, e);//�����½ڵ�
	        linkNodeLast(p);//�ڵ��������β
	        return p;
	    }
	   Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {//��ָ���ڵ���ָ�����һ���ڵ��趨Ϊָ������һ���ڵ�
	        LinkedHashMap.Entry<K,V> q = (LinkedHashMap.Entry<K,V>)p;
	        LinkedHashMap.Entry<K,V> t =
	            new LinkedHashMap.Entry<K,V>(q.hash, q.key, q.value, next);
	        transferLinks(q, t);
	        return t;
	    }

	    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {//ͬ�ϣ��ǹ��ں����ڵ�
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
	    void afterNodeRemoval(Node<K,V> e) { //��������ȥ��ָ���Ľڵ�
	        LinkedHashMap.Entry<K,V> p =
	            (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;//��ָ���ڵ��ǰ�ڵ����ڵ㸳����ʵ��
	        p.before = p.after = null;	 //��p��ǰ��ָ���
	        if (b == null)
	            head = a;//��ǰ�ڵ����ڵ㻥����������ָ���ڵ�
	        else
	            b.after = a;
	        if (a == null)
	            tail = b;
	        else
	            a.before = b;
	    }
	    void afterNodeInsertion(boolean evict) { // �Ƴ�˫�����ͷ���
	        LinkedHashMap.Entry<K,V> first;
	        if (evict && (first = head) != null && removeEldestEntry(first)) {
	            K key = first.key;
	            removeNode(hash(key), key, null, false, true);
	        }
	    }
	    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
	        return false;
	    }
	    void afterNodeAccess(Node<K,V> e) { // ���з��ʷ�ĩβ���ص��������ܶຯ������put���ж��ᱻ�ص���������˳��Ϊtrue���ҷ��ʵĶ�����β��㣬�����ʵĽڵ����˫����ĩβ��
	        LinkedHashMap.Entry<K,V> last;//��������β�ڵ�
	        if (accessOrder && (last = tail) != e) {//���accessOrderΪtrue����e��Ϊβ���ڵ�
	            LinkedHashMap.Entry<K,V> p =
	                (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;//b����e��ǰһ���ڵ㣬a����p��һ���ڵ�
	            p.after = null;//��p��after���
	            if (b == null)
	                head = a;
	            else
	                b.after = a;//e��ǰ�ڵ�ָ���ڵ�
	            if (a != null)
	                a.before = b;//e�ĺ�ڵ�ָ��e��ǰ�ڵ�
	            else
	                last = b;
	            if (last == null)
	                head = p;
	            else {
	                p.before = last;//p��ǰָ��ԭβ�ڵ�
	                last.after = p;//ԭβ�ڵ��ָe�ڵ㣬����e�ڵ��β��
	            }
	            tail = p;//��e��β���ڵ�
	            ++modCount;
	        }
	    }
	    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {//��s�����д������
	        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after) {//д��ÿ���ڵ�ļ�ֵ
	            s.writeObject(e.key);
	            s.writeObject(e.value);
	        }
	    }
	    public LinkedHashMap(int initialCapacity, float loadFactor) {//���캯�������ø��๹�캬��
	        super(initialCapacity, loadFactor);
	        accessOrder = false;//Ĭ�ϲ����н��������Ľڵ��ĩβ����
	    }
	    public LinkedHashMap(int initialCapacity) {//δָ���������ӣ�Ĭ��0.75
	        super(initialCapacity);
	        accessOrder = false;
	    }
	    public LinkedHashMap() {//�����Լ��������Ӿ�Ĭ�ϣ�16��0.75
	        super();
	        accessOrder = false;
	    }
	    public LinkedHashMap(Map<? extends K, ? extends V> m) {//��Map�е�Ԫ�ط����Map��
	        super();
	        accessOrder = false;
	        putMapEntries(m, false);
	    }
	    public LinkedHashMap(int initialCapacity,
                float loadFactor,
                boolean accessOrder) {
	    	super(initialCapacity, loadFactor);
	    	this.accessOrder = accessOrder;//ָ���Ƿ�LRU
	    }
	    public boolean containsValue(Object value) {//��˫�������в�ѯ�Ƿ���ڴ�valueֵ
	        for (LinkedHashMap.Entry<K,V> e = head; e != null; e = e.after) {
	            V v = e.value;
	            if (v == value || (value != null && value.equals(v)))
	                return true;
	        }
	        return false;
	    }
	    public V get(Object key) {//ͨ������ֵ
	        Node<K,V> e;
	        if ((e = getNode(hash(key), key)) == null)//ʹ�ø����ȡ��㡣
	            return null;
	        if (accessOrder)
	            afterNodeAccess(e);//�ص���������e������ĩβ
	        return e.value;
	    }
	    public V getOrDefault(Object key, V defaultValue) {//��õ�keyֵ���ݷ���idefaultValue���ݣ��ص�
	        Node<K,V> e;
	        if ((e = getNode(hash(key), key)) == null)
	            return defaultValue;
	        if (accessOrder)
	            afterNodeAccess(e);
	        return e.value;
	    }
	    public void clear() {//���Map������
	        super.clear();
	        head = tail = null;
	    }
	    public Set<K> keySet() {//��ȡ����key����Set��
	        Set<K> ks = keySet;
	        if (ks == null) {
	            ks = new LinkedKeySet();
	            keySet = ks;
	        }
	        return ks;
	    }
	    final class LinkedKeySet extends AbstractSet<K> {//Map���н���
	        public final int size()                 { return size; }
	        public final void clear()               { LinkedHashMap.this.clear(); }
	        public final Iterator<K> iterator() {//������
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
	    public Collection<V> values() {//��ȡ����Value����Collection��
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
	    public Set<Map.Entry<K,V>> entrySet() {//��ȡ����Entryʵ��
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
	    abstract class LinkedHashIterator {//����������ʵ�ֳ�����
	        LinkedHashMap.Entry<K,V> next;//��һ��Linked�ڵ�
	        LinkedHashMap.Entry<K,V> current;//��ǰLinked�ڵ�
	        int expectedModCount;

	        LinkedHashIterator() {//��ʼ��
	            next = head;
	            expectedModCount = modCount;
	            current = null;
	        }

	        public final boolean hasNext() {//��һ���Ƿ����
	            return next != null;
	        }

	        final LinkedHashMap.Entry<K,V> nextNode() {//��һ���ڵ�
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

	    final class LinkedKeyIterator extends LinkedHashIterator//key�ĵ�����ʵ��
	        implements Iterator<K> {
	        public final K next() { return nextNode().getKey(); }
	    }

	    final class LinkedValueIterator extends LinkedHashIterator//value������ʵ��
	        implements Iterator<V> {
	        public final V next() { return nextNode().value; }
	    }

	    final class LinkedEntryIterator extends LinkedHashIterator//Entry������ʵ��
	        implements Iterator<Map.Entry<K,V>> {
	        public final Map.Entry<K,V> next() { return nextNode(); }
	    }


	    

	
	
}
