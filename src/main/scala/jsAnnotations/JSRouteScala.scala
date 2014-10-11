package jsAnnotations

import play.api.mvc.{Controller, Action}

/**
 * Helper for getting the jsroutes into a controller
 */
object JSRouteScala extends Controller {

  def getJsRoutesResult = Action {
    implicit request =>
      if (JSRoutesPlugin.jsRoutes.isEmpty == true) {
        InternalServerError("No jsroutes found in the Plugin: " + classOf[JSRoutesPlugin].getCanonicalName)
      }
      Ok(JSRoutesPlugin.getJSContent(request.host)).as("text/javascript")
  }
}
