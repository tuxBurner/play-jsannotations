package com.github.tuxBurner.jsAnnotations;


import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.util.CollectionUtils;

import play.Environment;
import play.Routes;

import play.api.Play;
import play.api.routing.JavaScriptReverseRoute;


import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.JavaScript;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by tuxburner on 08.02.16.
 */
@Singleton
public class JsRoutesComponentImpl implements JsRoutesComponent {


    /**
     * The class where the javascript routes are generated at.
     */
    private static String JSROUTES_CLASS_NAME = "controllers.routes$javascript";

    /**
     * We need the environment for class loading.
     */
    private final Environment environment;

    /**
     * The js routes which match the annotation
     */
    private Set<JavaScriptReverseRoute> jsRoutes = new HashSet<>();

    @Inject
    public JsRoutesComponentImpl(Environment environment) {
        this.environment = environment;
        onStart();
    }

    /**
     * This initializes the module implementation.
     */
    private void onStart() {
        // Get the classloader of the current running app
        final ClassLoader classLoader = environment.classLoader();
        Class<?> staticJsClass;
        try {
            staticJsClass = Class.forName(JSROUTES_CLASS_NAME, true, classLoader);
        } catch (ClassNotFoundException e) {
            if (JsRoutesModule.LOGGER.isErrorEnabled()) {
                JsRoutesModule.LOGGER.error("An error happened while loading class: " + JSROUTES_CLASS_NAME, e);
            }
            return;
        }

        final Set<Method> jsRouteAnnotatedMethods = getJsRouteAnnotatedMethods();

        if (CollectionUtils.isEmpty(jsRouteAnnotatedMethods) == true) {
            if (JsRoutesModule.LOGGER.isDebugEnabled() == true) {
                JsRoutesModule.LOGGER.debug("Found no cotroller method annotated with: " + JSRoute.class.getCanonicalName());
            }
            return;
        }

        final Set<String> reverseNameForAnnotated = buildReversedNamesFromAnnotated(jsRouteAnnotatedMethods);
        final Set<JavaScriptReverseRoute> jsRoutesForMethods = findJsRoutesForMethods(staticJsClass, reverseNameForAnnotated);

        jsRoutes.addAll(jsRoutesForMethods);
    }


    /**
     * Finds all JSRoutes which are JS routes an annotated.
     *
     * @param staticJsClass           the class where the js routes mehtods are hold at
     * @param reverseNameForAnnotated the names of the annotated methods as Reserve methods.
     * @return a set of routes which match.
     */
    private Set<JavaScriptReverseRoute> findJsRoutesForMethods(final Class<?> staticJsClass, final Set<String> reverseNameForAnnotated) {
        Set<JavaScriptReverseRoute> reverseRoutes = new HashSet<JavaScriptReverseRoute>();
        for (Field staticJsField : staticJsClass.getFields()) {
            // get its methodsWithJsRoutes
            Set<Method> allMethods = ReflectionUtils.getAllMethods(staticJsField.getType(), ReflectionUtils.withReturnType(JavaScriptReverseRoute.class));
            for (Method method : allMethods) {
                // for each method, add its result to the reverseRoutes
                try {

                    final String classMethodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
                    if (JsRoutesModule.LOGGER.isDebugEnabled() == true) {
                        JsRoutesModule.LOGGER.debug("Checking if: " + classMethodName + " is in the annotated set.");
                    }

                    if (reverseNameForAnnotated.contains(classMethodName) == false) {
                        if (JsRoutesModule.LOGGER.isDebugEnabled() == true) {
                            JsRoutesModule.LOGGER.debug(classMethodName + " is NOT in the annotated set.");
                        }
                        continue;
                    }

                    if (JsRoutesModule.LOGGER.isDebugEnabled() == true) {
                        JsRoutesModule.LOGGER.debug("Invoking method: " + method.getDeclaringClass().getCanonicalName() + "." + method.getName());
                    }

                    JavaScriptReverseRoute invoke = (JavaScriptReverseRoute) method.invoke(staticJsField.get(null));
                    reverseRoutes.add(invoke);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    if (JsRoutesModule.LOGGER.isErrorEnabled() == true) {
                        JsRoutesModule.LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
        return reverseRoutes;
    }

    /**
     * Looks in the classpath for all the methods annotated with JSRoute
     *
     * @return a set of methods.
     */
    private Set<Method> getJsRouteAnnotatedMethods() {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder().setUrls(
                        ClasspathHelper.forPackage("controllers")).setScanners(
                        new MethodAnnotationsScanner()));
        Set<Method> methodsWithJsRoutes = reflections.getMethodsAnnotatedWith(JSRoute.class);


        if (JsRoutesModule.LOGGER.isDebugEnabled() == true) {
            JsRoutesModule.LOGGER.debug("Found: " + methodsWithJsRoutes.size() + " methodsWithJsRoutes annotated with " + JSRoute.class.getCanonicalName());
        }

        return methodsWithJsRoutes;
    }

    /**
     * Generates the ReverseRoute Names for the annotated methods.
     *
     * @param methodsWithJsRoutes the methods to check
     * @return a set of strings witht the names of the annotated methods.
     */
    private Set<String> buildReversedNamesFromAnnotated(Set<Method> methodsWithJsRoutes) {
        final Set<String> reverseNameForAnnotated = new HashSet<>();
        // check if it is an annotated one
        for (Method methodsWithJsRoute : methodsWithJsRoutes) {
            final String classMethodName = methodsWithJsRoute.getDeclaringClass().getSimpleName() + "." + methodsWithJsRoute.getName();
            final String reverseClassMethodName = "Reverse" + classMethodName;
            if (JsRoutesModule.LOGGER.isDebugEnabled() == true) {
                JsRoutesModule.LOGGER.debug("Translate" + classMethodName + " to -> " + reverseClassMethodName);
            }
            reverseNameForAnnotated.add(reverseClassMethodName);
        }

        return reverseNameForAnnotated;
    }

    public Result getJsRoutesResult() {
        if (jsRoutes != null && jsRoutes.isEmpty() == false) {
            Controller.response().setContentType("text/javascript");
            return Results.ok(getJSContent(play.mvc.Http.Context.current().request().host()));
        } else {
            return Results.internalServerError("No jsroutes found in the Plugin: " + JsRoutesComponentImpl.class.getCanonicalName());
        }
    }

    public String getJSContent(String host) {
        JavaScript jsRoutesJs = Routes.javascriptRouter("jsRoutes",
                jsRoutes.toArray(new JavaScriptReverseRoute[jsRoutes.size()]));

        return jsRoutesJs.text();

    }

    public Set<JavaScriptReverseRoute> getJsRoutes() {
        return jsRoutes;
    }
}
