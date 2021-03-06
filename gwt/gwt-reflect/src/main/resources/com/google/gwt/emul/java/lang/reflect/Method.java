package java.lang.reflect;

import java.lang.annotation.Annotation;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.UnsafeNativeLong;
import com.google.gwt.reflect.shared.ReflectUtil;

/**
 * A <code>Method</code> provides information about, and access to, a single method on a class or interface.
 * The reflected method may be a class method or an instance method (including an abstract method).
 * <p>
 * A <code>Method</code> permits widening conversions to occur when matching the actual parameters to invoke
 * with the underlying method's formal parameters, but it throws an <code>IllegalArgumentException</code> if a
 * narrowing conversion would occur.
 *
 * @see Member
 * @see java.lang.Class
 * @see java.lang.Class#getMethods()
 * @see java.lang.Class#getMethod(String, Class[])
 * @see java.lang.Class#getDeclaredMethods()
 * @see java.lang.Class#getDeclaredMethod(String, Class[])
 * @author Kenneth Russell
 * @author Nakul Saraiya
 */
public class Method extends AccessibleObject implements GenericDeclaration, Member {

  private Class clazz;
  private String name;
  private Class returnType;
  private Class[] parameterTypes;
  private Class[] exceptionTypes;
  private int modifiers;
  // Generics and annotations support
  private transient String signature;

  private JavaScriptObject method;
  private JavaScriptObject annos;

  private int[] constants;
  
  // Modifiers that can be applied to a method in source code
  private static final int LANGUAGE_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
    Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL | Modifier.SYNCHRONIZED | Modifier.NATIVE;

  // Generics infrastructure

  private String getGenericSignature() {
    return signature;
  }

  protected Method(){}

  protected Method(JavaScriptObject method) {
//    this.constants = ConstPool.getConstPool().getArrayInt(constants);
    this.method = method;
    this.parameterTypes = exceptionTypes = new Class<?>[0];
    this.annos = JavaScriptObject.createObject();
  }
  
  public Method(Class from, String name, Class[] parameterTypes, Class returnType, Class[] checkedExceptions,
    int modifiers, JavaScriptObject method, JavaScriptObject annos) {
    this.clazz = from;
    this.name = name;
    this.parameterTypes = parameterTypes;
    this.returnType = returnType;
    this.exceptionTypes = checkedExceptions;
    this.modifiers = modifiers;
    this.signature = "";// should be able to infer signature
    this.method = method;
    this.annos = annos;
  }

  /**
   * Returns the <code>Class</code> object representing the class or interface that declares the method
   * represented by this <code>Method</code> object.
   */
  public Class<?> getDeclaringClass() {
    return clazz;
  }

  /**
   * Returns the name of the method represented by this <code>Method</code> object, as a <code>String</code>.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the Java language modifiers for the method represented by this <code>Method</code> object, as an
   * integer. The <code>Modifier</code> class should be used to decode the modifiers.
   *
   * @see Modifier
   */
  public int getModifiers() {
    return modifiers;
  }

  /**
   * Returns an array of <tt>TypeVariable</tt> objects that represent the type variables declared by the
   * generic declaration represented by this <tt>GenericDeclaration</tt> object, in declaration order. Returns
   * an array of length 0 if the underlying generic declaration declares no type variables.
   *
   * @return an array of <tt>TypeVariable</tt> objects that represent the type variables declared by this
   * generic declaration
   * @throws GenericSignatureFormatError if the generic signature of this generic declaration does not conform
   * to the format specified in the Java Virtual Machine Specification, 3rd edition
   * @since 1.5
   */
  public TypeVariable<Method>[] getTypeParameters() {
    // if (getGenericSignature() != null)
    // return (TypeVariable<Method>[])getGenericInfo().getTypeParameters();
    // else
    return (TypeVariable<Method>[])new TypeVariable[0];
  }

  /**
   * Returns a <code>Class</code> object that represents the formal return type of the method represented by
   * this <code>Method</code> object.
   *
   * @return the return type for the method this object represents
   */
  public Class<?> getReturnType() {
    return returnType;
  }

  /**
   * Returns a <tt>Type</tt> object that represents the formal return type of the method represented by this
   * <tt>Method</tt> object.
   * <p>
   * If the return type is a parameterized type, the <tt>Type</tt> object returned must accurately reflect the
   * actual type parameters used in the source code.
   * <p>
   * If the return type is a type variable or a parameterized type, it is created. Otherwise, it is resolved.
   *
   * @return a <tt>Type</tt> object that represents the formal return type of the underlying method
   * @throws GenericSignatureFormatError if the generic method signature does not conform to the format
   * specified in the Java Virtual Machine Specification, 3rd edition
   * @throws TypeNotPresentException if the underlying method's return type refers to a non-existent type
   * declaration
   * @throws MalformedParameterizedTypeException if the underlying method's return typed refers to a
   * parameterized type that cannot be instantiated for any reason
   * @since 1.5
   */
  public Type getGenericReturnType() {
    // if (getGenericSignature() != null) {
    // return getGenericInfo().getReturnType();
    // } else {
    return getReturnType();
    // }
  }

  /**
   * Returns an array of <code>Class</code> objects that represent the formal parameter types, in declaration
   * order, of the method represented by this <code>Method</code> object. Returns an array of length 0 if the
   * underlying method takes no parameters.
   *
   * @return the parameter types for the method this object represents
   */
  public Class<?>[] getParameterTypes() {
    return (Class<?>[])parameterTypes;// .clone();
  }

  /**
   * Returns an array of <tt>Type</tt> objects that represent the formal parameter types, in declaration
   * order, of the method represented by this <tt>Method</tt> object. Returns an array of length 0 if the
   * underlying method takes no parameters.
   * <p>
   * If a formal parameter type is a parameterized type, the <tt>Type</tt> object returned for it must
   * accurately reflect the actual type parameters used in the source code.
   * <p>
   * If a formal parameter type is a type variable or a parameterized type, it is created. Otherwise, it is
   * resolved.
   *
   * @return an array of Types that represent the formal parameter types of the underlying method, in
   * declaration order
   * @throws GenericSignatureFormatError if the generic method signature does not conform to the format
   * specified in the Java Virtual Machine Specification, 3rd edition
   * @throws TypeNotPresentException if any of the parameter types of the underlying method refers to a
   * non-existent type declaration
   * @throws MalformedParameterizedTypeException if any of the underlying method's parameter types refer to a
   * parameterized type that cannot be instantiated for any reason
   * @since 1.5
   */
  public Type[] getGenericParameterTypes() {
    // No generics support yet :/
    // if (getGenericSignature() != null)
    // return getGenericInfo().getParameterTypes();
    // else
    return getParameterTypes();
  }

  /**
   * Returns an array of <code>Class</code> objects that represent the types of the exceptions declared to be
   * thrown by the underlying method represented by this <code>Method</code> object. Returns an array of
   * length 0 if the method declares no exceptions in its <code>throws</code> clause.
   *
   * @return the exception types declared as being thrown by the method this object represents
   */
  public Class<?>[] getExceptionTypes() {
    return (Class<?>[])exceptionTypes;// .clone();
  }

  /**
   * Returns an array of <tt>Type</tt> objects that represent the exceptions declared to be thrown by this
   * <tt>Method</tt> object. Returns an array of length 0 if the underlying method declares no exceptions in
   * its <tt>throws</tt> clause.
   * <p>
   * If an exception type is a parameterized type, the <tt>Type</tt> object returned for it must accurately
   * reflect the actual type parameters used in the source code.
   * <p>
   * If an exception type is a type variable or a parameterized type, it is created. Otherwise, it is
   * resolved.
   *
   * @return an array of Types that represent the exception types thrown by the underlying method
   * @throws GenericSignatureFormatError if the generic method signature does not conform to the format
   * specified in the Java Virtual Machine Specification, 3rd edition
   * @throws TypeNotPresentException if the underlying method's <tt>throws</tt> clause refers to a
   * non-existent type declaration
   * @throws MalformedParameterizedTypeException if the underlying method's <tt>throws</tt> clause refers to a
   * parameterized type that cannot be instantiated for any reason
   * @since 1.5
   */
  public Type[] getGenericExceptionTypes() {
    // Type[] result;
    // if (getGenericSignature() != null &&
    // ((result = getGenericInfo().getExceptionTypes()).length > 0))
    // return result;
    // else
    return getExceptionTypes();
  }

  /**
   * Compares this <code>Method</code> against the specified object. Returns true if the objects are the same.
   * Two <code>Methods</code> are the same if they were declared by the same class and have the same name and
   * formal parameter types and return type.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj instanceof Method) {
      Method other = (Method)obj;
      if ((getDeclaringClass() == other.getDeclaringClass()) && (getName() == other.getName())) {
        if (!returnType.equals(other.getReturnType())) return false;
        /* Avoid unnecessary cloning */
        Class[] params1 = parameterTypes;
        Class[] params2 = other.parameterTypes;
        if (params1.length == params2.length) {
          for (int i = 0; i < params1.length; i++) {
            if (params1[i] != params2[i]) return false;
          }
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns a hashcode for this <code>Method</code>. The hashcode is computed as the exclusive-or of the
   * hashcodes for the underlying method's declaring class name and the method's name.
   */
  @Override
  public int hashCode() {
    return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
  }

  /**
   * Returns a string describing this <code>Method</code>. The string is formatted as the method access
   * modifiers, if any, followed by the method return type, followed by a space, followed by the class
   * declaring the method, followed by a period, followed by the method name, followed by a parenthesized,
   * comma-separated list of the method's formal parameter types. If the method throws checked exceptions, the
   * parameter list is followed by a space, followed by the word throws followed by a comma-separated list of
   * the thrown exception types. For example:
   *
   * <pre>
   *    public boolean java.lang.Object.equals(java.lang.Object)
   * </pre>
   * <p>
   * The access modifiers are placed in canonical order as specified by "The Java Language Specification".
   * This is <tt>public</tt>, <tt>protected</tt> or <tt>private</tt> first, and then other modifiers in the
   * following order: <tt>abstract</tt>, <tt>static</tt>, <tt>final</tt>, <tt>synchronized</tt>,
   * <tt>native</tt>.
   */
  @Override
  public String toString() {
    try {
      StringBuffer sb = new StringBuffer();
      int mod = getModifiers() & LANGUAGE_MODIFIERS;
      if (mod != 0) {
        sb.append(Modifier.toString(mod) + " ");
      }
      sb.append(Field.getTypeName(getReturnType()) + " ");
      sb.append(Field.getTypeName(getDeclaringClass()) + ".");
      sb.append(getName() + "(");
      Class[] params = getParameterTypes();
      for (int j = 0; j < params.length; j++) {
        sb.append(Field.getTypeName(params[j]));
        if (j < (params.length - 1)) sb.append(",");
      }
      sb.append(")");
      Class[] exceptions = getExceptionTypes();
      if (exceptions != null && exceptions.length > 0) {
        sb.append(" throws ");
        for (int k = 0; k < exceptions.length; k++) {
          sb.append(exceptions[k].getName());
          if (k < (exceptions.length - 1)) sb.append(",");
        }
      }
      return sb.toString();
    } catch (Exception e) {
      return "<" + e + ">";
    }
  }

  /**
   * Returns a string describing this <code>Method</code>, including type parameters. The string is formatted
   * as the method access modifiers, if any, followed by an angle-bracketed comma-separated list of the
   * method's type parameters, if any, followed by the method's generic return type, followed by a space,
   * followed by the class declaring the method, followed by a period, followed by the method name, followed
   * by a parenthesized, comma-separated list of the method's generic formal parameter types. A space is used
   * to separate access modifiers from one another and from the type parameters or return type. If there are
   * no type parameters, the type parameter list is elided; if the type parameter list is present, a space
   * separates the list from the class name. If the method is declared to throw exceptions, the parameter list
   * is followed by a space, followed by the word throws followed by a comma-separated list of the generic
   * thrown exception types. If there are no type parameters, the type parameter list is elided.
   * <p>
   * The access modifiers are placed in canonical order as specified by "The Java Language Specification".
   * This is <tt>public</tt>, <tt>protected</tt> or <tt>private</tt> first, and then other modifiers in the
   * following order: <tt>abstract</tt>, <tt>static</tt>, <tt>final</tt>, <tt>synchronized</tt>
   * <tt>native</tt>.
   *
   * @return a string describing this <code>Method</code>, include type parameters
   * @since 1.5
   */
  public String toGenericString() {
    try {
      StringBuilder sb = new StringBuilder();
      int mod = getModifiers() & LANGUAGE_MODIFIERS;
      if (mod != 0) {
        sb.append(Modifier.toString(mod) + " ");
      }
      Type[] typeparms = getTypeParameters();
      if (typeparms.length > 0) {
        boolean first = true;
        sb.append("<");
        for (Type typeparm : typeparms) {
          if (!first) sb.append(",");
          if (typeparm instanceof Class)
            sb.append(((Class)typeparm).getName());
          else
            sb.append(typeparm.toString());
          first = false;
        }
        sb.append("> ");
      }

      Type genRetType = getGenericReturnType();
      sb.append(((genRetType instanceof Class) ? Field.getTypeName((Class)genRetType) : genRetType.toString()) +
        " ");

      sb.append(Field.getTypeName(getDeclaringClass()) + ".");
      sb.append(getName() + "(");
      Type[] params = getGenericParameterTypes();
      for (int j = 0; j < params.length; j++) {
        sb.append((params[j] instanceof Class) ? Field.getTypeName((Class)params[j]) : (params[j].toString()));
        if (j < (params.length - 1)) sb.append(",");
      }
      sb.append(")");
      Type[] exceptions = getGenericExceptionTypes();
      if (exceptions.length > 0) {
        sb.append(" throws ");
        for (int k = 0; k < exceptions.length; k++) {
          sb.append((exceptions[k] instanceof Class) ? ((Class)exceptions[k]).getName()
            : exceptions[k].toString());
          if (k < (exceptions.length - 1)) sb.append(",");
        }
      }
      return sb.toString();
    } catch (Exception e) {
      return "<" + e + ">";
    }
  }

  /**
   * Invokes the underlying method represented by this <code>Method</code> object, on the specified object
   * with the specified parameters. Individual parameters are automatically unwrapped to match primitive
   * formal parameters, and both primitive and reference parameters are subject to method invocation
   * conversions as necessary.
   * <p>
   * If the underlying method is static, then the specified <code>obj</code> argument is ignored. It may be
   * null.
   * <p>
   * If the number of formal parameters required by the underlying method is 0, the supplied <code>args</code>
   * array may be of length 0 or null.
   * <p>
   * If the underlying method is an instance method, it is invoked using dynamic method lookup as documented
   * in The Java Language Specification, Second Edition, section 15.12.4.4; in particular, overriding based on
   * the runtime type of the target object will occur.
   * <p>
   * If the underlying method is static, the class that declared the method is initialized if it has not
   * already been initialized.
   * <p>
   * If the method completes normally, the value it returns is returned to the caller of invoke; if the value
   * has a primitive type, it is first appropriately wrapped in an object. However, if the value has the type
   * of an array of a primitive type, the elements of the array are <i>not</i> wrapped in objects; in other
   * words, an array of primitive type is returned. If the underlying method return type is void, the
   * invocation returns null.
   *
   * @param obj the object the underlying method is invoked from
   * @param args the arguments used for the method call
   * @return the result of dispatching the method represented by this object on <code>obj</code> with
   * parameters <code>args</code>
   * @exception IllegalAccessException if this <code>Method</code> object enforces Java language access control
   * and the underlying method is inaccessible.
   * @exception IllegalArgumentException if the method is an instance method and the specified object argument
   * is not an instance of the class or interface declaring the underlying method (or of a subclass or
   * implementor thereof); if the number of actual and formal parameters differ; if an unwrapping conversion
   * for primitive arguments fails; or if, after possible unwrapping, a parameter value cannot be converted to
   * the corresponding formal parameter type by a method invocation conversion.
   * @exception InvocationTargetException if the underlying method throws an exception.
   * @exception NullPointerException if the specified object is null and the method is an instance method.
   * @exception ExceptionInInitializerError if the initialization provoked by this method fails.
   */
  @UnsafeNativeLong
  public Object invoke(Object obj, Object ... initargs) throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
    return call(method, obj, initargs);
  }

  @UnsafeNativeLong
  private native Object call(JavaScriptObject func, Object obj, Object[] args)
  /*-{
    // The method we're calling is "static", and takes obj as first parameter
    args.unshift(obj);
    // The rest of the args are accessed by generated param names A, B, C...
    return func.apply(this, args);
  }-*/;

  /**
   * Returns <tt>true</tt> if this method is a bridge method; returns <tt>false</tt> otherwise.
   *
   * @return true if and only if this method is a bridge method as defined by the Java Language Specification.
   * @since 1.5
   */
  public boolean isBridge() {
    return (getModifiers() & Modifier.BRIDGE) != 0;
  }

  /**
   * Returns <tt>true</tt> if this method was declared to take a variable number of arguments; returns
   * <tt>false</tt> otherwise.
   *
   * @return <tt>true</tt> if an only if this method was declared to take a variable number of arguments.
   * @since 1.5
   */
  public boolean isVarArgs() {
    return (getModifiers() & Modifier.VARARGS) != 0;
  }

  /**
   * Returns <tt>true</tt> if this method is a synthetic method; returns <tt>false</tt> otherwise.
   *
   * @return true if and only if this method is a synthetic method as defined by the Java Language
   * Specification.
   * @since 1.5
   */
  public boolean isSynthetic() {
    return Modifier.isSynthetic(getModifiers());
  }

  /**
   * @throws NullPointerException {@inheritDoc}
   * @since 1.5
   */
  @Override
  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    if (annotationClass == null)
      throw new NullPointerException("Null annotationClass in getAnnotation() for "+this);
    return ReflectUtil.getAnnotation(annos, annotationClass);
  }

  /**
   * @since 1.5
   */
  @Override
  public Annotation[] getDeclaredAnnotations() {
    return ReflectUtil.getAnnotations(annos, new Annotation[0]);
  }

  /**
   * Returns the default value for the annotation member represented by this <tt>Method</tt> instance. If the
   * member is of a primitive type, an instance of the corresponding wrapper type is returned. Returns null if
   * no default is associated with the member, or if the method instance does not represent a declared member
   * of an annotation type.
   *
   * @return the default value for the annotation member represented by this <tt>Method</tt> instance.
   * @throws TypeNotPresentException if the annotation is of type {@link Class} and no definition can be found
   * for the default class value.
   * @since 1.5
   */
  public Object getDefaultValue() {
    // if (annotationDefault == null)
    return null;
    // Class memberType = AnnotationType.invocationHandlerReturnType(
    // getReturnType());
    // Object result = AnnotationParser.parseMemberValue(
    // memberType, ByteBuffer.wrap(annotationDefault),
    // sun.misc.SharedSecrets.getJavaLangAccess().
    // getConstantPool(getDeclaringClass()),
    // getDeclaringClass());
    // if (result instanceof sun.reflect.annotation.ExceptionProxy)
    // throw new AnnotationFormatError("Invalid default: " + this);
    // return result;
  }

  /**
   * Returns an array of arrays that represent the annotations on the formal parameters, in declaration order,
   * of the method represented by this <tt>Method</tt> object. (Returns an array of length zero if the
   * underlying method is parameterless. If the method has one or more parameters, a nested array of length
   * zero is returned for each parameter with no annotations.) The annotation objects contained in the
   * returned arrays are serializable. The caller of this method is free to modify the returned arrays; it
   * will have no effect on the arrays returned to other callers.
   *
   * @return an array of arrays that represent the annotations on the formal parameters, in declaration order,
   * of the method represented by this Method object
   * @since 1.5
   */
  public Annotation[][] getParameterAnnotations() {
    // int numParameters = parameterTypes.length;
    // if (parameterAnnotations == null)
    // return new Annotation[numParameters][0];
    return new Annotation[0][];
    //
    // Annotation[][] result = AnnotationParser.parseParameterAnnotations(
    // parameterAnnotations,
    // sun.misc.SharedSecrets.getJavaLangAccess().
    // getConstantPool(getDeclaringClass()),
    // getDeclaringClass());
    // if (result.length != numParameters)
    // throw new java.lang.annotation.AnnotationFormatError(
    // "Parameter annotations don't match number of parameters");
    // return result;
  }
}
