package xapi.dev.resource.impl;

import java.io.DataInputStream;
import java.io.IOException;

import xapi.bytecode.ClassFile;
import xapi.dev.resource.api.ClasspathResource;
import xapi.util.X_Util;

public class ByteCodeResource extends DelegateClasspathResource{

  private ClassFile data;

  public ByteCodeResource(ClasspathResource source) {
    super(source);
  }

  public ClassFile getClassData() {
    if (data == null) {
      try {
        DataInputStream in = new DataInputStream(open());
        try {
          data = new ClassFile(in);
        }finally {
          in.close();
        }
      } catch(IOException e) {
        throw X_Util.rethrow(e);
      }
    }
    return data;
  }
  
  public String toString() {
    return getClassData().getName();
  };
  
}
