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

package controllers.events.seals

import controllers.actions._
import forms.events.seals.SealIdentityFormProvider
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.events.seals.SealIdentityPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import models.messages.Seal
import queries.{ContainersQuery, SealsQuery}

import scala.concurrent.{ExecutionContext, Future}

class SealIdentityController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: SealIdentityFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Index, sealIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        val preparedForm = request.userAnswers.get(SealIdentityPage(eventIndex, sealIndex)) match {
          case None        => form(sealIndex)
          case Some(value) => form(sealIndex).fill(value.numberOrMark)
        }

        renderView(mrn, mode, preparedForm).map(Ok(_))
    }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Index, sealIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        val seals = request.userAnswers.get(SealsQuery(eventIndex)).getOrElse(Seq.empty)

        form(sealIndex, seals)
          .bindFromRequest()
          .fold(
            formWithErrors => renderView(mrn, mode, formWithErrors).map(BadRequest(_)),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SealIdentityPage(eventIndex, sealIndex), Seal(value)))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(SealIdentityPage(eventIndex, sealIndex), mode, updatedAnswers))
          )
    }

  private def renderView(mrn: MovementReferenceNumber, mode: Mode, preparedForm: Form[String])(implicit request: DataRequest[AnyContent]) = {
    val json = Json.obj(
      "form" -> preparedForm,
      "mrn"  -> mrn,
      "mode" -> mode
    )

    renderer.render("events/seals/sealIdentity.njk", json)
  }
}
