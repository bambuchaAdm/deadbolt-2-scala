package be.objectify.deadbolt.scala.test.security

import be.objectify.deadbolt.scala.{DeadboltHandler, DynamicResourceHandler}
import play.api.Logger
import play.api.mvc.Request

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
class CompositeDynamicResourceHandler(delegates: Map[String, DynamicResourceHandler]) extends DynamicResourceHandler {

  val logger: Logger = Logger(this.getClass)

  override def checkPermission[A](permissionValue: String,
                                  deadboltHandler: DeadboltHandler,
                                  request: Request[A]): Future[Boolean] = {
    deadboltHandler.getSubject(request)
    .map {
      case Some(subject) =>
        subject.getPermissions.toList.exists(p => p.getValue.contains("zombie"))
      case None => false
    }
  }

  override def isAllowed[A](name: String,
                            meta: String,
                            deadboltHandler: DeadboltHandler,
                            request: Request[A]): Future[Boolean] =
    delegates.get(name) match {
      case Some(handler) => handler.isAllowed(name,
                                               meta,
                                               deadboltHandler,
                                               request)
      case None =>
        logger.error(s"No DynamicResourceHandler found for key [$name], denying access")
        Future(false)
    }
}