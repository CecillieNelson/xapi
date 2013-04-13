# XApi - Cross Platform Java Service Framework

The core of the XApi library is a lightweight dependency injection system designed to bind java interfaces to implementation classes, either in singleton or instance scope.

The primary targets are web server and client applications built using [Google Web Toolkit](http://code.google.com/p/google-web-toolkit), though work is also being done on a multi-platform java client framework called [PlayN](http://code.google.com/p/playn).

Using this core tool, every other module is built up as a standalone service, designed to expose all functionality through interfaces which can be easily overridden.

Most of the implementation modules are not yet ready for release, but the core library has proven itself useful enough times to go public and put artifacts on maven central.

## Usage

Step Zero: Get the code

(maven users)
<dependency>
 <groupId>wetheinter.net</groupId>
 <artifactId>xapi-core</artifactId>
 <version>0.3</version> 
</dependency>

<!--for your gwt modules:-->
<dependency>
 <groupId>wetheinter.net</groupId>
 <artifactId>xapi-gwt</artifactId>
 <version>0.3</version> 
</dependency>

(anyone else)

git clone git://github.com/TheInternetParty/XApi.git

jre requires xapi-core.jar;
gwt requires xapi-core.jar and xapi-gwt.jar

Step One: Create a service api

interface Service {
  void doSomething();
}

Step Two: Create some implementations, with annotations

//you can pick one basic class as a default for all platforms 
@SingletonDefault(implFor=Service.class)
class BasicService implements Service {
  public void doSomething() {
    //do nothing
  }
}
//then, create service overrides in any package you want
@SingletonOverride(implFor=Service.class, platform=PlatformType.GwtAll)
class GwtService implements Service {
  public void doSomething() {
    GWT.create(OtherThing.class);
  }
}

Step Three: Use X_Inject class to get singletons or instances

Service service = X_Inject.singleton(Service.class);

That's it.

You now have a working copy of your service interface.

If you use @InstanceDefault and @InstanceOverride, you would call X_Inject.instance(Something.class) instead.  The only difference is .instance() is an object factory, and .singleton() is a service factory.

You don't have to call any initializer methods on anything,  
it will just work in gwt, android and plain jre runtimes.  
Our bleeding edge branch does contain a preload() method,  
which can be used to load services you'll need later in the background.

The jre runtime takes a page out of the java.util.ServiceLoader playbook, and uses named files in the META-INF/ folders to bind interfaces (or classes!) to implementations.  Default jre uses META-INF/instances/fully.qualified.class.Name or META-INF/singletons/f.q.c.N resource files, with the implementation class inside.  The android layer uses assets/wti/instances and assets/wti/singletons respectively.  This location is set by a System property, and there is a generator to build these files for you automatically.

The jre runtime also supports runtime injection using org.reflections, provided the jars are on your classpath, and you have not disabled runtime injection with its controlling System property (see wetheinter.net.util.X_Properties for details).  Once the jre hotswapping module is ready, you will be able to inherit xapi-jre, and have org.reflections inherited automatically by maven.  Hotswapping will require control over class loaders, and may not be available on all platforms, thus, the default implementation class merely logs the lack of support to the console and quits.

It's also worth mentioning that the gwt version is kind enough to not ruin code splitting!  Our first prototype worked with just a plain gwt generator, but it had to access every service implementation class in one method (using class literals in switch statements, which is legal in jsni!); unfortunately this completely destroys any chance of code splitting.  Our current iteration uses "magic method injection" to turn each call to X_Inject.singleton() or .instance() into a new, generated method, thus preventing any form of cross-referencing and keeping the code splitter happy.  There are also auxiallary singleton provider methods which accept either a callback object, or a callback class literal, and they produce GWT.runAsync() boilerplate for you.  When you use callback classes, they are also injectable, so you can easily inject the service and the callback together, and do so safely in multiple locations without ever running the callback twice.  Finally, an injected callback also defers the instantiation of the callback until inside the new async block, thus preventing any leaked classes or fields used in your callback.


## Roadmap

In the labs section (not public yet), there is a concurrency service to make threading and parallel execution of processes seamless across any java-compatible runtime you wish to execute in.  The final goal is to declare any complex process with methods annotated as atomic units of work, with fan-in and fan-out boilerplate for parallelizable and out-of-order sections generated automatically.  This section is most relevant to Appengine clients who know how much work it can take to atomize a process and connect many-to-one and one-to-many processes. 

There is also a collections framework designed to expose simple map-like, set-like interfaces which can be bound to the most efficient collection type for a given runtime.  The primary purpose of this api is for GWT applications to use native jso collections in place of java.util, and to expose collections with concurrency overhead only in concurrent environments.

Another work in progress is a PlayN framework being used to expose a gui library designed to make cross-platform, declarative ui "Just work". It uses platform-specific code generators to read in a common ui text format, and provide implementation classes across the full gamut of client devices. Well, everything except Windows Phone (PlayN supports Android, iOS, Web / Gwt, Flash and native Java).

There is also an appengine emulation module currently at 85% which exposes appengine datastore keys and entities directly into GWT clients.  It also contains an asynchronous entity streaming service for running batches of write, read, delete and queries using the low-level asynchronous api, (optionally) with multiple threads handling callbacks at once.   When the modules it is blocked on are complete (namely the concurrency module), it will be used to expose low-level *asynchronous only* appengine datastore service to gwt clients (eventually others like UrlFetch as well).  Since the apis are asynchronous, it's no problem to make the client service serialize and send http services, while the server performs the actual work (it also makes having one server talk to another server very easy, by extending the client implementation to run on the server).  Being able to DI your own authentication layer into such services will simply be a must!

Finally, the last big module on the roadmap is [collIDE](http://collide.googlecode.com), which was build and open sourced at Google.  It is a real-time web IDE for editting source files in the browser.  We have used our dependency injection system to create a plugin system for integrating arbitrary functionality into collIDE, and are actively using a very experimental copy (linux only!) for internal development.  Most of our build process is done through a web gui that is embeddable into any GWT project.  For collIDE integration, we have installed a gwt super dev mode compiler and a maven-build-runner, and created a second collIDE runtime which bootstraps into a tiny floating menu, allowing you to add it to other gwt apps, and access the full XApi toolchain in the browser tab you are developing.


## GWT Tools

For Google Web Toolkit XApi enables reflection support, more complete emulation of java.lang.Class, a range of code generation utilities, GWT.runAsync() injection, and a little something called "magic-method injection".

In order to make pluggable dependency injection work in GWT, we hacked the GWT UnifyAST phase (at the beginning of production compiles, where java source is turned into an Abstract Syntax Tree) to allow the writing of custom "GWT.create()-like methods".  Using a custom configuration property in a .gwt.xml file, we map any static method in the app to a generator, which swaps out the method call with any combination of generated source files or programmatically assembled AST node objects.  The process was modelled on GWT.create(), but expands to allow you to send an arbitrary number of arguments, which you can then reference in your generated code.

There are two caveats with magic method injection.

The first is that, like GWT, if you want to know the class of a parameter at compile-time, you must send a class literal, and not a Class object.  There is a fix in the mix for using one magic method to record all classes passed in, to let the compiler know which types you want to support, and generate a provider class which can operate on runtime class objects.

The second is that whatever code is in the body of the method you replaced will be getting called during gwt dev and jre runtimes.  Even though it's not actually being called in gwt, and will get pruned, it may not contain any jre classes not included in your gwt module or methods on the gwt whitelist.  This is why our injection service allows you to cleanly separate source paths; a jre injected service can access whatever it wants, and the gwt module will never need to know.  We went through the trouble to make our JreInjector gwt-compatible so our other services wouldn't have to care about gwt dependencies.  

There may be a fix for requiring gwt compatible code in the method bodies replaced by injection if we can move the magic method injection from UnifyAST to GwtAstBuilder.  The only trouble here is that we would be forced to work entirely on eclipse JDT AST instead of the more feature rich gwt ast; waiting for the UnifyAST stage is also handy because it gives us access to the JProgram and TypeOracle and RebindPermutationOracle needed to call into standard gwt generators.  By tying into the GWT generator framework, we can allow the replaced method bodies to just call GWT.create() and access the exact same code generated for injected methods, or to do java-esque stuff like reflection, to mask a non-gwt compatible dependency.

Beyond the dependency injection system, xapi-gwt also used to give fuller (and eventually complete) emulation for reflection and java.lang.Class.  Current support for reflection is only the Constructor, Method and Field methods, and provides full functionality for invoking methods, but only partial metadata.  Our internal purpose for supporting reflection is for the collIDE project.  If we can are running a web IDE on an arbitrary source file, and your build files are all there, then there's no reason we can't just recompile your file, and send back metadata, warnings or errors (most likely through a long-lived maven plugin).  There is also talk of implementing java 7 method handles in GWT using the same techniques as used for Constructor and Method emulation.

Future enhancements to the gwt branch of the project will be geared around more dynamic code generation, with a functional-programming style methodology of building ast nodes (or source code), as well as a project to make services in a gwt app hot-swappable, for extra-fast compile-time development; why recompile the whole app to change one piece?  In order to prepare for fully hotswappable code in the client and the server, all dynamic services are designed around static access to a provider object.  Change the provider object, never keep a reference to the returned object, and now all code which uses the functionality update automatically.  Throw in an event listener and an api for transferring live state, drop it into the collIDE real-time collaborative web app editor, and turn the devs loose!

Between source maps support, being able to reflectively visualize and interact with your live object graph, and the ability to edit and recompile that source code, writing web apps will become far more tactile and interactive than ever before!

## Codegen Tools

The initial investment in code generation is generally higher than its immediate payouy.  However, once you have a powerful set of code generation tools, you are able to work almost entirely in the interface and annotation layer, where you can achieve things like multiple inheritance (provided your generators play nicely together), boilerplate-free code, extensive testing without touching public code, complete dereferencing of unrelated dependencies, and the ability to write highly optimizable private final code; if someone wants to add functionality, they can inject their own generator or runtime service and build whatever they please. 

All that matters is the API.

The code that produces functionality can be hardcoded or dynamic.  In many cases, it's more efficient to just write the code and be done with it.  But, when you find yourself writing the same thing more than three times, it's time to ask yourself if you should keep inventing the wheel, or just automate the process of generating the wheel from its interface.

This is where it comes down to opportunity cost.  Do you bother with a generator, or just keep writing it by hand (and updating it by hand, in every location!)?  Well, if the generation process was dead simply to plug in, and looked a lot like a well structured java document in the first place, then the investment cost is much lower, and you gain the ability to add features to multiple implementations by changing one or two files, instead of ten or twenty or a hundred.

Code itself is an evolving thing; even if you don't plan to introduce breaking changes, your environment and your need for more features pretty much makes changes inevitable.  When you code in the interface + annotation + generator layer, you gain the ability to offer legacy implementations for legacy clients, and bleeding edge features for the more web savvy clients.

When you create a new pojo, do you really want to write fields and getters and setters and serializers and validators and events by hand every time?  Isn't it much nicer to say, "Ah, I need an Address object with fields for Street, State, Zip... And just annotate your interface with serialization and validation options"?  When you've written one thousand and one field serializers, will you think then about automation?  Ya, there might be runtime solutions to do that serialization for you, but unless they are preprocessing your classes, they're doing computations at runtime that could have been done at compile time, and you're losing out on performance.

## Unify Your Codebase

Many web applications suffer higher development costs from the separation of client and server than perhaps any other hurdle to web development (except maybe the thankfully deceased IE6).  After having worked in a number of enterprise environments on cloud computing web application, it became clear that a light weight server which can offload most of the work to the client was the way to go.  Keep the state on the client, and let the server just respond to requests as quickly as it mechanically can.

When there's a code-sharing barrier, and your client can't touch stuff on the server, and you don't want to pollute your server classloader with client dependencies, you run into the inevitable "I want to use this code over there, but I can't" routine.  And that routine gets old fast.

So, that is why we model all our services through an interface that operates only on other interfaces.  That way, if your business logic delves into sql, concurrency or server-only code, it doesn't block the client from implementing those same interfaces that just route through http requests instead of performing the actual action.

Any functionality your server is exposing will be used in one way or another by the client.  You can try to keep them separate if you want, but your *server* is just exposing *services*, so why try to fight it?  Make your services asynchronous; the client must be async, and the server *should* be async as well.  If you have multiple threads available and can shard one big task into ten small async tasks, your server will appear to go ten times faster to the client.  Obviously not all services need to be async, but any time any client or thread or actor in your application is sending off work to some other processor, do your clock cycles a favor and avoid busy-wait like the plague!

Best of all, supporting multiple runtime platforms when everything is an injected implementation of an interface is that your code is very, very portable.  If your business logic doesn't touch the concrete datastore or cache service it depends on, you can write (or generate!) a second implementation in another environment, and reuse as much code as there is overlap in the runtimes.

The XApi is still in its developmental infancy, but it is being built with limitless future extensibility in mind.  Runtimes with full support include java apps, android, all major web browsers via gwt, appengine, vert.x (in collIDE), and standard servlet containers.  The core depends on nothing, and every module being completed is designed to be as self-contained as possible; each major service is built as a maven module to avoid leaking dependencies, and to maximize the ability for compilers and pre-processors to optimize and prune as much as possible.


