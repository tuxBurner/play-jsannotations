package jsAnnotations;


import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import play.Application;
import play.Logger;
import play.Plugin;
import play.Routes;
import play.api.Play;
import play.core.Router;
import play.libs.Scala;
import play.mvc.Controller;
import play.mvc.Results;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This plugin collects all JSroute annotatet controllers and adds them to a set with the corresponding JavascriptReverseRoute
 * User: tuxburner
 */
public class JSRoutesPlugin extends Plugin {


    public static Set<Router.JavascriptReverseRoute> jsRoutes = new HashSet<Router.JavascriptReverseRoute>();

    private static String JSROUTES_CLASS_NAME = "controllers.routes$javascript";


    public JSRoutesPlugin(final Application application) {

    }


    @Override
    public void onStart() {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().setUrls(
                        ClasspathHelper.forPackage("controllers")).setScanners(
                        new MethodAnnotationsScanner()));
        Set<Method> methods = reflections.getMethodsAnnotatedWith(JSRoute.class);

        if (methods != null && methods.size() > 0) {
            if (Logger.isDebugEnabled() == true) {
                Logger.debug("Found: " + methods.size() + " methods annotated with " + JSRoute.class.getCanonicalName());
            }

            // Get the classloader of the current running app
            final ClassLoader classLoader = Play.classloader(Play.current());
            Class<?> staticJsClass = null;
            try {
                staticJsClass = Class.forName(JSROUTES_CLASS_NAME, true, classLoader);
            } catch (ClassNotFoundException e) {
                if (Logger.isErrorEnabled()) {
                    Logger.error("An error happened while loading class: " + JSROUTES_CLASS_NAME, e);
                }
                return;
            }


            final Map<Class, Object> ctrlInstanceCache = new HashMap<Class, Object>();
            for (final Method method : methods) {

                final String controllerClassName = method.getDeclaringClass().getSimpleName();
                // if the classname ends with a $ it cannot be used
                if (controllerClassName.endsWith("$")) {
                    if (Logger.isDebugEnabled() == true) {
                        Logger.debug("Controller class name " + controllerClassName + " ends with a $ skipping it");
                    }
                    continue;
                }

                try {
                    final Field staticJsField = staticJsClass.getField(controllerClassName);
                    if (Logger.isDebugEnabled() == true) {
                        Logger.debug("Found static field in for js routing.");
                    }
                    final Class<?> ctrlRouteClass = staticJsField.getType();
                    if (ctrlInstanceCache.containsKey(ctrlRouteClass) == false) {
                        final Object ctrlInstance = ctrlRouteClass.newInstance();
                        ctrlInstanceCache.put(ctrlRouteClass, ctrlInstance);
                    }
                    // the method in the reversrouting which returns the jsreverseroute
                    final Method jsRouteMethod = ctrlRouteClass.getMethod(method.getName());
                    final Object jsReverseRoute = jsRouteMethod.invoke(ctrlInstanceCache.get(ctrlRouteClass));

                    if (jsReverseRoute instanceof Router.JavascriptReverseRoute == true) {
                        if (Logger.isDebugEnabled() == true) {
                            Logger.debug("Adding " + method.getName() + " in class: " + controllerClassName + " to the jsRoutes");
                        }
                        jsRoutes.add((Router.JavascriptReverseRoute) jsReverseRoute);
                    }

                } catch (Exception e) {
                    if (Logger.isErrorEnabled() == true) {
                        Logger.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Creates the Result which can be used in a Controller
     *
     * @return
     */
    public static Results.Status getJsRoutesResult() {
        if (JSRoutesPlugin.jsRoutes != null && JSRoutesPlugin.jsRoutes.isEmpty() == false) {
            Controller.response().setContentType("text/javascript");
            return Results.ok(getJSContent(play.mvc.Http.Context.current().request().host()));
        } else {
            return Results.internalServerError("No jsroutes found in the Plugin: " + JSRoutesPlugin.class.getCanonicalName());
        }
    }

    public static String getJSContent(String host) {
        return play.api.Routes.javascriptRouter("jsRoutes", Scala.Option("jQuery.ajax"), host, Scala.toSeq(JSRoutesPlugin.jsRoutes.toArray(new Router.JavascriptReverseRoute[JSRoutesPlugin.jsRoutes.size()]))).text();
        //return Routes.javascriptRouter("jsRoutes", JSRoutesPlugin.jsRoutes.toArray(new Router.JavascriptReverseRoute[JSRoutesPlugin.jsRoutes.size()]));
    }


}
