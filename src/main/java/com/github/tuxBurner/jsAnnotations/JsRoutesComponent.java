package com.github.tuxBurner.jsAnnotations;

import play.api.routing.JavaScriptReverseRoute;
import play.mvc.Result;

import java.util.Set;

/**
 * Interface for the js route component.
 */
public interface JsRoutesComponent {


    /**
     * Getter for the found JavaScriptReverseRoute which where annotated with JSRoute
     * @return
     */
    Set<JavaScriptReverseRoute> getJsRoutes();

    /**
     * Creates the Result which can be used in a Controller
     *
     * @return
     */
    Result getJsRoutesResult();

    /**
     * Returns the actual js content which represents the handling for the annotated routes.
     * @param host
     * @return
     */
    String getJSContent(String host);
}
