package xapi.dev.model;

import java.lang.reflect.Method;
import java.util.Arrays;

import xapi.annotation.inject.PlatformType;
import xapi.annotation.model.FieldName;
import xapi.annotation.model.Key;
import xapi.annotation.model.Persistent;
import xapi.annotation.model.Serializable;
import xapi.dev.exceptions.GeneratorFailedException;
import xapi.dev.source.ClassBuffer;
import xapi.dev.source.MethodBuffer;
import xapi.dev.source.SourceBuilder;
import xapi.log.api.LogLevel;
import xapi.log.api.LogService;
import xapi.model.api.Model;
import xapi.model.impl.AbstractModel;


public class ModelGenerator {

  @SuppressWarnings({"unchecked", "unused"})
  public static void generateModelClass(LogService logger, Class<?> modelSource,
    ModelGeneratorContext modelContext) {
    PlatformType platform = modelContext.getPlatform();
    if (!modelSource.isInterface())
      throw new GeneratorFailedException("Tried to get a model that was not an interface.  "
        + "Please do not request concrete classes from X_Model; we must injust our own subclasses manually.");

    final Class<? extends Model> baseClass;
    switch (platform) {
    case GwtAll:
    case GwtDev:
    case GwtScript:
      try {
        baseClass = Class.class.cast(Class.forName("xapi.model.gwt.ModelGwt"));
      } catch (Exception e) {
        logger.log(LogLevel.ERROR, "Unable to find xapi.model.gwt.ModelGwt on " +
        		"classpath; ensure you are inheriting xapi-gwt-model.");
        throw new RuntimeException(e);
      }
      break;
    case All:
    case Android:
    case Appengine:
    case Desktop:
    case Flash:
    case Ios:
    case Java:
    case WebApp:
    default:
      baseClass = AbstractModel.class;
    }
    // reflect upon the model source we need to generate.
    Method[] methods = modelSource.getMethods();
    if (logger.shouldLog(LogLevel.TRACE)) {
      logger.log(
        LogLevel.TRACE,
        "Generating model for interface " + modelSource.getCanonicalName() + " on platform " +
          platform.name());
      for (Method method : methods) {
        logger.log(LogLevel.TRACE, Arrays.asList(method.getAnnotations()) + ": " + method.toGenericString());
      }
    }

    // first, collect up our metadata
    Persistent classPersistence = modelSource.getAnnotation(Persistent.class);
    Serializable classSerializable = modelSource.getAnnotation(Serializable.class);

    logger.log(LogLevel.TRACE, "Class Peristence Level: " + classPersistence);
    logger.log(LogLevel.TRACE, "Default Serialization Level: " + classSerializable);

    SourceBuilder<ModelGeneratorContext> ctx = new SourceBuilder<ModelGeneratorContext>();
    ClassBuffer cls = new ClassBuffer(ctx);
    cls.setSuperClass(baseClass.getCanonicalName());

    MethodBuffer deserialize = new MethodBuffer(ctx);
    MethodBuffer equals = new MethodBuffer(ctx);
    MethodBuffer hashCode = new MethodBuffer(ctx);
    MethodBuffer load = new MethodBuffer(ctx);
    MethodBuffer persist = new MethodBuffer(ctx);
    MethodBuffer serialize = new MethodBuffer(ctx);
    MethodBuffer toString = new MethodBuffer(ctx);
    for (Method method : methods) {
      Persistent methodPersistence = method.getAnnotation(Persistent.class);
      Serializable methodSerializable = method.getAnnotation(Serializable.class);
      Key methodIsKey = method.getAnnotation(Key.class);
      FieldName methodName = method.getAnnotation(FieldName.class);

      if (methodPersistence == null) methodPersistence = classPersistence;
      if (methodSerializable == null) methodSerializable = classSerializable;

      // okay, we've got our metadata. Now, generate the class.
      if (methodPersistence != null) {

      }
      if (methodSerializable != null) {
        if (modelContext.isServer() && methodSerializable.serverToClient()) {

        }
        if (modelContext.isClient() && methodSerializable.clientToServer()) {

        }
      }
    }
  }

}
