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
import derivable.DeriveNumberOfContainers
import forms.events.transhipments.AddContainerFormProvider
import javax.inject.Inject
import models.{Mode, MovementReferenceNumber}
import navigation.Navigator
import pages.events.transhipments.AddContainerPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}
import utils.AddContainerHelper

import scala.concurrent.{ExecutionContext, Future}

class AddContainerController @Inject()(
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalActionProvider,
  requireData: DataRequiredAction,
  formProvider: AddContainerFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  private def getPageTitle(containerCount: Int)(implicit messages: Messages): String =
    if (containerCount == 1) {
      messages("addContainer.title.singular", containerCount)
    } else {
      messages("addContainer.title.plural", containerCount)
    }

  def onPageLoad(mrn: MovementReferenceNumber, eventIndex: Int, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      val preparedForm = request.userAnswers.get(AddContainerPage(eventIndex)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      val containerCount     = request.userAnswers.get(DeriveNumberOfContainers(eventIndex)).getOrElse(0)
      val addContainerHelper = AddContainerHelper(request.userAnswers)
      val containers: Option[Section] = request.userAnswers
        .get(DeriveNumberOfContainers(eventIndex))
        .map(List.range(0, _))
        .map(_.flatMap(addContainerHelper.containerRow(eventIndex, _)))
        .map(Section.apply)

      val json = Json.obj(
        "form"       -> preparedForm,
        "mode"       -> mode,
        "mrn"        -> mrn,
        "radios"     -> Radios.yesNo(preparedForm("value")),
        "pageTitle"  -> getPageTitle(containerCount),
        "containers" -> containers
      )

      renderer.render("events/transhipments/addContainer.njk", json).map(Ok(_))
  }

  def onSubmit(mrn: MovementReferenceNumber, eventIndex: Int, mode: Mode): Action[AnyContent] = (identify andThen getData(mrn) andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val json = Json.obj(
              "form"      -> formWithErrors,
              "mode"      -> mode,
              "mrn"       -> mrn,
              "pageTitle" -> getPageTitle(request.userAnswers.get(DeriveNumberOfContainers(eventIndex)).getOrElse(0)),
              "radios"    -> Radios.yesNo(formWithErrors("value"))
            )

            renderer.render("events/transhipments/addContainer.njk", json).map(BadRequest(_))
          },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AddContainerPage(eventIndex), value))
            } yield Redirect(navigator.nextPage(AddContainerPage(eventIndex), mode, updatedAnswers))
        )
  }
}
