# Play JsAnnotations library

This is a simple Play 2.4 plugin, which allows you to expose the routes for javascript with an simple @JSRoute.
The advantage over the "normal" way in play is that you just to have to annotate the Result and it will be exposed as a javascript route.

## Installation (using sbt)

You will need to add the following resolver in your `project/Build.scala` file:

```scala
resolvers += "tuxburner.github.io" at "http://tuxburner.github.io/repo"
```

Add a dependency on the following artifact:

```scala
libraryDependencies += "com.github.tuxBurner" %% "play-jsannotations" % "2.4.0"
```

Activate the plugin in the `conf/application.conf` like this:

```
play.modules.enabled  += "com.github.tuxBurner.jsAnnotations.JsRoutesModule"
```


## Usage

Just annotate a Result like this:

```java
     @JSRoute
     public Result doSomething() {
        return ok("I did somethin");
     }
```

In your routes you need something like this to expose the routes to javascript:

```
  # Javascript routing
  GET     /assets/javascripts/routes   controllers.Application.jsRoutes()
```



In the Application controller add a method like this:
```java

  @Inject
  JsRoutesComponent jsRoutesComponent;

  /**
   * Register the routes to certain stuff to the javascript routing so we can
   * reach it better from there
   *
   * @return
   */
  public Result jsRoutes() {
   return jsRoutesComponent.getJsRoutesResult();
  }
```

Now you need to load the javascript routes into your  template

```html
  <script src="@routes.Application.jsRoutes" type="text/javascript"></script>
```

Finally you can call the route in javascript like this:

```javascript
  jsRoutes.controllers.SomeController.doSomething().ajax({
    data: {},
    success: function (data) {
      alert(data);
    },
    error: function (data) {
      alert('Error')
    }
  });
```
