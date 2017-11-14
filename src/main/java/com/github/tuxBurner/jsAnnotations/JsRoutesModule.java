package com.github.tuxBurner.jsAnnotations;


import com.google.inject.AbstractModule;
import play.Logger;

/**
 * The module which handles everything.
 */
public class JsRoutesModule extends AbstractModule
{

    /**
     * The logger of this module.
     */
    public static Logger.ALogger LOGGER = Logger.of(JsRoutesModule.class);


    @Override
    protected void configure() {
        bind(JsRoutesComponent.class).to(JsRoutesComponentImpl.class);
    }
}
