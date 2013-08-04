package com.google.gwt.reflect.rebind;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.gwt.reflect.client.MemberPool;

public class JvmMemberPool <T> implements MemberPool<T>{

  private final Class<T> type;

  public JvmMemberPool(Class<T> type) {
    this.type = type;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annoCls) {
    return type.getAnnotation(annoCls);
  }

  @Override
  public Annotation[] getAnnotations() {
    return type.getAnnotations();
  }

  @Override
  public Annotation[] getDeclaredAnnotations() {
    return type.getDeclaredAnnotations();
  }

  @Override
  public Constructor<T> getConstructor(Class<?> ... params) throws NoSuchMethodException {
    return type.getConstructor(params);
  }

  @Override
  public Constructor<T> getDeclaredConstructor(Class<?> ... params) throws NoSuchMethodException {
    return type.getDeclaredConstructor(params);
  }

  @Override
  public Constructor<T>[] getConstructors() {
    return (Constructor<T>[])type.getConstructors();
  }

  @Override
  public Constructor<T>[] getDeclaredConstructors() {
    return (Constructor<T>[])type.getDeclaredConstructors();
  }

  @Override
  public Field getField(String name) throws NoSuchFieldException {
    return type.getField(name);
  }

  @Override
  public Field getDeclaredField(String name) throws NoSuchFieldException {
    return type.getDeclaredField(name);
  }

  @Override
  public Field[] getFields() {
    return type.getFields();
  }

  @Override
  public Field[] getDeclaredFields() {
    return type.getDeclaredFields();
  }

  @Override
  public Method getMethod(String name, Class<?> ... params) throws NoSuchMethodException {
    return type.getMethod(name, params);
  }

  @Override
  public Method getDeclaredMethod(String name, Class<?> ... params) throws NoSuchMethodException {
    return type.getDeclaredMethod(name, params);
  }

  @Override
  public Method[] getMethods() {
    return type.getMethods();
  }

  @Override
  public Method[] getDeclaredMethods() {
    return type.getDeclaredMethods();
  }

  @Override
  public MemberPool<? super T> getSuperclass() {
    return new JvmMemberPool(type.getSuperclass());
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public MemberPool<? super T>[] getInterfaces() {
    Class<?>[] ifaces = type.getInterfaces();
    MemberPool[] members = new MemberPool[ifaces.length];
    for (int i = ifaces.length; i-->0;) {
      members[i] = new JvmMemberPool(ifaces[i]);
    }
    return members;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public MemberPool<?>[] getClasses() {
    Class<?>[] classes = type.getClasses();
    MemberPool<?>[] members = new MemberPool[classes.length];
    for (int i = classes.length; i-->0;) {
      members[i] = new JvmMemberPool(classes[i]);
    }
    return members;
  }

  @Override
  public Class<T> getType() {
    return type;
  }

}
