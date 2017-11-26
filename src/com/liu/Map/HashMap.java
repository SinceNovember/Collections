package com.liu.Map;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class HashMap<K,V> extends AbstractMap<K, V>implements Map<K, V>,Cloneable,Serializable{
	 private static final long serialVersionUID = 362498820763181265L;
	 /**
	     * Ĭ�ϳ�ʼ������Ĭ��Ϊ2��4�η� = 16��2��n�η���Ϊ�˼ӿ�hash�����ٶȣ���������hash��ͻ������h & (length-1)����1111111
	     */
	 static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; 
	 /**
	     * ���������Ĭ��Ϊ2��30�η���
	     */
	 static final int MAXIMUM_CAPACITY = 1 << 30;
	 /**
	     * Ĭ�ϸ������ӣ�Ĭ��Ϊ0.75
	     */
	 static final float DEFAULT_LOAD_FACTOR = 0.75f;
	/// ��Ͱ(bucket)�ϵĽ�����������ֵʱ��ת�ɺ����
	    static final int TREEIFY_THRESHOLD = 8; 
	 // ��Ͱ(bucket)�ϵĽ����С�����ֵʱ��ת����
	 static final int UNTREEIFY_THRESHOLD = 6;
	/// Ͱ�нṹת��Ϊ�������Ӧ��table����С��С
	 static final int MIN_TREEIFY_CAPACITY = 64;
	 final float loadFactor;//��������
	 transient Node<K,V>[] table;//Node���飬���ÿ��Entry
	 transient Set<Map.Entry<K,V>> entrySet;//ÿ��Entry��ŵ�Set��
	 transient int size;//��ǰMap��key-valueӳ��ĸ���
	 transient int modCount;//Hash��ṹ���޸Ĵ���������ʵ�ֵ���������ʧ����Ϊ
	 int threshold;//// �ٽ�ֵ ��ʵ�ʴ�С(����*�������)�����ٽ�ֵʱ�����������
	  /** 
	 * ��̬�ڲ��࣬һ������࣬������������Ϊhashֵ������ֵ
	 */
	transient Set<K>        keySet;
    transient Collection<V> values;
	static class Node<K,V> implements Map.Entry<K,V> {
	        final int hash;//hashֵ
	        final K key;//�洢�ļ�
	        V value;//�洢��ֵ
	        Node<K,V> next;//ָ����һ��Entry

	        Node(int hash, K key, V value, Node<K,V> next) {
	            this.hash = hash;
	            this.key = key;
	            this.value = value;
	            this.next = next;
	        }

			@Override
			public K getKey() {//��ȡ��ǰ���ļ�
				return key;
			}

			@Override
			public V getValue() {//��ȡ��ǰ���ļ�
				// TODO Auto-generated method stub
				return value;
			}
			   public final String toString() { return key + "=" + value; }
			   public final int hashCode() {//��ȡ��ǰ��hashֵ
		            return Objects.hashCode(key) ^ Objects.hashCode(value);
		        }
			@Override
			public V setValue(V newValue) {//���õ�ǰ����ֵΪ��ֵ
				  V oldValue = value;
		            value = newValue;
		            return oldValue;
			}
			public final boolean equals(Object o) {//�ж�����Ķ����뵱ǰ��Entry�Ƿ����
	            if (o == this)
	                return true;
	            if (o instanceof Map.Entry) { //�ж������������Entry��ʵ��
	                Map.Entry<?,?> e = (Map.Entry<?,?>)o;//ǿ��ת��ΪEntry
	                if (Objects.equals(key, e.getKey()) &&
	                    Objects.equals(value, e.getValue()))//�ж��뵱ǰ�ļ�ֵ�Ƿ����
	                    return true;
	            }
	            return false;
	        }
	 }
	/**  
	* @Title: hash  
	* @Description: ͨ������ļ�����hashֵ
	*/  
	static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
	  static Class<?> comparableClassFor(Object x) {
	        if (x instanceof Comparable) {
	            Class<?> c; Type[] ts, as; Type t; ParameterizedType p;
	            if ((c = x.getClass()) == String.class) // bypass checks
	                return c;
	            if ((ts = c.getGenericInterfaces()) != null) {
	                for (int i = 0; i < ts.length; ++i) {
	                    if (((t = ts[i]) instanceof ParameterizedType) &&
	                        ((p = (ParameterizedType)t).getRawType() ==
	                         Comparable.class) &&
	                        (as = p.getActualTypeArguments()) != null &&
	                        as.length == 1 && as[0] == c) // type arg is c
	                        return c;
	                }
	            }
	        }
	        return null;
	    }
	  @SuppressWarnings({"rawtypes","unchecked"}) // for cast to Comparable
	    static int compareComparables(Class<?> kc, Object k, Object x) {
	        return (x == null || x.getClass() != kc ? 0 :
	                ((Comparable)k).compareTo(x));
	    }
	  //tableSizeFor(initialCapacity)���ش���initialCapacity����С�Ķ�������ֵ��
	  static final int tableSizeFor(int cap) {
	        int n = cap - 1;
	        n |= n >>> 1;
	        n |= n >>> 2;
	        n |= n >>> 4;
	        n |= n >>> 8;
	        n |= n >>> 16;
	        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
	    }

	  public HashMap(int initialCapacity, float loadFactor) {
		  // ��ʼ��������С��0�����򱨴�
	        if (initialCapacity < 0)
	            throw new IllegalArgumentException("Illegal initial capacity: " +
	                                               initialCapacity);
	     // ��ʼ�������ܴ������ֵ������Ϊ���ֵ
	        if (initialCapacity > MAXIMUM_CAPACITY)
	            initialCapacity = MAXIMUM_CAPACITY;
	        // ������Ӳ���С�ڻ����0������Ϊ������
	        if (loadFactor <= 0 || Float.isNaN(loadFactor))
	            throw new IllegalArgumentException("Illegal load factor: " +
	                                               loadFactor);
	     // ��ʼ���������  
	        this.loadFactor = loadFactor;
	     // ��ʼ��threshold��С
	        this.threshold = tableSizeFor(initialCapacity);
	    }
	  public HashMap(int initialCapacity) {
		// ����HashMap(int, float)�͹��캯��
	        this(initialCapacity, DEFAULT_LOAD_FACTOR);
	    }
	   public HashMap() {
		   // ��ʼ���������     
	        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
	    }
	   public HashMap(Map<? extends K, ? extends V> m) {
		// ��ʼ��������� 
	        this.loadFactor = DEFAULT_LOAD_FACTOR;
	     // ��m�е�����Ԫ�������HashMap��
	        putMapEntries(m, false);
	    }
	   //���������Map��ӵ���Map��
	   final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
	        int s = m.size();
	        if (s > 0) {
	        	// �ж�table�Ƿ��Ѿ���ʼ��
	            if (table == null) { // pre-size
	            	// δ��ʼ����sΪm��ʵ��Ԫ�ظ���
	                float ft = ((float)s / loadFactor) + 1.0F;
	                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
	                         (int)ft : MAXIMUM_CAPACITY);
	             // ����õ���t������ֵ�����ʼ����ֵ
	                if (t > threshold)
	                    threshold = tableSizeFor(t);
	            }
	         // �ѳ�ʼ��������mԪ�ظ���������ֵ���������ݴ���
	            else if (s > threshold)
	                resize();
	         // ��m�е�����Ԫ�������HashMap��
	            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
	                K key = e.getKey();
	                V value = e.getValue();
	                putVal(hash(key), key, value, false, evict);
	            }
	        }
	    }
	   //����Map�ĳ���
	   public int size() {
	        return size;
	    }
	   //�ж�Map�Ƿ�Ϊ��
	   public boolean isEmpty() {
	        return size == 0;
	    }
	   /*ͨ��key��ȡvalue��ֵ
	    * ʵ�����ж�key��key��hashֵ�����ж�
	    */
	   public V get(Object key) {
	        Node<K,V> e;
	        return (e = getNode(hash(key), key)) == null ? null : e.value;
	    }
	   //ͨ��hashֵ�Լ�key��ȡNode(Entry)
	   final Node<K,V> getNode(int hash, Object key) {
	        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
	        // table�Ѿ���ʼ�������ȴ���0������hashѰ��table�е���Ҳ��Ϊ��
	        if ((tab = table) != null && (n = tab.length) > 0 &&
	            (first = tab[(n - 1) & hash]) != null) 
	        {
	        	 // Ͱ�е�һ��(����Ԫ��)���
	            if (first.hash == hash && // always check first node
	                ((k = first.key) == key || (key != null && key.equals(k))))//�ж�Map�Ƿ�
	                return first;
	         // Ͱ�в�ֹһ�����
	            if ((e = first.next) != null) {
	                if (first instanceof TreeeNode)
	                	// �ں�����в���
	                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
	             // �����������в���
	                do {
	                    if (e.hash == hash &&
	                        ((k = e.key) == key || (key != null && key.equals(k))))
	                        return e;
	                } while ((e = e.next) != null);
	            }
	        }
	        return null;
	    }
	   //�ж�map���Ƿ���ڴ�key
	    public boolean containsKey(Object key) {
	        return getNode(hash(key), key) != null;
	    }
	    //��key-value����map��
	    public V put(K key, V value) {
	        return putVal(hash(key), key, value, false, true);
	    }
	    //����µ�key-valueʵ�ʺ���
	 final V putVal(int hash, K key, V value, boolean onlyIfAbsent,  
                boolean evict) {  
     Node<K,V>[] tab; Node<K,V> p; int n, i;  
//�Ƚ�table����tab���ж�table�Ƿ�Ϊnull���СΪ0����Ϊ�棬�͵���resize������ʼ��  
     if ((tab = table) == null || (n = tab.length) == 0)  
         n = (tab = resize()).length;  
//ͨ��i = (n - 1) & hash�õ�table�е�indexֵ����Ϊnull����ֱ�����һ��newNode  
     if ((p = tab[i = (n - 1) & hash]) == null)  
         tab[i] = newNode(hash, key, value, null);  
     else {  
     //ִ�е����˵��������ײ����tab[i]��Ϊ�գ���Ҫ��ɵ����������  
         Node<K,V> e; K k;  
         if (p.hash == hash &&  
             ((k = p.key) == key || (key != null && key.equals(k))))  
//��ʱpָ����table[i]�д洢���Ǹ�Node�����������Ľڵ���hashֵ��keyֵ��p���Ѿ����ڣ���p����e  
             e = p;  
//���table������node���hash��key��ֵ�뽫Ҫ�����Node��hash��key���Ǻϣ�����Ҫ�����node�ڵ�����������ڵ��в��ҡ�  
         else if (p instanceof TreeNode)  
         //��p���ں�����ṹʱ�����պ������ʽ����  
             e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);  
         else {  
 //������˵����ײ�Ľڵ��Ե�������ʽ�洢��forѭ������ʹ����������������  
             for (int binCount = 0; ; ++binCount) {  
     //��p����һ���ڵ㸳��e�����Ϊnull������һ���½ڵ㸳��p����һ���ڵ�  
                 if ((e = p.next) == null) {  
                     p.next = newNode(hash, key, value, null);  
     //�����ͻ�ڵ�ﵽ8��������treeifyBin(tab, hash)�����treeifyBin���Ȼ�ȥ�жϵ�ǰhash��ĳ��ȣ��������64�Ļ���ʵ���Ͼ�ֻ����resize������table������Ѿ��ﵽ64����ô�ŻὫ��ͻ��洢�ṹ��Ϊ�������  

                     if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st  
                         treeifyBin(tab, hash);  
                     break;  
                 }  
//�������ͬ��hash��key�����˳�ѭ��  
                 if (e.hash == hash &&  
                     ((k = e.key) == key || (key != null && key.equals(k))))  
                     break;  
                 p = e;//��p����Ϊ��һ���ڵ�  
             }  
         }  
//��e��Ϊnull����ʾ�Ѿ������������ڵ�hash��key��ͬ�Ľڵ㣬hashmap������keyֵ��Ӧ��value�Ḳ����ǰ��ͬkeyֵ��Ӧ��valueֵ����������������ʵ�ֵ�  
         if (e != null) { // existing mapping for key  
             V oldValue = e.value;  
     //�ж��Ƿ��޸��Ѳ���ڵ��value  
             if (!onlyIfAbsent || oldValue == null)  
                 e.value = value;  
             afterNodeAccess(e);  
             return oldValue;  
         }  
     }  
     ++modCount;//�����½ڵ��hashmap�Ľṹ��������+1  
     if (++size > threshold)  
         resize();//HashMap�нڵ���+1���������threshold����ôҪ����һ������  
     afterNodeInsertion(evict);  
     return null;  
 }  
	//�������ݣ��������һ������hash���䣬���һ����hash�������е�Ԫ�أ��Ƿǳ���ʱ�ġ��ڱ�д�����У�Ҫ��������resize��
	final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;//������ʱNode�����ͱ�������Ϊhash table  
        int oldCap = (oldTab == null) ? 0 : oldTab.length;//��ȡhash table�ĳ���  
        int oldThr = threshold;//��ȡ��������
        int newCap, newThr = 0;//��ʼ���µ�table���Ⱥ�����ֵ 
        if (oldCap > 0) {
        	//ִ�е����˵��table�Ѿ���ʼ��  
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
          //�������ݣ�����������ֵ���ӱ�  
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) 
            newCap = oldThr; //�ù�������ʼ��������ֵ��������ֱֵ�Ӹ�����table����
        else {               
        	//�ϵ�table����������ֵ��Ϊ0����ʼ����������������ֵ���ڵ���hashmap������ʽ��������ʱ���Ͳ������ַ�ʽ��ʼ�� 
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        if (newThr == 0) {
        	 //�������ֵΪ0��������������  ��Ϊ�µ�������С*��������
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;//����������ֵΪthreshold  
        @SuppressWarnings({"rawtypes","unchecked"})
                    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap]; //��ʼ���µ�table���� 
        table = newTab;
        //��ԭ����table��Ϊnullʱ����Ҫ��table[i]�еĽڵ�Ǩ��  
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
              //ȡ�������е�һ���ڵ㱣�棬����Ϊnull�������������
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;//�����ͷ�  
                    if (e.next == null)
                    	//������ֻ��һ���ڵ㣬û�к����ڵ㣬��ֱ�����¼�������table�е�index�������˽ڵ�洢����table��Ӧ��indexλ�ô�  
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                    	 //��e�Ǻ�����ڵ㣬�򰴺�����ƶ� 
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                    	//Ǩ�Ƶ������е�ÿ���ڵ�  
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                        	/** 
                        	* �������ϵļ�ֵ�԰�hashֵ�ֳ�lo��hi������lo����������λ����ԭ����ͬ[ԭ��λ 
                        	* j]��hi����������λ��Ϊ[ԭ��λ��j+oldCap]�� 
                        	* ����ļ�ֵ�Լ���lo����hi��ȡ���� �ж�����if ((e.hash & oldCap) == 0)����Ϊ* capacity��2���ݣ�����oldCapΪ10...0�Ķ�������ʽ�����ж�����Ϊ�棬��ζ�� 
                        	* oldCapΪ1����λ��Ӧ��hashλΪ0�����������ļ���û��Ӱ�죨������ 
                        	* =hash&(newCap-*1)��newCap=oldCap<<2�������ж�����Ϊ�٣��� oldCapΪ1����λ* ��Ӧ��hashλΪ1�� 
                        	* ��������=hash&( newCap-1 )= hash&( (oldCap<<2) - 1)���൱�ڶ���10...0�� 
                        	* �� oldCap 
                        	 
                        	* ���ӣ� 
                        	* ������=16��������10000��������=32��������100000 
                        	* �������ļ��㣺 
                        	* hash = xxxx xxxx xxxy xxxx 
                        	* ������-1 1111 
                        	* &���� xxxx 
                        	* �������ļ��㣺 
                        	* hash = xxxx xxxx xxxy xxxx 
                        	* ������-1 1 1111 
                        	* &���� y xxxx 
                        	* ������ = ������ + y0000�����ж�����Ϊ�棬��y=0(lo����������)������y=1(hi�� 
                        	* ����=������+������10000) 
                        	   */  
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
	  final void treeifyBin(Node<K,V>[] tab, int hash) {
	        int n, index; Node<K,V> e;
	        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
	            resize();
	        else if ((e = tab[index = (n - 1) & hash]) != null) {
	            TreeNode<K,V> hd = null, tl = null;
	            do {
	                TreeNode<K,V> p = replacementTreeNode(e, null);
	                if (tl == null)
	                    hd = p;
	                else {
	                    p.prev = tl;
	                    tl.next = p;
	                }
	                tl = p;
	            } while ((e = e.next) != null);
	            if ((tab[index] = hd) != null)
	                hd.treeify(tab);
	        }
	    }
	  //��Map m�еķ����HashMap��
	  public void putAll(Map<? extends K, ? extends V> m) {
	        putMapEntries(m, true);
	    }
	  //�Ƴ�keyΪָ����Entry(Node),����Value
	  public V remove(Object key) {
	        Node<K,V> e;
	        return (e = removeNode(hash(key), key, null, false, true)) == null ?
	            null : e.value;
	    }
	  //������ʵ�Ƴ�Entry�ĺ���
	    final Node<K,V> removeNode(int hash, Object key, Object value,
                boolean matchValue, boolean movable)
	    {
	        Node<K,V>[] tab; Node<K,V> p; int n, index;
	        //��table���鸳tab���飬�ж������Ƿ��г����ҵ�hash��Ӧ������λ��
	        if ((tab = table) != null && (n = tab.length) > 0 &&
	            (p = tab[index = (n - 1) & hash]) != null) {
	            Node<K,V> node = null, e; K k; V v;
	            if (p.hash == hash &&
	                ((k = p.key) == key || (key != null && key.equals(k))))//�����Ͱ��һ���ڵ��������key���
	                node = p;//��ֵ��node�ڵ�
	            else if ((e = p.next) != null) {//�����������Ѱ��
	                if (p instanceof TreeNode)//����Ǻ�����ʵ��
	                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);//������������Ѱ��
	                else {
	                    do {//���б���Ͱ�ڵ�
	                        if (e.hash == hash &&
	                            ((k = e.key) == key ||
	                             (key != null && key.equals(k)))) {
	                            node = e;//�ҵ���ֵ��node�ڵ�
	                            break;
	                        }
	                        p = e;//p��¼��ǰλ��
	                    } while ((e = e.next) != null);
	                }
	            }
	            if (node != null && (!matchValue || (v = node.value) == value ||
	                                 (value != null && value.equals(v)))) {//�ҵ�����Ľڵ�
	                if (node instanceof TreeNode)//����ǽڵ��ں������
	                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);//���ú����ɾ����������ɾ��
	                else if (node == p)//����ǵ�һ�ڵ�
	                    tab[index] = node.next;//����һ����㸳ֵ��һλ
	                else
	                    p.next = node.next;//ɾ��ָ���ڵ�(�ҵ��Ľ���ǰһ���ڵ�ָ���ҵ�������һ���ڵ㣩
	                ++modCount;
	                --size;
	                afterNodeRemoval(node);
	                return node;
	            }
	        }
	        return null;
	    }
	    //���Map��Ԫ��
	    public void clear() {
	        Node<K,V>[] tab;
	        modCount++;
	        if ((tab = table) != null && size > 0) {
	            size = 0;
	            for (int i = 0; i < tab.length; ++i)
	                tab[i] = null;
	        }
	    }
	    //�жϴ����value��Map���Ƿ����
	    public boolean containsValue(Object value) {
	        Node<K,V>[] tab; V v;
	        if ((tab = table) != null && size > 0) {
	            for (int i = 0; i < tab.length; ++i) {
	                for (Node<K,V> e = tab[i]; e != null; e = e.next) {//��������Entry
	                    if ((v = e.value) == value ||
	                        (value != null && value.equals(v)))//�ҵ�����true
	                        return true;
	                }
	            }
	        }
	        return false;
	    }
	    public Set<K> keySet() {//�����е�key��ŵ�Set�з���
	        Set<K> ks = keySet;
	        if (ks == null) {
	            ks = new KeySet();
	            keySet = ks;
	        }
	        return ks;
	    }
	    //keysetʵ��
	    final class KeySet extends AbstractSet<K> {
	        public final int size()                 { return size; }//����Map����
	        public final void clear()               { HashMap.this.clear(); }//���Map
	        public final Iterator<K> iterator()     { return new KeyIterator(); }//����key�ĵ�����
	        public final boolean contains(Object o) { return containsKey(o); }//�ж�ָ����key�Ƿ����Map��
	        public final boolean remove(Object key) {//�Ƴ�ָ��key�Ľڵ�                                                   
	            return removeNode(hash(key), key, null, false, true) != null;
	        }
	        public final Spliterator<K> spliterator() {
	            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
	        }
	        public final void forEach(Consumer<? super K> action) {
	            Node<K,V>[] tab;
	            if (action == null)
	                throw new NullPointerException();
	            if (size > 0 && (tab = table) != null) {
	                int mc = modCount;
	                for (int i = 0; i < tab.length; ++i) {
	                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
	                        action.accept(e.key);
	                }
	                if (modCount != mc)
	                    throw new ConcurrentModificationException();
	            }
	        }
	    }
	    //���ڽ�������е�ֵ��Collection������з���
	    public Collection<V> values() {
	        Collection<V> vs = values;
	        if (vs == null) {
	            vs = new Values();
	            values = vs;
	        }
	        return vs;
	    }
	    //ʵ�ʽ��л�ȡֵ�ú���
	    final class Values extends AbstractCollection<V> {
	        public final int size()                 { return size; }//����Map����
	        public final void clear()               { HashMap.this.clear(); }//���Map
	        public final Iterator<V> iterator()     { return new ValueIterator(); }//����һ���µ���ֵ������
	        public final boolean contains(Object o) { return containsValue(o); }//�ж�ָ������ֵ�Ƿ����Map��
	        public final Spliterator<V> spliterator() {
	            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
	        }
	        public final void forEach(Consumer<? super V> action) {
	            Node<K,V>[] tab;
	            if (action == null)
	                throw new NullPointerException();
	            if (size > 0 && (tab = table) != null) {
	                int mc = modCount;
	                for (int i = 0; i < tab.length; ++i) {
	                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
	                        action.accept(e.value);
	                }
	                if (modCount != mc)
	                    throw new ConcurrentModificationException();
	            }
	        }
	    }
	  	//�������е�Entryʵ��		
	    public Set<Map.Entry<K,V>> entrySet() {
	        Set<Map.Entry<K,V>> es;
	        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
	    }
	    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
	        public final int size()                 { return size; }
	        public final void clear()               { HashMap.this.clear(); }
	        public final Iterator<Map.Entry<K,V>> iterator() {//����һ��Map.Entry<K,V>�ĵ�����
	            return new EntryIterator();
	        }
	        public final boolean contains(Object o) {//�ж�ָ���Ķ����Ƿ����Map��
	            if (!(o instanceof Map.Entry))//�ж�o�ǲ���Map.Entry��ʵ��
	                return false;
	            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
	            Object key = e.getKey();//��ָ��key�ĸ��Ƶ��µ�key��
	            Node<K,V> candidate = getNode(hash(key), key);//��ȡkey�Ľڵ�
	            return candidate != null && candidate.equals(e);//�ж�ָ���ڵ���ͨ��key�ҵĽ���Ƿ���ͬ
	        }
	        //�Ƴ�ָ����Entry
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
	            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
	        }
	        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
	            Node<K,V>[] tab;
	            if (action == null)
	                throw new NullPointerException();
	            if (size > 0 && (tab = table) != null) {
	                int mc = modCount;
	                for (int i = 0; i < tab.length; ++i) {
	                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
	                        action.accept(e);
	                }
	                if (modCount != mc)
	                    throw new ConcurrentModificationException();
	            }
	        }
	    }
	    //�ж�ָ����key�Ƿ��Ž�㣬���ڷ���ֵ�������ڷ���defaultValue
	    @Override
	    public V getOrDefault(Object key, V defaultValue) {
	        Node<K,V> e;
	        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
	    }
	    //���һ��ָ��key-value��Entry
	    @Override
	    public V putIfAbsent(K key, V value) {
	        return putVal(hash(key), key, value, true, true);
	    }
	    //�Ƴ�ָ����key-value��Node���ɹ�����true���ɹ�����false
	    @Override
	    public boolean remove(Object key, Object value) {
	        return removeNode(hash(key), key, value, true, true) != null;
	    }

	    //��ָ���Ľ���ֵ�����滻
	    @Override
	    public boolean replace(K key, V oldValue, V newValue) {
	        Node<K,V> e; V v;
	        if ((e = getNode(hash(key), key)) != null &&
	            ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
	            e.value = newValue;
	            afterNodeAccess(e);
	            return true;
	        }
	        return false;
	    }
	    @Override
	    public V computeIfAbsent(K key,
	                             Function<? super K, ? extends V> mappingFunction) {
	        if (mappingFunction == null)
	            throw new NullPointerException();
	        int hash = hash(key);
	        Node<K,V>[] tab; Node<K,V> first; int n, i;
	        int binCount = 0;
	        TreeNode<K,V> t = null;
	        Node<K,V> old = null;
	        if (size > threshold || (tab = table) == null ||
	            (n = tab.length) == 0)
	            n = (tab = resize()).length;
	        if ((first = tab[i = (n - 1) & hash]) != null) {
	            if (first instanceof TreeNode)
	                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
	            else {
	                Node<K,V> e = first; K k;
	                do {
	                    if (e.hash == hash &&
	                        ((k = e.key) == key || (key != null && key.equals(k)))) {
	                        old = e;
	                        break;
	                    }
	                    ++binCount;
	                } while ((e = e.next) != null);
	            }
	            V oldValue;
	            if (old != null && (oldValue = old.value) != null) {
	                afterNodeAccess(old);
	                return oldValue;
	            }
	        }
	        V v = mappingFunction.apply(key);
	        if (v == null) {
	            return null;
	        } else if (old != null) {
	            old.value = v;
	            afterNodeAccess(old);
	            return v;
	        }
	        else if (t != null)
	            t.putTreeVal(this, tab, hash, key, v);
	        else {
	            tab[i] = newNode(hash, key, v, first);
	            if (binCount >= TREEIFY_THRESHOLD - 1)
	                treeifyBin(tab, hash);
	        }
	        ++modCount;
	        ++size;
	        afterNodeInsertion(true);
	        return v;
	    }

	    public V computeIfPresent(K key,
	                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
	        if (remappingFunction == null)
	            throw new NullPointerException();
	        Node<K,V> e; V oldValue;
	        int hash = hash(key);
	        if ((e = getNode(hash, key)) != null &&
	            (oldValue = e.value) != null) {
	            V v = remappingFunction.apply(key, oldValue);
	            if (v != null) {
	                e.value = v;
	                afterNodeAccess(e);
	                return v;
	            }
	            else
	                removeNode(hash, key, null, false, true);
	        }
	        return null;
	    }

	    @Override
	    public V compute(K key,
	                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
	        if (remappingFunction == null)
	            throw new NullPointerException();
	        int hash = hash(key);
	        Node<K,V>[] tab; Node<K,V> first; int n, i;
	        int binCount = 0;
	        TreeNode<K,V> t = null;
	        Node<K,V> old = null;
	        if (size > threshold || (tab = table) == null ||
	            (n = tab.length) == 0)
	            n = (tab = resize()).length;
	        if ((first = tab[i = (n - 1) & hash]) != null) {
	            if (first instanceof TreeNode)
	                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
	            else {
	                Node<K,V> e = first; K k;
	                do {
	                    if (e.hash == hash &&
	                        ((k = e.key) == key || (key != null && key.equals(k)))) {
	                        old = e;
	                        break;
	                    }
	                    ++binCount;
	                } while ((e = e.next) != null);
	            }
	        }
	        V oldValue = (old == null) ? null : old.value;
	        V v = remappingFunction.apply(key, oldValue);
	        if (old != null) {
	            if (v != null) {
	                old.value = v;
	                afterNodeAccess(old);
	            }
	            else
	                removeNode(hash, key, null, false, true);
	        }
	        else if (v != null) {
	            if (t != null)
	                t.putTreeVal(this, tab, hash, key, v);
	            else {
	                tab[i] = newNode(hash, key, v, first);
	                if (binCount >= TREEIFY_THRESHOLD - 1)
	                    treeifyBin(tab, hash);
	            }
	            ++modCount;
	            ++size;
	            afterNodeInsertion(true);
	        }
	        return v;
	    }
	    @Override
	    public V merge(K key, V value,
	                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
	        if (value == null)
	            throw new NullPointerException();
	        if (remappingFunction == null)
	            throw new NullPointerException();
	        int hash = hash(key);
	        Node<K,V>[] tab; Node<K,V> first; int n, i;
	        int binCount = 0;
	        TreeNode<K,V> t = null;
	        Node<K,V> old = null;
	        if (size > threshold || (tab = table) == null ||
	            (n = tab.length) == 0)
	            n = (tab = resize()).length;
	        if ((first = tab[i = (n - 1) & hash]) != null) {
	            if (first instanceof TreeNode)
	                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
	            else {
	                Node<K,V> e = first; K k;
	                do {
	                    if (e.hash == hash &&
	                        ((k = e.key) == key || (key != null && key.equals(k)))) {
	                        old = e;
	                        break;
	                    }
	                    ++binCount;
	                } while ((e = e.next) != null);
	            }
	        }
	        if (old != null) {
	            V v;
	            if (old.value != null)
	                v = remappingFunction.apply(old.value, value);
	            else
	                v = value;
	            if (v != null) {
	                old.value = v;
	                afterNodeAccess(old);
	            }
	            else
	                removeNode(hash, key, null, false, true);
	            return v;
	        }
	        if (value != null) {
	            if (t != null)
	                t.putTreeVal(this, tab, hash, key, value);
	            else {
	                tab[i] = newNode(hash, key, value, first);
	                if (binCount >= TREEIFY_THRESHOLD - 1)
	                    treeifyBin(tab, hash);
	            }
	            ++modCount;
	            ++size;
	            afterNodeInsertion(true);
	        }
	        return value;
	    }

	    @Override
	    public void forEach(BiConsumer<? super K, ? super V> action) {
	        Node<K,V>[] tab;
	        if (action == null)
	            throw new NullPointerException();
	        if (size > 0 && (tab = table) != null) {
	            int mc = modCount;
	            for (int i = 0; i < tab.length; ++i) {
	                for (Node<K,V> e = tab[i]; e != null; e = e.next)
	                    action.accept(e.key, e.value);
	            }
	            if (modCount != mc)
	                throw new ConcurrentModificationException();
	        }
	    }
	    //�滻����function�е�Entry
	    @Override
	    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
	        Node<K,V>[] tab;
	        if (function == null)
	            throw new NullPointerException();
	        if (size > 0 && (tab = table) != null) {
	            int mc = modCount;
	            for (int i = 0; i < tab.length; ++i) {
	                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
	                    e.value = function.apply(e.key, e.value);
	                }
	            }
	            if (modCount != mc)
	                throw new ConcurrentModificationException();
	        }
	    }
	    //��¡
	    @SuppressWarnings("unchecked")
	    @Override
	    public Object clone() {
	        HashMap<K,V> result;
	        try {
	            result = (HashMap<K,V>)super.clone();
	        } catch (CloneNotSupportedException e) {
	            // this shouldn't happen, since we are Cloneable
	            throw new InternalError(e);
	        }
	        result.reinitialize();
	        result.putMapEntries(this, false);
	        return result;
	    }
	    final float loadFactor() { return loadFactor; }//���ؼ�������
	    final int capacity() {//��������
	        return (table != null) ? table.length :
	            (threshold > 0) ? threshold :
	            DEFAULT_INITIAL_CAPACITY;
	    }
	    private void writeObject(java.io.ObjectOutputStream s)
	            throws IOException {
	            int buckets = capacity();
	            // Write out the threshold, loadfactor, and any hidden stuff
	            s.defaultWriteObject();
	            s.writeInt(buckets);
	            s.writeInt(size);
	            internalWriteEntries(s);
	        }

	        /**
	         * Reconstitute the {@code HashMap} instance from a stream (i.e.,
	         * deserialize it).
	         */
	        private void readObject(java.io.ObjectInputStream s)
	            throws IOException, ClassNotFoundException {
	            // Read in the threshold (ignored), loadfactor, and any hidden stuff
	            s.defaultReadObject();
	            reinitialize();
	            if (loadFactor <= 0 || Float.isNaN(loadFactor))
	                throw new InvalidObjectException("Illegal load factor: " +
	                                                 loadFactor);
	            s.readInt();                // Read and ignore number of buckets
	            int mappings = s.readInt(); // Read number of mappings (size)
	            if (mappings < 0)
	                throw new InvalidObjectException("Illegal mappings count: " +
	                                                 mappings);
	            else if (mappings > 0) { // (if zero, use defaults)
	                // Size the table using given load factor only if within
	                // range of 0.25...4.0
	                float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
	                float fc = (float)mappings / lf + 1.0f;
	                int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
	                           DEFAULT_INITIAL_CAPACITY :
	                           (fc >= MAXIMUM_CAPACITY) ?
	                           MAXIMUM_CAPACITY :
	                           tableSizeFor((int)fc));
	                float ft = (float)cap * lf;
	                threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
	                             (int)ft : Integer.MAX_VALUE);
	                @SuppressWarnings({"rawtypes","unchecked"})
	                    Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
	                table = tab;

	                // Read the keys and values, and put the mappings in the HashMap
	                for (int i = 0; i < mappings; i++) {
	                    @SuppressWarnings("unchecked")
	                        K key = (K) s.readObject();
	                    @SuppressWarnings("unchecked")
	                        V value = (V) s.readObject();
	                    putVal(hash(key), key, value, false, false);
	                }
	            }
	        }
	        abstract class HashIterator {
	            Node<K,V> next;        // ��һ���ڵ�
	            Node<K,V> current;     // ��ǰ���
	            int expectedModCount;  // for fast-fail
	            int index;             // ����

	            HashIterator() {
	                expectedModCount = modCount;
	                Node<K,V>[] t = table;//��table��ֵ��t
	                current = next = null;
	                index = 0;
	                if (t != null && size > 0) { // advance to first entry
	                    do {} while (index < t.length && (next = t[index++]) == null);//��ȡ��һ��Entryλ��
	                }
	            }

	            public final boolean hasNext() {//�ж��Ƿ�����һ����ʵָnext��ǰ�ģ�ӦΪִ��nextNode����ǰ�����Զ���������ǰ�ƶ���
	                return next != null;
	            }

	            final Node<K,V> nextNode() {//��һ���ڵ�
	                Node<K,V>[] t;
	                Node<K,V> e = next;
	                if (modCount != expectedModCount)
	                    throw new ConcurrentModificationException();
	                if (e == null)
	                    throw new NoSuchElementException();
	                if ((next = (current = e).next) == null && (t = table) != null) {
	                    do {} while (index < t.length && (next = t[index++]) == null);//����һ��tableλ�ø�ֵ����һ���ڵ㣬��������ָ����һ��λ��
	                }
	                return e;
	            }

	            public final void remove() {//�Ƴ���ǰλ�õ�Entry
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
	        //����key�ĵ�����
	        final class KeyIterator extends HashIterator
	        implements Iterator<K> {
	        public final K next() { return nextNode().key; }
	    }
	      //����value�ĵ�����
	    final class ValueIterator extends HashIterator
	        implements Iterator<V> {
	        public final V next() { return nextNode().value; }
	    }
	    //����Map.Entry�ĵ�����
	    final class EntryIterator extends HashIterator
	        implements Iterator<Map.Entry<K,V>> {
	        public final Map.Entry<K,V> next() { return nextNode(); }
	    }
	    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {//����һ���µ�Node�ڵ�
	        return new Node<>(hash, key, value, next);
	    }
	    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {//����һ��ָ�����ݲ���ָ��ָ������һ���ڵ���½ڵ�
	        return new Node<>(p.hash, p.key, p.value, next);
	    }
	    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {//����һ���´����ĺ�����ڵ�
	        return new TreeNode<>(hash, key, value, next);
	    }
	    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {//���ݴ�����Ľڵ㣬����һ�����µĺ�����ڵ�
	        return new TreeNode<>(p.hash, p.key, p.value, next);
	    }
	    void reinitialize() {//��ʼ��Map
	        table = null;
	        entrySet = null;
	        keySet = null;
	        values = null;
	        modCount = 0;
	        threshold = 0;
	        size = 0;
	    }
	    void afterNodeAccess(Node<K,V> p) { }
	    void afterNodeInsertion(boolean evict) { }
	    void afterNodeRemoval(Node<K,V> p) { }
	    //�������
	    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
	        TreeNode<K,V> parent;  // ���׽��
	        TreeNode<K,V> left;//���ӽڵ�
	        TreeNode<K,V> right;//���ӽڵ�
	        TreeNode<K,V> prev;  //ǰ�����
	        boolean red;
	        TreeNode(int hash, K key, V val, Node<K,V> next) {
	            super(hash, key, val, next);
	        }
	        final TreeNode<K,V> root() {//���ظ����
	            for (TreeNode<K,V> r = this, p;;) {
	                if ((p = r.parent) == null)
	                    return r;
	                r = p;
	            }
	        }
	        /*�������δ�о�*/////
	        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
	            int n;
	            if (root != null && tab != null && (n = tab.length) > 0) {
	                int index = (n - 1) & root.hash;
	                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
	                if (root != first) {
	                    Node<K,V> rn;
	                    tab[index] = root;
	                    TreeNode<K,V> rp = root.prev;
	                    if ((rn = root.next) != null)
	                        ((TreeNode<K,V>)rn).prev = rp;
	                    if (rp != null)
	                        rp.next = rn;
	                    if (first != null)
	                        first.prev = root;
	                    root.next = first;
	                    root.prev = null;
	                }
	                assert checkInvariants(root);
	            }
	        }
	        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
	            TreeNode<K,V> p = this;
	            do {
	                int ph, dir; K pk;
	                TreeNode<K,V> pl = p.left, pr = p.right, q;
	                if ((ph = p.hash) > h)
	                    p = pl;
	                else if (ph < h)
	                    p = pr;
	                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
	                    return p;
	                else if (pl == null)
	                    p = pr;
	                else if (pr == null)
	                    p = pl;
	                else if ((kc != null ||
	                          (kc = comparableClassFor(k)) != null) &&
	                         (dir = compareComparables(kc, k, pk)) != 0)
	                    p = (dir < 0) ? pl : pr;
	                else if ((q = pr.find(h, k, kc)) != null)
	                    return q;
	                else
	                    p = pl;
	            } while (p != null);
	            return null;
	        }

	        /**
	         * Calls find for root node.
	         */
	        final TreeNode<K,V> getTreeNode(int h, Object k) {
	            return ((parent != null) ? root() : this).find(h, k, null);
	        }

	        /**
	         * Tie-breaking utility for ordering insertions when equal
	         * hashCodes and non-comparable. We don't require a total
	         * order, just a consistent insertion rule to maintain
	         * equivalence across rebalancings. Tie-breaking further than
	         * necessary simplifies testing a bit.
	         */
	        static int tieBreakOrder(Object a, Object b) {
	            int d;
	            if (a == null || b == null ||
	                (d = a.getClass().getName().
	                 compareTo(b.getClass().getName())) == 0)
	                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
	                     -1 : 1);
	            return d;
	        }

	        /**
	         * Forms tree of the nodes linked from this node.
	         * @return root of tree
	         */
	        final void treeify(Node<K,V>[] tab) {
	            TreeNode<K,V> root = null;
	            for (TreeNode<K,V> x = this, next; x != null; x = next) {
	                next = (TreeNode<K,V>)x.next;
	                x.left = x.right = null;
	                if (root == null) {
	                    x.parent = null;
	                    x.red = false;
	                    root = x;
	                }
	                else {
	                    K k = x.key;
	                    int h = x.hash;
	                    Class<?> kc = null;
	                    for (TreeNode<K,V> p = root;;) {
	                        int dir, ph;
	                        K pk = p.key;
	                        if ((ph = p.hash) > h)
	                            dir = -1;
	                        else if (ph < h)
	                            dir = 1;
	                        else if ((kc == null &&
	                                  (kc = comparableClassFor(k)) == null) ||
	                                 (dir = compareComparables(kc, k, pk)) == 0)
	                            dir = tieBreakOrder(k, pk);

	                        TreeNode<K,V> xp = p;
	                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
	                            x.parent = xp;
	                            if (dir <= 0)
	                                xp.left = x;
	                            else
	                                xp.right = x;
	                            root = balanceInsertion(root, x);
	                            break;
	                        }
	                    }
	                }
	            }
	            moveRootToFront(tab, root);
	        }

	        /**
	         * Returns a list of non-TreeNodes replacing those linked from
	         * this node.
	         */
	        final Node<K,V> untreeify(HashMap<K,V> map) {
	            Node<K,V> hd = null, tl = null;
	            for (Node<K,V> q = this; q != null; q = q.next) {
	                Node<K,V> p = map.replacementNode(q, null);
	                if (tl == null)
	                    hd = p;
	                else
	                    tl.next = p;
	                tl = p;
	            }
	            return hd;
	        }

	        /**
	         * Tree version of putVal.
	         */
	        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
	                                       int h, K k, V v) {
	            Class<?> kc = null;
	            boolean searched = false;
	            TreeNode<K,V> root = (parent != null) ? root() : this;
	            for (TreeNode<K,V> p = root;;) {
	                int dir, ph; K pk;
	                if ((ph = p.hash) > h)
	                    dir = -1;
	                else if (ph < h)
	                    dir = 1;
	                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
	                    return p;
	                else if ((kc == null &&
	                          (kc = comparableClassFor(k)) == null) ||
	                         (dir = compareComparables(kc, k, pk)) == 0) {
	                    if (!searched) {
	                        TreeNode<K,V> q, ch;
	                        searched = true;
	                        if (((ch = p.left) != null &&
	                             (q = ch.find(h, k, kc)) != null) ||
	                            ((ch = p.right) != null &&
	                             (q = ch.find(h, k, kc)) != null))
	                            return q;
	                    }
	                    dir = tieBreakOrder(k, pk);
	                }

	                TreeNode<K,V> xp = p;
	                if ((p = (dir <= 0) ? p.left : p.right) == null) {
	                    Node<K,V> xpn = xp.next;
	                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
	                    if (dir <= 0)
	                        xp.left = x;
	                    else
	                        xp.right = x;
	                    xp.next = x;
	                    x.parent = x.prev = xp;
	                    if (xpn != null)
	                        ((TreeNode<K,V>)xpn).prev = x;
	                    moveRootToFront(tab, balanceInsertion(root, x));
	                    return null;
	                }
	            }
	        }

	        /**
	         * Removes the given node, that must be present before this call.
	         * This is messier than typical red-black deletion code because we
	         * cannot swap the contents of an interior node with a leaf
	         * successor that is pinned by "next" pointers that are accessible
	         * independently during traversal. So instead we swap the tree
	         * linkages. If the current tree appears to have too few nodes,
	         * the bin is converted back to a plain bin. (The test triggers
	         * somewhere between 2 and 6 nodes, depending on tree structure).
	         */
	        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
	                                  boolean movable) {
	            int n;
	            if (tab == null || (n = tab.length) == 0)
	                return;
	            int index = (n - 1) & hash;
	            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
	            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
	            if (pred == null)
	                tab[index] = first = succ;
	            else
	                pred.next = succ;
	            if (succ != null)
	                succ.prev = pred;
	            if (first == null)
	                return;
	            if (root.parent != null)
	                root = root.root();
	            if (root == null || root.right == null ||
	                (rl = root.left) == null || rl.left == null) {
	                tab[index] = first.untreeify(map);  // too small
	                return;
	            }
	            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
	            if (pl != null && pr != null) {
	                TreeNode<K,V> s = pr, sl;
	                while ((sl = s.left) != null) // find successor
	                    s = sl;
	                boolean c = s.red; s.red = p.red; p.red = c; // swap colors
	                TreeNode<K,V> sr = s.right;
	                TreeNode<K,V> pp = p.parent;
	                if (s == pr) { // p was s's direct parent
	                    p.parent = s;
	                    s.right = p;
	                }
	                else {
	                    TreeNode<K,V> sp = s.parent;
	                    if ((p.parent = sp) != null) {
	                        if (s == sp.left)
	                            sp.left = p;
	                        else
	                            sp.right = p;
	                    }
	                    if ((s.right = pr) != null)
	                        pr.parent = s;
	                }
	                p.left = null;
	                if ((p.right = sr) != null)
	                    sr.parent = p;
	                if ((s.left = pl) != null)
	                    pl.parent = s;
	                if ((s.parent = pp) == null)
	                    root = s;
	                else if (p == pp.left)
	                    pp.left = s;
	                else
	                    pp.right = s;
	                if (sr != null)
	                    replacement = sr;
	                else
	                    replacement = p;
	            }
	            else if (pl != null)
	                replacement = pl;
	            else if (pr != null)
	                replacement = pr;
	            else
	                replacement = p;
	            if (replacement != p) {
	                TreeNode<K,V> pp = replacement.parent = p.parent;
	                if (pp == null)
	                    root = replacement;
	                else if (p == pp.left)
	                    pp.left = replacement;
	                else
	                    pp.right = replacement;
	                p.left = p.right = p.parent = null;
	            }

	            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

	            if (replacement == p) {  // detach
	                TreeNode<K,V> pp = p.parent;
	                p.parent = null;
	                if (pp != null) {
	                    if (p == pp.left)
	                        pp.left = null;
	                    else if (p == pp.right)
	                        pp.right = null;
	                }
	            }
	            if (movable)
	                moveRootToFront(tab, r);
	        }

	        /**
	         * Splits nodes in a tree bin into lower and upper tree bins,
	         * or untreeifies if now too small. Called only from resize;
	         * see above discussion about split bits and indices.
	         *
	         * @param map the map
	         * @param tab the table for recording bin heads
	         * @param index the index of the table being split
	         * @param bit the bit of hash to split on
	         */
	        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
	            TreeNode<K,V> b = this;
	            // Relink into lo and hi lists, preserving order
	            TreeNode<K,V> loHead = null, loTail = null;
	            TreeNode<K,V> hiHead = null, hiTail = null;
	            int lc = 0, hc = 0;
	            for (TreeNode<K,V> e = b, next; e != null; e = next) {
	                next = (TreeNode<K,V>)e.next;
	                e.next = null;
	                if ((e.hash & bit) == 0) {
	                    if ((e.prev = loTail) == null)
	                        loHead = e;
	                    else
	                        loTail.next = e;
	                    loTail = e;
	                    ++lc;
	                }
	                else {
	                    if ((e.prev = hiTail) == null)
	                        hiHead = e;
	                    else
	                        hiTail.next = e;
	                    hiTail = e;
	                    ++hc;
	                }
	            }

	            if (loHead != null) {
	                if (lc <= UNTREEIFY_THRESHOLD)
	                    tab[index] = loHead.untreeify(map);
	                else {
	                    tab[index] = loHead;
	                    if (hiHead != null) // (else is already treeified)
	                        loHead.treeify(tab);
	                }
	            }
	            if (hiHead != null) {
	                if (hc <= UNTREEIFY_THRESHOLD)
	                    tab[index + bit] = hiHead.untreeify(map);
	                else {
	                    tab[index + bit] = hiHead;
	                    if (loHead != null)
	                        hiHead.treeify(tab);
	                }
	            }
	        }

	        /* ------------------------------------------------------------ */
	        // Red-black tree methods, all adapted from CLR

	        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
	                                              TreeNode<K,V> p) {
	            TreeNode<K,V> r, pp, rl;
	            if (p != null && (r = p.right) != null) {
	                if ((rl = p.right = r.left) != null)
	                    rl.parent = p;
	                if ((pp = r.parent = p.parent) == null)
	                    (root = r).red = false;
	                else if (pp.left == p)
	                    pp.left = r;
	                else
	                    pp.right = r;
	                r.left = p;
	                p.parent = r;
	            }
	            return root;
	        }

	        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
	                                               TreeNode<K,V> p) {
	            TreeNode<K,V> l, pp, lr;
	            if (p != null && (l = p.left) != null) {
	                if ((lr = p.left = l.right) != null)
	                    lr.parent = p;
	                if ((pp = l.parent = p.parent) == null)
	                    (root = l).red = false;
	                else if (pp.right == p)
	                    pp.right = l;
	                else
	                    pp.left = l;
	                l.right = p;
	                p.parent = l;
	            }
	            return root;
	        }

	        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
	                                                    TreeNode<K,V> x) {
	            x.red = true;
	            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
	                if ((xp = x.parent) == null) {
	                    x.red = false;
	                    return x;
	                }
	                else if (!xp.red || (xpp = xp.parent) == null)
	                    return root;
	                if (xp == (xppl = xpp.left)) {
	                    if ((xppr = xpp.right) != null && xppr.red) {
	                        xppr.red = false;
	                        xp.red = false;
	                        xpp.red = true;
	                        x = xpp;
	                    }
	                    else {
	                        if (x == xp.right) {
	                            root = rotateLeft(root, x = xp);
	                            xpp = (xp = x.parent) == null ? null : xp.parent;
	                        }
	                        if (xp != null) {
	                            xp.red = false;
	                            if (xpp != null) {
	                                xpp.red = true;
	                                root = rotateRight(root, xpp);
	                            }
	                        }
	                    }
	                }
	                else {
	                    if (xppl != null && xppl.red) {
	                        xppl.red = false;
	                        xp.red = false;
	                        xpp.red = true;
	                        x = xpp;
	                    }
	                    else {
	                        if (x == xp.left) {
	                            root = rotateRight(root, x = xp);
	                            xpp = (xp = x.parent) == null ? null : xp.parent;
	                        }
	                        if (xp != null) {
	                            xp.red = false;
	                            if (xpp != null) {
	                                xpp.red = true;
	                                root = rotateLeft(root, xpp);
	                            }
	                        }
	                    }
	                }
	            }
	        }

	        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
	                                                   TreeNode<K,V> x) {
	            for (TreeNode<K,V> xp, xpl, xpr;;)  {
	                if (x == null || x == root)
	                    return root;
	                else if ((xp = x.parent) == null) {
	                    x.red = false;
	                    return x;
	                }
	                else if (x.red) {
	                    x.red = false;
	                    return root;
	                }
	                else if ((xpl = xp.left) == x) {
	                    if ((xpr = xp.right) != null && xpr.red) {
	                        xpr.red = false;
	                        xp.red = true;
	                        root = rotateLeft(root, xp);
	                        xpr = (xp = x.parent) == null ? null : xp.right;
	                    }
	                    if (xpr == null)
	                        x = xp;
	                    else {
	                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
	                        if ((sr == null || !sr.red) &&
	                            (sl == null || !sl.red)) {
	                            xpr.red = true;
	                            x = xp;
	                        }
	                        else {
	                            if (sr == null || !sr.red) {
	                                if (sl != null)
	                                    sl.red = false;
	                                xpr.red = true;
	                                root = rotateRight(root, xpr);
	                                xpr = (xp = x.parent) == null ?
	                                    null : xp.right;
	                            }
	                            if (xpr != null) {
	                                xpr.red = (xp == null) ? false : xp.red;
	                                if ((sr = xpr.right) != null)
	                                    sr.red = false;
	                            }
	                            if (xp != null) {
	                                xp.red = false;
	                                root = rotateLeft(root, xp);
	                            }
	                            x = root;
	                        }
	                    }
	                }
	                else { // symmetric
	                    if (xpl != null && xpl.red) {
	                        xpl.red = false;
	                        xp.red = true;
	                        root = rotateRight(root, xp);
	                        xpl = (xp = x.parent) == null ? null : xp.left;
	                    }
	                    if (xpl == null)
	                        x = xp;
	                    else {
	                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
	                        if ((sl == null || !sl.red) &&
	                            (sr == null || !sr.red)) {
	                            xpl.red = true;
	                            x = xp;
	                        }
	                        else {
	                            if (sl == null || !sl.red) {
	                                if (sr != null)
	                                    sr.red = false;
	                                xpl.red = true;
	                                root = rotateLeft(root, xpl);
	                                xpl = (xp = x.parent) == null ?
	                                    null : xp.left;
	                            }
	                            if (xpl != null) {
	                                xpl.red = (xp == null) ? false : xp.red;
	                                if ((sl = xpl.left) != null)
	                                    sl.red = false;
	                            }
	                            if (xp != null) {
	                                xp.red = false;
	                                root = rotateRight(root, xp);
	                            }
	                            x = root;
	                        }
	                    }
	                }
	            }
	        }

	        /**
	         * Recursive invariant check
	         */
	        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
	            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
	                tb = t.prev, tn = (TreeNode<K,V>)t.next;
	            if (tb != null && tb.next != t)
	                return false;
	            if (tn != null && tn.prev != t)
	                return false;
	            if (tp != null && t != tp.left && t != tp.right)
	                return false;
	            if (tl != null && (tl.parent != t || tl.hash > t.hash))
	                return false;
	            if (tr != null && (tr.parent != t || tr.hash < t.hash))
	                return false;
	            if (t.red && tl != null && tl.red && tr != null && tr.red)
	                return false;
	            if (tl != null && !checkInvariants(tl))
	                return false;
	            if (tr != null && !checkInvariants(tr))
	                return false;
	            return true;
	        }
	    }

}
