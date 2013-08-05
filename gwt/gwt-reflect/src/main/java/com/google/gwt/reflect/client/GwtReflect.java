package com.google.gwt.reflect.client;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.gwt.core.client.UnsafeNativeLong;
import com.google.gwt.core.shared.GWT;


/**
 * A set of static accessor classes to enable reflection in gwt.
 *
 * Each method should be treated like GWT.create; you must send a Class literal, and not a reference.
 *
 * Literal: SomeClass.class
 * Reference: Class<?> someClass;
 *
 * Some methods will fail gracefully if you let a reference slip through.
 * Gwt production compiles will warn you if it can generate a sub-optimal solution
 * (aka maps from reference to factory), but will throw an error if it cannot deliver the functionality.
 *
 * @author James X. Nelson (james@wetheinter.net, @james)
 *
 */
public class GwtReflect {

  private GwtReflect() {}

  /**
   * Ensures that a given class has all its reflection data filled in.
   *
   * A magic method injection optimizes this in production mode.
   * You MUST send a class literal for this process to work in production.
   *
   * Work is in progress to create a monolithic runtime factory,
   * so when a non-constant literal is encountered,
   * the prod mode implementation can do a runtime lookup of the type.
   *
   * A flag may be created to allow class refs to fall through and do nothing,
   * but a do-nothing call should just be erased, not worked around.
   *
   * Gwt dev and standard jvms will just call standard reflection methods,
   * so they do nothing to make a class magic.
   *
   * @param cls - The class to enhance in gwt production mode.
   * @return - The same class, casted to a compatible generic supertype.
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> magicClass(Class<? extends T> cls) {
    assert cls != null;
    return Class.class.cast(cls);
  }

  /**
   *
   * In gwt dev and standard jvms, this just calls cls.getConstructor(...).newInstance(...);
   * in gwt production, this is a magic method which will generate calls to new T(params);
   *
   * Note that for gwt production to be fully optimized, you must always send class literals (SomeClass.class)
   * If you send a class reference (a Class&lt;?> object),
   * the magic method injector will be forced to generate a monolithic helper class.
   *
   * In gwt production, this method will avoid generating the magic class metadata.
   *
   * @param cls - The class on which to call .newInstance();
   * @param paramSignature - The constructor parameter signature
   * @param params - The actual objects (which should be assignable to param signature).
   * @return A new instance of type T
   * @throws Exception - Standard reflection exceptions in java vms, generator-base exceptions in js vms.
   */
  public static <T> T construct(Class<? extends T> cls, Class<?>[] paramSignature, Object ... params)
    throws Exception {
    return magicClass(cls).getConstructor(paramSignature).newInstance(params);
  }

  /**
   * For the time being you MUST send only class literals to this method.
   * <p>
   * Returns a new Typed[size], null-initialized and properly typed.
   * Utilizes standard java array reflection in gwt dev and plain jvms.
   * <p>
   * If you want to create multi dimensional arrays with only one dimension defined,
   * just call SomeType[][][] array = newArray(SomeType[][].class, 2);
   * <p>
   * It you need to create primitive arrays, prefer {@link Array#newInstance(Class, int)},
   * which returns type Object, and cast it yourself.  Because this type signature is generic,
   * int, double and friends will auto-box.  The only difference between this method and the
   * one from java.lang.reflect.Array is the return type is typesafely cast for you.
   * <p>
   * In gwt production mode, this method call is replaced with new T[dimensions[0]][dimensions[1]...[];
   * <p>
   * Failing to use a class literal will currently make the compiler fail,
   * and will eventually resort to a runtime lookup in the ConstPool to get a seed array to clone.
   *
   * @param classLit - The class for which a new array will be created.
   * @param size - The size of the new array.
   * @return new T[dimension]
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] newArray(Class<?> classLit, int size) {
    return (T[])Array.newInstance(classLit, size);
  }


  /**
   * For the time being you MUST send only class literals to this method.
   * <p>
   * Returns a two-dimensional array, with the inner two dimensions filled in.
   * Utilizes standard java array reflection in gwt dev and plain jvms.
   * <p>
   * If you want to create complex multi-dimensional arrays this method will fill in
   * the two inner dimensions of whatever class you send (array classes welcome).
   * SomeType[][][][] array = newArray(SomeType[][].class, 4, 4);
   * <p>
   * It you need to create primitive arrays, or more complex multi-dimensional arrays,
   * prefer {@link Array#newInstance(Class, int ...)}, which returns type Object,
   * and cast it yourself.  Because this type signature is generic,
   * int, double and friends will auto-box.  The only difference between this method and the
   * one from java.lang.reflect.Array is the return type is typesafely cast for you.
   * <p>
   * In gwt production mode, this method call is replaced with new T[dimensions[0]][dimensions[1]...[];
   * <p>
   * Failing to use a class literal will currently make the compiler fail,
   * and will eventually resort to a runtime lookup in the ConstPool to get a seed array to clone.
   *
   * @param classLit - The class for which a new array will be created.
   * @param dim1 - The size of the new array's inner dimension.
   * @param dim1 - The size of the new array's outer dimension.
   * @return new T[dim1][dim2];
   */
  @SuppressWarnings("unchecked")
  public static <T> T[][] newArray(Class<T> classLit, int dim1, int dim2) {
    return (T[][])Array.newInstance(classLit, dim1, dim2);
  }

  /**
   * In jvms, we defer to java.lang.reflect.Array;
   * In gwt, java.lang.reflect.Array defers to here,
   * where the compiler will call into the c.g.g.jjs.instrinc.c.g.g.lang super source.
   *
   * @param array - Any array[] instance; java or js
   * @return - The number of elements in the [].
   */
  public static int arrayLength(Object array) {
    if (GWT.isProdMode())
      return jsniLength(array);
    else
      return Array.getLength(array);
  }

  public static Object arrayGet(Object array, int index) {
    if (GWT.isProdMode())
      return jsniGet(array, index);
    else
      return Array.get(array, index);
  }

  public static native int jsniLength(Object array)
  /*-{
    return array.length;
  }-*/;

  public static native Object jsniGet(Object array, int index)
  /*-{
    return array[index];
  }-*/;

  public static String joinClasses(String separator, Class<?> ... vals) {
    int ind = vals.length;
    String[] values = new String[ind];
    for(;ind-->0;){
      Class<?> cls = vals[ind];
      if (cls != null)
        values[ind] = cls.getCanonicalName();
    }
    if (values.length == 0) return "";// need at least one element
    // all string operations use a new array, so minimize all calls possible
    char[] sep = separator.toCharArray();

    // determine final size and normalize nulls
    int totalSize = (values.length - 1) * sep.length;// separator size
    for (int i = 0; i < values.length; i++) {
      if (values[i] == null)
        values[i] = "";
      else
        totalSize += values[i].length();
    }

    // exact size; no bounds checks or resizes
    char[] joined = new char[totalSize];
    ind = 0;
    // note, we are iterating all the elements except the last one
    int i = 0, end = values.length - 1;
    for (; i < end; i++) {
      System.arraycopy(values[i].toCharArray(), 0, joined, ind, values[i].length());
      ind += values[i].length();
      System.arraycopy(sep, 0, joined, ind, sep.length);
      ind += sep.length;
    }
    // now, add the last element;
    // this is why we checked values.length == 0 off the hop
    String last = values[end];
    System.arraycopy(last.toCharArray(), 0, joined, ind, last.length());

    return new String(joined);
  }

    /**
     * Escapes string content to be a valid string literal.
     * Copied directly from {@link com.google.gwt.core.ext.Generator#escape(String)}
     *
     * @return an escaped version of <code>unescaped</code>, suitable for being
     *         enclosed in double quotes in Java source
     */
    public static String escape(String unescaped) {
      int extra = 0;
      for (int in = 0, n = unescaped.length(); in < n; ++in) {
        switch (unescaped.charAt(in)) {
          case '\0':
          case '\n':
          case '\r':
          case '\"':
          case '\\':
            ++extra;
            break;
        }
      }

      if (extra == 0) {
        return unescaped;
      }

      char[] oldChars = unescaped.toCharArray();
      char[] newChars = new char[oldChars.length + extra];
      for (int in = 0, out = 0, n = oldChars.length; in < n; ++in, ++out) {
        char c = oldChars[in];
        switch (c) {
          case '\0':
            newChars[out++] = '\\';
            c = '0';
            break;
          case '\n':
            newChars[out++] = '\\';
            c = 'n';
            break;
          case '\r':
            newChars[out++] = '\\';
            c = 'r';
            break;
          case '\"':
            newChars[out++] = '\\';
            c = '"';
            break;
          case '\\':
            newChars[out++] = '\\';
            c = '\\';
            break;
        }
        newChars[out] = c;
      }
      return String.valueOf(newChars);
  }
  
  @SuppressWarnings("unchecked")
  public static <T> Constructor<T>[] getDeclaredConstructors(Class<T> c) {
    return Constructor[].class.cast(makeAccessible(c.getDeclaredConstructors()));
  }
  
  @SuppressWarnings("unchecked")
  public static <T> Constructor<? super T>[] getPublicConstructors(Class<T> c) {
    return Constructor[].class.cast(c.getConstructors());
  }
  
  public static <T> Constructor<T> getDeclaredConstructor(Class<T> c, Class<?> ... params) {
    try {
      return makeAccessible(c.getDeclaredConstructor(params));
    } catch (NoSuchMethodException e) {
      log("Could not retrieve "+c+"("+joinClasses(", ", params),e);
      throw new RuntimeException(e);
    }
  }
  
  public static <T> Constructor<T> getPublicConstructor(Class<T> c, Class<?> ... params) {
    try {
      return c.getConstructor(params);
    } catch (NoSuchMethodException e) {
      log("Could not retrieve "+c+"("+joinClasses(", ", params),e);
      throw new RuntimeException(e);
    }
  }
    
  public static Field[] getDeclaredFields(Class<?> c) {
    return makeAccessible(c.getDeclaredFields());
  }

  public static Field[] getPublicFields(Class<?> c) {
    return c.getFields();
  }
  
  public static Field getDeclaredField(Class<?> c, String name) {
    try {
      return makeAccessible(c.getDeclaredField(name));
    } catch (NoSuchFieldException e) {
      log("Could not retrieve "+c+"."+name,e);
      throw new RuntimeException(e);
    }
  }
  
  public static Field getPublicField(Class<?> c, String name) {
    try {
      return c.getField(name);
    } catch (NoSuchFieldException e) {
      log("Could not retrieve "+c+"."+name,e);
      throw new RuntimeException(e);
    }
  }
  
  public static Method[] getDeclaredMethods(Class<?> c) {
    return makeAccessible(c.getDeclaredMethods());
  }
  
  public static Method[] getPublicMethods(Class<?> c) {
    return c.getMethods();
  }
  
  public static Method getDeclaredMethod(Class<?> c, String name, Class<?> ... params) {
    try {
      return makeAccessible(c.getDeclaredMethod(name, params));
    } catch (NoSuchMethodException e) {
      log("Could not retrieve "+c+"."+name+"("+joinClasses(", ", params),e);
      throw new RuntimeException(e);
    }
  }
  
  public static Method getPublicMethod(Class<?> c, String name, Class<?> ... params) {
    try {
      return c.getMethod(name, params);
    } catch (NoSuchMethodException e) {
      log("Could not retrieve "+c+"."+name+"("+joinClasses(", ", params),e);
      throw new RuntimeException(e);
    }
  }
  
  private static <T extends AccessibleObject> T makeAccessible(T member) {
    // TODO use security manager
    if (!member.isAccessible())
      member.setAccessible(true);
    return member;
  }

  private static <T extends AccessibleObject> T[] makeAccessible(T[] members) {
    for (T member : members)
      makeAccessible(member);
    return members;
  }

  private static void log(String string, Throwable e) {
    GWT.log(string, e);
  }

  @UnsafeNativeLong
  private static Long boxLong(long l) {return new Long(l);}

  @UnsafeNativeLong
  private static long unboxLong(Long l) {return l.longValue();}
  
}
