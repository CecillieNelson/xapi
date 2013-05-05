package xapi.source.read;


public class JavaVisitor <Param> {

  /**
   * The default TypeData object is used to deserialize source-compatible type names.
   * 
   * Lexing package names and inner classes requires the use java convention naming:
   * lowercase.packages.Uppercase.Classes
   * 
   * packagename with uppercase first-chars in fragments will be lexed incorrectly.
   * 
   * @author "James X. Nelson (james@wetheinter.net)"
   *
   */
  public static class TypeData {
    
    public static final TypeData NONE = new TypeData("");
    
    public TypeData(String name) {
      clsName = name;
      int end = name.lastIndexOf('.');
      if (end == -1)
        simpleName = clsName;
      else
        simpleName = clsName.substring(end+1);
    }
    
    /**
     * The prefix of lowercase package fragments.
     * Might be "super" or "this"
     */
    public String pkgName = "";
    /**
     * The fully qualified class (including parent classes, if any)
     */
    public String clsName;
    /**
     * The last class fragment
     */
    public String simpleName;
    
    /**
     * Any generics string, including enclosing &lt; &gt; characters.
     */
    public String generics = "";
    /**
     * The number of array dimensions, if any, for this type.
     */
    public int arrayDepth;
    
    @Override
    public String toString() {
      StringBuilder b = new StringBuilder();
      if (pkgName.length() > 0)
        b.append(pkgName).append('.');
      b.append(clsName).append(generics);
      b.append(getArrayDefinition());
      return b.toString();
    }

    protected String getArrayDefinition() {
      StringBuilder b = new StringBuilder();
      int i = arrayDepth;
      while (i --> 0)
        b.append("[]");
      return b.toString();
    }

    public String getImportName() {
      int index = clsName.indexOf('.');
      return (pkgName.length()==0?"":pkgName+"."
          +(index == -1 ? clsName : clsName.substring(0, index)));
    }

    public String getQualifiedName() {
      return (pkgName.length()==0?"":pkgName+".")+clsName;
    }

    public String getSimpleName() {
      return clsName
          + (generics.length() > 0 ? generics : "")
          + getArrayDefinition()
      ;
    }
  }
  
  public static interface JavadocVisitor <Param> {
    void visitJavadoc(String javadoc, Param receiver);
  }
  
  public static interface ModifierVisitor <Param> {
    void visitModifier(int modifier, Param receiver);
  }
  
  public static interface TypeVisitor <Param> extends ModifierVisitor <Param> {
//    void visitType(String type, Param receiver);
  }
  
  public static interface AnnotationVisitor <Param> {
    AnnotationMemberVisitor<Param> visitAnnotation(String annoName, String annoBody, Param receiver);
  }

  public static interface AnnotationMemberVisitor <Param> {
    void visitMember(String name, String value, Param receiver);
  }

  public static interface GenericVisitor <Param> {
    void visitGeneric(String generic, Param receiver);
  }

  public static interface MemberVisitor <Param> 
  extends AnnotationVisitor<Param>, 
  GenericVisitor<Param>, 
  JavadocVisitor<Param>,
  ModifierVisitor<Param>
  {
  }
  
  public static interface FieldVisitor <Param> 
  extends MemberVisitor<Param> {
    void visitName(String type, Param receiver);
    void visitInitializer(String initializer, Param receiver);
  }

  public static interface ParameterVisitor <Param> 
  extends AnnotationVisitor<Param>, ModifierVisitor<Param> {
    void visitType(TypeData type, String name, boolean varargs, Param receiver);
  }
  
  public static interface ExecutableVisitor <Param> 
  extends MemberVisitor<Param> {
    ParameterVisitor<Param> visitParameter();
    void visitException(String type, Param receiver);
  }
  
  public static interface ConstructorVisitor <Param> 
  extends ExecutableVisitor<Param> {
  }
  
  public static interface MethodVisitor <Param> 
  extends ExecutableVisitor<Param> {
    void visitReturnType(TypeData returnType, Param receiver);
    void visitName(String name, Param receiver);
  }
  
}
