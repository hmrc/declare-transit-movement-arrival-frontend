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

import controllers.actions._
import javax.inject.Inject
import models.MovementReferenceNumber
import pages.PresentationOfficePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.viewmodels.NunjucksSupport

class ConfirmationController @Inject()(override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalActionProvider,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       renderer: Renderer)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(mrn: MovementReferenceNumber): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      request.userAnswers.get(PresentationOfficePage) match {
        case Some(presentationOffice) =>
          val contactUsMessage = if (presentationOffice.phoneNumber.isDefined) {
            msg"arrivalComplete.para2.withPhoneNumber".withArgs(presentationOffice.name, presentationOffice.phoneNumber)
          } else msg"arrivalComplete.para2".withArgs(presentationOffice.name)

          val json = Json.obj(
            "mrn"       -> mrn,
            "contactUs" -> contactUsMessage
          )
          sessionRepository.remove(mrn.toString)
          renderer.render("arrivalComplete.njk", json).map(Ok(_))

        case _ => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))

      }
  }

}
