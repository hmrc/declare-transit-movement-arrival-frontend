/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.testOnly

import javax.inject.Inject
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class TestOnlyController @Inject()(override val messagesApi: MessagesApi,
                                   mongo: ReactiveMongoApi,
                                   val controllerComponents: MessagesControllerComponents,
                                   renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController {

  def dropMongoCollection: Action[AnyContent] = Action.async {
    implicit request =>
      val collection: Future[JSONCollection] = mongo.database.map(_.collection[JSONCollection]("user-answers"))

      collection.flatMap(
        _.drop(failIfNotFound = false) map {
          case true  => Ok("Dropped  'User-answers' Mongo collection")
          case false => Ok("collection does not exist or something gone wrong")
        }
      )
  }
}
