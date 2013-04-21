package xapi.collect.api;

import xapi.collect.proxy.CollectionProxy;

public interface ObjectTo <K, V>
extends EntryIterable<K,V>, CollectionProxy<K,V>, HasValues<K,V>
{

  static interface Many <K, V> extends ObjectTo<K, IntTo<V>> {
  }

// Inherited from CollectionProxy
//  V get(K key);
//  boolean remove(K key);
  V put(K key, V value);

  Class<K> keyType();
  Class<V> valueType();

  Class<?> componentType();

}
