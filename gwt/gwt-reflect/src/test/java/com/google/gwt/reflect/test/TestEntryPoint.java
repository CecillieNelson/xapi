package com.google.gwt.reflect.test;

import static com.google.gwt.reflect.client.GwtReflect.magicClass;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.reflect.client.ConstPool;
import com.google.gwt.reflect.client.GwtReflect;
import com.google.gwt.reflect.client.MemberPool;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TestEntryPoint implements EntryPoint {

  private final Map<Method,Object> tests = new LinkedHashMap<Method,Object>();
  private final Map<Class<?>,Method[]> testClasses = new LinkedHashMap<Class<?>,Method[]>();

  @Override
  public void onModuleLoad() {
    String module = GWT.getModuleName(), host = GWT.getHostPageBaseURL().replace("/"+module, "");
    debug("<a href='#' onclick=\""
          + "window.__gwt_bookmarklet_params = "
            + "{server_url:'" + host+ "', "
            + "module_name:'" + module + "'}; "
          + "var s = document.createElement('script'); "
          + "s.src = 'http://localhost:1337/dev_mode_on.js'; "
          + "document.getElementsByTagName('head')[0].appendChild(s); "
          + "return true;"
        + "\">Recompile</a>", null);
    
    GWT.runAsync(TestEntryPoint.class, new RunAsyncCallback() {

      @Override
      public void onSuccess() {
        try {
          String.class.getMethod("equals", Object.class).invoke("!", "!");
        } catch (Exception e) {debug("Basic string reflection not working", e);
        }
        // Do not change the order of the following calls unless you also
        // update the initial-load-sequence defined in CompileSizeTest.gwt.xml
        loadTests(false);
        addArrayTests();
        addAnnotationTests();
        addConstructorTests();
        addMethodTests();
        addFieldTests();
        
        ConstPool.loadConstPool(new AsyncCallback<ConstPool>() {
          @Override
          public void onSuccess(ConstPool result) {
            for (MemberPool<?> m : result.getAllReflectionData()) {
              try {
                Class<?> c = m.getType();
                if (!testClasses.containsKey(c)) {
                  addTests(c);
                }
              } catch (Throwable e) {
                debug("Error adding tests", e);
              }
            }
            loadTests(true);
          }

          @Override
          public void onFailure(Throwable caught) {
            debug("Error loading ConstPool", caught);
          }
        });
        
        
      }

      @Override
      public void onFailure(Throwable reason) {
        debug("Error loading TestEntryPoint", reason);
      }
    });

  }

  protected void loadTests(final boolean forReal) {
    GWT.runAsync(JUnit4Test.class, new RunAsyncCallback() {
      
      @Override
      public void onSuccess() {
        if (forReal) {
          displayTests();
          runTests();
        }
      }
      
      @Override
      public void onFailure(Throwable reason) {
        
      }
    });
  }

  protected void addAnnotationTests() {
    GWT.runAsync(AnnotationTests.class, new RunAsyncCallback() {
      @Override
      public void onSuccess() {
        magicClass(AnnotationTests.class);
        try {
          addTests(AnnotationTests.class);
        } catch (Throwable e) {
          debug("Error adding AnnotationTests", e);
        }
      }
      
      @Override
      public void onFailure(Throwable reason) {
        debug("Error loading AnnotationTests", reason);
      }
    });
  }

  protected void addArrayTests() {
    GWT.runAsync(ArrayTests.class, new RunAsyncCallback() {
      @Override
      public void onSuccess() {
        magicClass(ArrayTests.class);
        try {
          addTests(ArrayTests.class);
        } catch (Throwable e) {
          debug("Error adding ArrayTests", e);
        }
      }
      
      @Override
      public void onFailure(Throwable reason) {
        debug("Error loading ArrayTests", reason);
      }
    });
  }

  protected void addConstructorTests() {
    GWT.runAsync(ConstructorTests.class, new RunAsyncCallback() {
      @Override
      public void onSuccess() {
        magicClass(ConstructorTests.class);
        try {
          addTests(ConstructorTests.class);
        } catch (Throwable e) {
          debug("Error adding ConstructorTests", e);
        }
      }
      
      @Override
      public void onFailure(Throwable reason) {
        debug("Error loading ConstructorTests", reason);
      }
    });
  }

  protected void addFieldTests() {
    GWT.runAsync(FieldTests.class, new RunAsyncCallback() {
      @Override
      public void onSuccess() {
        magicClass(FieldTests.class);
        try {
          addTests(FieldTests.class);
        } catch (Throwable e) {
          debug("Error adding FieldTests", e);
        }
      }
      
      @Override
      public void onFailure(Throwable reason) {
        debug("Error loading FieldTests", reason);
      }
    });
  }

  protected void addMethodTests() {
    GWT.runAsync(MethodTests.class, new RunAsyncCallback() {
      @Override
      public void onSuccess() {
        magicClass(MethodTests.class).getMethods();
        try {
          addTests(MethodTests.class);
        } catch (Throwable e) {
          debug("Error adding MethodTests", e);
        }
      }
      
      @Override
      public void onFailure(Throwable reason) {
        debug("Error loading MethodTests", reason);
      }
    });
  }

  private void addTests(Class<?> cls) throws Throwable {
    Method[] allTests = JUnit4Test.findTests(cls);
    if (allTests.length > 0) {
      testClasses.put(cls, allTests);
      Object inst = cls.newInstance();
      for (Method method : allTests) {
        tests.put(method, inst);
      }
    }
  }

  private void displayTests() {
    BodyElement body = Document.get().getBody();
    
    for (final Class<?> c : testClasses.keySet()) {
      DivElement div = Document.get().createDivElement();
      div.getStyle().setDisplay(Display.INLINE_BLOCK);
      div.getStyle().setVerticalAlign(VerticalAlign.TOP);
      div.getStyle().setMarginRight(2, Unit.EM);
      div.getStyle().setProperty("maxHeight", "400px");
      div.getStyle().setOverflowY(Overflow.AUTO);
      
      StringBuilder b = new StringBuilder();
      b
          .append("<h3>")
          .append(c.getName())
          .append("</h3>")
      ;
      try {
        String path = c.getProtectionDomain().getCodeSource().getLocation().getPath();
        b.append("<sup><a href='file://"+path+"'>")
        .append(path)
        .append("</a></sup>");
      } catch (Exception ignored) {}
      div.setInnerHTML(b.toString());
      for (final Method m : testClasses.get(c)) {
        final String id = m.getName()+c.hashCode();
        b = new StringBuilder();
        b.append("<pre>");
        b.append("<a href='javascript:'>");
        b.append(m.getName());
        b.append("</a>");
        b.append('(');
        b.append(GwtReflect.joinClasses(", ", m.getParameterTypes()));
        b.append(')');
        b.append("</pre>");
        b.append("<div id='"+id+"'> </div>");
        Element el = Document.get().createDivElement().cast();
        el.setInnerHTML(b.toString());
        DOM.setEventListener(el, new EventListener() {
          @Override
          public void onBrowserEvent(Event event) {
            if (event.getTypeInt() == Event.ONCLICK) {
              runTest(m);
            }
          }
        });
        DOM.sinkEvents(el, Event.ONCLICK);
        div.appendChild(el);
      }
      body.appendChild(div);
    }
    
  }

  private void runTests() {
    int delay = 1;
    for (final Method method : tests.keySet()) {
      new Timer() {

        @Override
        public void run() {
          runTest(method);
        }
      }.schedule(delay += 5);
    }
  }

  protected void runTest(Method m) {
    String id = m.getName()+m.getDeclaringClass().hashCode();
    com.google.gwt.dom.client.Element el = Document.get().getElementById(id);
    try {
      JUnit4Test.runTest(tests.get(m), m);
      debug(el, "<div style='color:green'>" + m.getName() + " passes!</div>", null);
    } catch (Throwable e) {
      String error = m.getDeclaringClass().getName() + "." + m.getName() + " failed";
      while (e.getClass() == RuntimeException.class && e.getCause() != null)
        e = e.getCause();
      debug(el, error, e);
      throw new AssertionError(error);
    }
  }

  private void debug(String string, Throwable e) {
    DivElement el = Document.get().createDivElement();
    debug(el, string, e);
    Document.get().getBody().appendChild(el);
  }
  
  private void debug(com.google.gwt.dom.client.Element el, String string, Throwable e) {
    StringBuilder b = new StringBuilder();
    b.append(string);
    b.append('\n');
    b.append("<pre style='color:red;'>");
    while (e != null) {
      b.append(e);
      b.append('\n');
      for (StackTraceElement trace : e.getStackTrace()) {
        b.append('\t')
          .append(trace.getClassName())
          .append('.')
          .append(trace.getMethodName())
          .append(' ')
          .append(trace.getFileName())
          .append(':')
          .append(trace.getLineNumber())
          .append('\n');
      }
      e = e.getCause();
    }
    b.append("</pre>");

    el.setInnerHTML(b.toString());
  }
}
