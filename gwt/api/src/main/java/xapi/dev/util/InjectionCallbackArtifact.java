package xapi.dev.util;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.gwt.core.ext.linker.Artifact;
import com.google.gwt.core.ext.linker.Transferable;
import com.google.gwt.core.ext.linker.impl.StandardLinkerContext;

import static xapi.dev.util.InjectionUtils.generatedAsyncProviderName;
import static xapi.dev.util.InjectionUtils.toSourceName;
import static xapi.dev.util.InjectionUtils.toUniqueName;

@Transferable
public class InjectionCallbackArtifact extends Artifact<InjectionCallbackArtifact>{
  private static final long serialVersionUID = -6712802856568810532L;
  
  private String boundTarget;
  private final String canonicalName;
  private final String className;
  private final String generatedName;
  private final String implementationPackage;
  private final String packageName;
  private final String simpleName;
  
  private final Set<String> callbacks = new LinkedHashSet<String>();

  public InjectionCallbackArtifact(@Nonnull String packageName, @Nonnull String className) {
    super(StandardLinkerContext.class);
    this.packageName = packageName;
    //TODO: allow package crunching
    this.implementationPackage = packageName+".impl";
    this.className = toSourceName(className.replace(packageName+".", ""));
    this.canonicalName = packageName+"."+this.className;
    this.generatedName = toUniqueName(this.className);
    int lastPeriod = this.className.lastIndexOf('.');
    if (lastPeriod==-1){
      this.simpleName = this.className;
    }else{
      this.simpleName = this.className.substring(lastPeriod+1);
    }
    this.boundTarget = canonicalName;//default is bind to self
  }

  @Override
  public int hashCode() {
    return canonicalName.hashCode();
  }

  @Override
  protected int compareToComparableArtifact(InjectionCallbackArtifact o) {
    return canonicalName.compareTo(canonicalName);
  }


  @Override
  protected Class<InjectionCallbackArtifact> getComparableArtifactType() {
    return InjectionCallbackArtifact.class;
  }

  public void addCallback(String packageName, String className) {
    callbacks.add(packageName+"."+className);
  }
  public void addCallback(String qualifiedClassName) {
    callbacks.add(qualifiedClassName);
  }
  @Override
  public String toString() {
    return canonicalName+" -> "+callbacks;
  }

  public Iterable<String> getCallbacks() {
    return callbacks;
  }

  /**
   * @return the generatedName (simple name with enclosing types prefixed)
   */
  public String getGeneratedName() {
    return generatedName;
  }
  /**
   * @return the canonicalName (qualified java source name)
   */
  public String getCanonicalName() {
    return canonicalName;
  }

  /**
   * @return the implementationPackage
   */
  public String getImplementationPackage() {
    return implementationPackage;
  }

  /**
   * @return the simpleName
   */
  public String getSimpleName() {
    return simpleName;
  }

  /**
   * @return the packageName
   */
  public String getPackageName() {
    return packageName;
  }

  /**
   * @return true if this artifact has not been bound to any injections
   */
  public boolean isTargetUnbound() {
    return canonicalName.equals(boundTarget);
  }
  /**
   * @return The class being injected over top of this class
   * If this injection is unbound, this value will match the canonical name.
   */
  public String getBoundTarget(){
    return boundTarget;
  }

  /**
   * @param boundTarget the boundTarget to set
   */
  public void bindTo(String boundTarget) {
    this.boundTarget = boundTarget.replaceAll("[$]", ".");
  }

  public String getAsyncInjectionName() {
    return implementationPackage+"."+generatedAsyncProviderName(generatedName);
  }


}
