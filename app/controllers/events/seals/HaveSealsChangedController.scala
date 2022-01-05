/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.events.seals

import controllers.actions._
import forms.events.seals.HaveSealsChangedFormProvider
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.events.seals.HaveSealsChangedPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class HaveSealsChangedController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: HaveSealsChangedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Index, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(HaveSealsChangedPage(eventIndex)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      renderView(mrn, eventIndex, mode, preparedForm).map(Ok(_))
  }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Index, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => renderView(mrn, eventIndex, mode, formWithErrors).map(BadRequest(_)),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(HaveSealsChangedPage(eventIndex), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(HaveSealsChangedPage(eventIndex), mode, updatedAnswers))
        )
  }

  private def renderView(mrn: MovementReferenceNumber, eventIndex: Index, mode: Mode, preparedForm: Form[Boolean])(implicit
    request: DataRequest[AnyContent]
  ): Future[Html] = {
    val json = Json.obj(
      "form"        -> preparedForm,
      "mode"        -> mode,
      "mrn"         -> mrn,
      "radios"      -> Radios.yesNo(preparedForm("value")),
      "onSubmitUrl" -> routes.HaveSealsChangedController.onSubmit(mrn, eventIndex, mode).url
    )
    renderer.render("events/seals/haveSealsChanged.njk", json)
  }
}
