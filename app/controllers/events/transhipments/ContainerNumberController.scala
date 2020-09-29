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

package controllers.events.transhipments

import controllers.actions._
import forms.events.transhipments.ContainerNumberFormProvider
import javax.inject.Inject
import models.domain.ContainerDomain
import models.{DraftArrivalRef, Index, Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.events.transhipments.ContainerNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.ContainersQuery
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class ContainerNumberController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: ContainerNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(ref: DraftArrivalRef, eventIndex: Index, containerIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(ref) andThen requireData).async {
      implicit request =>
        val preparedForm = request.userAnswers.get(ContainerNumberPage(eventIndex, containerIndex)) match {
          case None        => formProvider(containerIndex)
          case Some(value) => formProvider(containerIndex).fill(value.containerNumber)
        }

        val json = Json.obj(
          "form"        -> preparedForm,
          "ref"         -> ref,
          "mode"        -> mode,
          "onSubmitUrl" -> routes.ContainerNumberController.onSubmit(ref, eventIndex, containerIndex, mode).url
        )

        renderer.render("events/transhipments/containerNumber.njk", json).map(Ok(_))
    }

  def onSubmit(ref: DraftArrivalRef, eventIndex: Index, containerIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(ref) andThen requireData).async {
      implicit request =>
        val containers = request.userAnswers.get(ContainersQuery(eventIndex)).getOrElse(Seq.empty)

        formProvider(containerIndex, containers)
          .bindFromRequest()
          .fold(
            formWithErrors => {

              val json = Json.obj(
                "form"        -> formWithErrors,
                "ref"         -> ref,
                "mode"        -> mode,
                "onSubmitUrl" -> routes.ContainerNumberController.onSubmit(ref, eventIndex, containerIndex, mode).url
              )

              renderer.render("events/transhipments/containerNumber.njk", json).map(BadRequest(_))
            },
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ContainerNumberPage(eventIndex, containerIndex), ContainerDomain(value)))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ContainerNumberPage(eventIndex, containerIndex), mode, updatedAnswers))
          )
    }
}
