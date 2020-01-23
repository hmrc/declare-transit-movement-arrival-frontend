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
import forms.events.transhipments.ConfirmRemoveContainerFormProvider
import javax.inject.Inject
import models.{Index, Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.events.transhipments.{ConfirmRemoveContainerPage, ContainerNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}

import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveContainerController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: ConfirmRemoveContainerFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form                           = formProvider()
  private val confirmRemoveContainerTemplate = "events/transhipments/confirmRemoveContainer.njk"

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Int, containerIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(ContainerNumberPage(eventIndex, containerIndex)) match {
          case Some(container) => {
            val json = Json.obj(
              "form"            -> form,
              "mode"            -> mode,
              "mrn"             -> mrn,
              "containerNumber" -> container.containerNumber,
              "radios"          -> Radios.yesNo(form("value"))
            )

            renderer.render(confirmRemoveContainerTemplate, json).map(Ok(_))
          }
          case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }
    }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Int, containerIndex: Index, mode: Mode): Action[AnyContent] =
    (identify andThen getData(mrn) andThen requireData).async {
      implicit request =>
        request.userAnswers.get(ContainerNumberPage(eventIndex, containerIndex)) match {
          case Some(container) => {
            form
              .bindFromRequest()
              .fold(
                formWithErrors => {

                  val json = Json.obj(
                    "form"            -> formWithErrors,
                    "mode"            -> mode,
                    "mrn"             -> mrn,
                    "containerNumber" -> container.containerNumber,
                    "radios"          -> Radios.yesNo(formWithErrors("value"))
                  )

                  renderer.render(confirmRemoveContainerTemplate, json).map(BadRequest(_))
                }, {
                  case true =>
                    for {
                      updatedAnswers <- Future.fromTry(request.userAnswers.remove(ContainerNumberPage(eventIndex, containerIndex)))
                      _              <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(navigator.nextPage(ConfirmRemoveContainerPage(eventIndex), mode, updatedAnswers))
                  case false =>
                    Future.successful(Redirect(navigator.nextPage(ConfirmRemoveContainerPage(eventIndex), mode, request.userAnswers)))
                }
              )
          }
          case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }

    }
}
