package org.foree.bookreader.readpage;


import android.support.annotation.NonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author foree
 * @date 2018/7/20
 * @description 存放当前阅读界面的结构体，可以从任意一个节点往前后遍历
 * first，last分别指向头尾，head指向当前节点
 */
public class ChapterLinkedList<E> implements Iterable {
    private static final String TAG = ChapterLinkedList.class.getSimpleName();
    private static final Object object = new Object();
    /**
     * 指向链表的头部
     */
    Node<E> first;
    /**
     * 指向链表的尾部
     */
    Node<E> last;
    /**
     * 指向链表的当前节点，默认指向被加入的第一个节点
     */
    Node<E> head;

//    private static ChapterLinkedList mInstance;


//    public static ChapterLinkedList getInstance(){
//        if( mInstance == null){
//            synchronized (object){
//                if (mInstance == null){
//                    mInstance = new ChapterLinkedList();
//                }
//            }
//        }
//        return mInstance;
//    }

    public ChapterLinkedList() {
    }

    /**
     * 添加到头部
     * @param item 数据
     */
    public void addFirst(E item){
        synchronized (object) {
            Node<E> f = first;
            Node<E> newNode = new Node<>(null, item, f);
            first = newNode;
            if (f == null) {
                head = first;
                last = first;
            } else {
                f.prev = newNode;
            }
        }
    }


    public void addLast(E item){
        synchronized (object) {
            Node<E> l = last;
            Node<E> newNode = new Node<>(l, item, null);
            last = newNode;
            if (l == null) {
                head = last;
                first = last;
            } else {
                l.next = newNode;
            }
        }
    }

    /**
     * 获取当前head指向的节点
     * @return
     */
    public E getCurrentData(){
        if (head != null) {
            return head.item;
        }else{
            return null;
        }
    }

    /**
     * 获取指向item数据的节点，获取不到返回null
     * @param item
     * @return
     */
    private Node<E> get(E item){
        for(Node<E> x = first; x != null; x = x.next){
            if (x.item == item){
                return x;
            }
        }
        return null;
    }

    public E getFirstData(){
        if(first != null) {
            return first.item;
        }else{
            return null;
        }
    }

    public E getLastData(){
        if(last != null){
            return last.item;
        }else{
            return null;
        }
    }

    /**
     * 指向head节点的前一个节点
     * @return
     */
    public E getPrevData(){
        return head.prev.item;
    }

    /**
     * 指向head节点的后一个节点
     * @return
     */
    public E getNextData(){
        return head.next.item;
    }

    public void movePrev(){
        head = head.prev;
    }

    public void moveNext(){
        head = head.next;
    }

    public boolean hasPrevious(){
        return head != null && head.hasPrevious();
    }

    public boolean hasNext(){
        return head != null && head.hasNext();
    }

    /**
     * 超过最大缓存数量之后，清理多余的Node
     */
    public void cleanCache(){

    }

    /**
     * list清零
     */
    public void reset(){
        first = head = last = null;
    }

    /**
     * 移动head到指定的节点
     * @param item
     */
    public void moveTo(E item){
        Node<E> node = get(item);
        if (node != null){
            head = node;
            return;
        }

        throw new NoSuchElementException();
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NonNull
    @Override
    public Iterator iterator() {
        return new LinkedIterator();
    }

    private class LinkedIterator implements Iterator {
        Node<E> lastReturned;
        Node<E> next;

        public LinkedIterator() {
            lastReturned = first;
            next = first;
        }

        @Override
        public boolean hasNext() {
            return lastReturned.next != null;
        }


        @Override
        public Object next() {
            if (!hasNext())
                throw new NoSuchElementException();
            lastReturned = next;
            next = lastReturned.next;
            return lastReturned.item;
        }

        /**
         * Removes the last object returned by {@code next} from the collection.
         * This method can only be called once between each call to {@code next}.
         *
         * @throws UnsupportedOperationException if removing is not supported by the collection being
         *                                       iterated.
         * @throws IllegalStateException         if {@code next} has not been called, or {@code remove} has
         *                                       already been called after the last call to {@code next}.
         */
        @Override
        public void remove() {

        }
    }


    private static class Node<E> {
        private Node<E> prev;
        private E item;
        private Node<E> next;

        public Node(Node<E> prev, E item, Node<E> next) {
            this.prev = prev;
            this.item = item;
            this.next = next;
        }

        /**
         * 是否有前一个节点
         * @return
         */
        public boolean hasPrevious(){
            return prev != null;
        }

        /**
         * 是否有下一个节点
         * @return
         */
        public boolean hasNext(){
            return next != null;
        }

        public E getData(){
            return item;
        }
    }

}
