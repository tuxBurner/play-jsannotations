package com.github.tuxBurner.jsAnnotations;


import play.Logger;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

/**
 * The module which handles evrything.
 */
public class JsRoutesModule extends Module {

    /**
     * The logger of this module.
     */
    public static Logger.ALogger LOGGER = Logger.of(JsRoutesModule.class);

    public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
        return seq(
                bind(JsRoutesComponent.class).to(JsRoutesComponentImpl.class)
        );
    }
}
