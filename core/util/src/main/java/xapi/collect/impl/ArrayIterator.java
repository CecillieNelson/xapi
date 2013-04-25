package xapi.collect.impl;

import java.util.Iterator;

import xapi.except.NotImplemented;

public class ArrayIterator <E> implements Iterable<E> {

  private final E[] array;

  private final class Itr implements Iterator<E> {
    int pos = 0, end = array.length;
    @Override
    public boolean hasNext() {
      return pos < end;
    }
    @Override
    public E next() {
      return array[pos++];
    }
    @Override
    public void remove() {
      ArrayIterator.this.remove(array[pos-1]);
    }
  }

  public ArrayIterator(E[] array) {
    this.array = array;
  }

  @Override
  public Iterator<E> iterator() {
    return new Itr();
  }

  protected void remove(E key) {
    throw new NotImplemented("ArrayIterator does not support remove");
  }

}
