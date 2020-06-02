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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRetrievalActionProvider, IdentifierAction}
import models.{ArrivalId, MovementReferenceNumber, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.ArrivalNotificationMessageService
import uk.gov.hmrc.http.HttpErrorFunctions
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.CheckYourAnswersHelper
import viewModels.sections.Section

import scala.concurrent.ExecutionContext
import scala.xml.{Elem, Node, Text}
import scala.xml.transform.{RewriteRule, RuleTransformer}

class CheckYourAnswersRejectionsController @Inject()(override val messagesApi: MessagesApi,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalActionProvider,
                                                     arrivalNotificationMessageService: ArrivalNotificationMessageService,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with HttpErrorFunctions {

  def onPageLoad(mrn: MovementReferenceNumber, arrivalId: ArrivalId): Action[AnyContent] = (identify andThen getData(mrn)).async {
    implicit request =>
      val helper = new CheckYourAnswersHelper(UserAnswers(mrn))

      val json = Json.obj(
        "sections" -> Json.toJson(Seq(Section(Seq(helper.movementReferenceNumber))))
      )
      renderer.render("check-your-answers-rejections.njk", json).map(Ok(_))
  }

  object RuleFactory {

    def createRuleTransformer(key: String, value: String): RuleTransformer =
      new RuleTransformer(new RewriteRule {
        override def transform(n: Node): Seq[Node] = n match {
          case elem @ Elem(prefix, label, attributes, scope, _) if elem.label.equalsIgnoreCase(key) =>
            Elem(prefix, label, attributes, scope, false, Text(value))
          case other => other
        }
      })

  }

  def onPost(mrn: MovementReferenceNumber, arrivalId: ArrivalId): Action[AnyContent] = (identify andThen getData(mrn)).async {
    implicit request =>
      arrivalNotificationMessageService.getArrivalNotificationMessage(arrivalId) map {
        case Some((xml, _)) =>
          val xmlTransformer   = RuleFactory.createRuleTransformer("DocNumHEA5", mrn.toString)
          val updatedXml: Node = xmlTransformer(xml.head)
          println(updatedXml)

          Redirect(routes.TechnicalDifficultiesController.onPageLoad())
      }

  }

}
