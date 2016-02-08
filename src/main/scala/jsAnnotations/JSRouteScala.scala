package jsAnnotations

import javax.inject.Inject

import play.api.mvc.{Controller, Action}

/**
 * Helper for getting the jsroutes into a controller
 */
class JSRouteScala @Inject()(jsRoutesComponent: JsRoutesComponent)  extends Controller {



  def getJsRoutesResult = Action {
    implicit request =>
      if (jsRoutesComponent.getJsRoutes.isEmpty == true) {
        InternalServerError("No jsroutes found in the Plugin: " + classOf[JsRoutesComponentImpl].getCanonicalName)
      }
      Ok(jsRoutesComponent.getJSContent(request.host)).as("text/javascript")
  }
}
