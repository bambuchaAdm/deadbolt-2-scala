/*
 * Copyright 2012-2016 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.scala

import javax.inject.{Inject, Provider, Singleton}

import play.api.Logger
import play.api.inject.Injector

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
 * Specifies the execution context used by Deadbolt.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
trait ExecutionContextProvider extends Provider[ExecutionContext]

/**
 * An implementation of [[ExecutionContextProvider]] that checks the injector for an instance of
 * [[DeadboltExecutionContextProvider]].  Falls back to [[scala.concurrent.ExecutionContext.global]] if
 * nothing is specified.
 */
@Singleton
class DefaultExecutionContextProvider @Inject() (injector: Injector) extends ExecutionContextProvider {

  val logger: Logger = Logger("deadbolt.execution-context")

  private val ecProvider: ExecutionContext = Try(injector.instanceOf[DeadboltExecutionContextProvider]) match {
    case Success(provider) =>
      logger.info("Custom execution context found")
      provider.get()
    case Failure(ex) =>
      logger.warn("No custom execution context found, falling back to scala.concurrent.ExecutionContext.global", ex)
      scala.concurrent.ExecutionContext.global
  }

  override def get(): ExecutionContext = ecProvider
}

trait DeadboltExecutionContextProvider extends Provider[ExecutionContext]
