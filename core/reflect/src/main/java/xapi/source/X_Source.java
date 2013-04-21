package xapi.source;


import static xapi.source.api.AccessFlag.isAbstract;
import static xapi.source.api.AccessFlag.isFinal;
import static xapi.source.api.AccessFlag.isNative;
import static xapi.source.api.AccessFlag.isPrivate;
import static xapi.source.api.AccessFlag.isProtected;
import static xapi.source.api.AccessFlag.isPublic;
import static xapi.source.api.AccessFlag.isStatic;
import static xapi.source.api.AccessFlag.isStrict;
import static xapi.source.api.AccessFlag.isSynchronized;
import static xapi.source.api.AccessFlag.isTransient;
import static xapi.source.api.AccessFlag.isVolatile;

import javax.inject.Provider;

import xapi.inject.X_Inject;
import xapi.source.api.IsType;
import xapi.source.service.SourceService;



public class X_Source {

  private X_Source() {}
  
  private static final Provider<SourceService> service = X_Inject.singletonLazy(SourceService.class);

  public static IsType toType(Class<?> cls) {
    return service.get().toType(cls);
  }
  public static IsType toType(String pkg, String enclosedName) {
    return service.get().toType(pkg.replace('/', '.'), enclosedName.replace('$', '.'));
  }

  public static String modifierToProtection(int protection) {
    return
      isPublic(protection) ? "public" :
      isProtected(protection) ? "protected" :
      isPrivate(protection) ? "private" :
      "";
  }
  public static String classModifiers(int protection) {
    return
      modifierToProtection(protection) +
      (
        isStatic(protection) ? " static"
          : isAbstract(protection) ? " abstract" : ""
        ) +
        (isFinal(protection) ? " final" : "")
        ;
  }

  public static String methodModifiers(int protection) {
    return
      modifierToProtection(protection) +
      (
        isStatic(protection) ? " static"
        : isAbstract(protection) ? " abstract" : ""
      ) +
      (isFinal(protection) ? " final" : "") +
      (isSynchronized(protection) ? " synchronized" : "") +
      (isNative(protection) ? " native" : "") +
      (isStrict(protection) ? " strictfp" : "")
      ;
  }

  public static String fieldModifiers(int protection) {
    return
      modifierToProtection(protection) +
      (
        isStatic(protection) ? " static" : "") +
        (isFinal(protection) ? " final" :
          isVolatile(protection) ? " volatile" : "") +
        (isTransient(protection) ? " transient" : "")
        ;
  }
  /**
   * Send in com.pkg.Clazz$InnerClass
   * or com/pkg/Clazz$InnerClass
   * Get back Pair<"com.pkg", "Clazz.InnerClass"
   * @param qualifiedBinaryName - The cls.getCanonicalName, or cls.getQualifiedBinaryName
   * @return - A pair of source names ('.' delimited), [pac.kage, Enclosing.Name]
   */
  public static IsType binaryToSource(String qualifiedBinaryName) {
    int arrDepth = 0;
    while(qualifiedBinaryName.charAt(0) == '[') {
      arrDepth++;
      qualifiedBinaryName = qualifiedBinaryName.substring(1);
    }
    qualifiedBinaryName = qualifiedBinaryName.replace('/', '.');
    int lastPkg = qualifiedBinaryName.lastIndexOf('.');
    String pkg;
    if (lastPkg == -1) {
      pkg = "";
    } else {
      pkg = qualifiedBinaryName.substring(0, lastPkg);
      assert pkg.equals(pkg.toLowerCase()) :
        "Either you are using an uppercase letter in your package name (stop that!)\n" +
        "or you are sending an inner class using period encoding instead of $ (also stop that!)\n" +
        "You sent "+qualifiedBinaryName+"; expected com.package.OuterClass$InnerClass";
    }
    String enclosed = toEnclosingType(qualifiedBinaryName.substring(lastPkg+1));
    return toType(pkg, addArrayBrackets(enclosed, arrDepth));
  }

  public static String toEnclosingType(String clsNoPackage) {
    return clsNoPackage.replace('$', '.');
  }
  public static String addArrayBrackets(String addTo, int arrDepth) {
    if (arrDepth<1)return addTo;
    StringBuilder b = new StringBuilder(addTo);
    while(arrDepth-->0)b.append("[]");
    return b.toString();
  }
  public static String stripJarName(String loc) {
    int ind = loc.indexOf("jar!");
    if (ind == -1)
      return loc;
    return stripFileName(loc.substring(0, ind+3));
  }
  public static String stripFileName(String loc) {
    return loc.startsWith("file:") ?  loc.substring(5) : loc;
  }
  public static String primitiveToObject(String datatype) {
    if ("int".equals(datatype))
      return "Integer";
    if ("char".equals(datatype))
      return "Character";
    return Character.toUpperCase(datatype.charAt(0)) + datatype.substring(1);
  }



}
