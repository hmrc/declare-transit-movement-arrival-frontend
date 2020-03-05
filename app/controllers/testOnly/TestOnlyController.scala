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

import controllers.actions._
import javax.inject.Inject
import models.UserAnswers
import navigation.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class TestOnlyController @Inject()(override val messagesApi: MessagesApi,
                                   mongo: ReactiveMongoApi,
                                   navigator: Navigator,
                                   identify: IdentifierAction,
                                   val controllerComponents: MessagesControllerComponents,
                                   renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def dropMongoCollection: Action[AnyContent] = Action.async {
    implicit request =>
      val collection: Future[JSONCollection] = mongo.database.map(_.collection[JSONCollection]("user-answers"))

      collection.flatMap(
        _.drop(false) map {
          case true  => Ok("Dropped  'User-answers' Mongo collection")
          case false => Ok("collection does not exist or something gone wrong")
        }
      )
  }

  def removeMovement(mrn: String): Action[AnyContent] = Action.async {
    implicit request =>
      val collection: Future[JSONCollection] = mongo.database.map(_.collection[JSONCollection]("user-answers"))

      collection.flatMap(
        _.findAndRemove(Json.obj("_id" -> mrn)) map {
          _.value match {
            case Some(_) => Ok(s"transit movement removed for the mrn $mrn")
            case _       => Ok("record does not exists")
          }
        }
      )
  }
}
