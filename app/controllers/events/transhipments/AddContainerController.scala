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
import forms.events.transhipments.AddContainerFormProvider
import javax.inject.Inject
import models.{DraftArrivalRef, Index, Mode}
import navigation.Navigator
import pages.events.transhipments.AddContainerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.{NunjucksSupport, Radios}
import viewModels.AddContainerViewModel

import scala.concurrent.{ExecutionContext, Future}

class AddContainerController @Inject()(
  override val messagesApi: MessagesApi,
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

  def onPageLoad(ref: DraftArrivalRef, eventIndex: Index, mode: Mode): Action[AnyContent] = (identify andThen getData(ref) andThen requireData).async {
    implicit request =>
      val json = Json.obj(
        "form"        -> form,
        "mode"        -> mode,
        "ref"         -> ref,
        "radios"      -> Radios.yesNo(form("value")),
        "onSubmitUrl" -> routes.AddContainerController.onSubmit(ref, eventIndex, mode).url
      ) ++ Json.toJsObject {
        AddContainerViewModel(eventIndex, request.userAnswers, mode)
      }

      renderer.render("events/transhipments/addContainer.njk", json).map(Ok(_))
  }

  def onSubmit(ref: DraftArrivalRef, eventIndex: Index, mode: Mode): Action[AnyContent] = (identify andThen getData(ref) andThen requireData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {

            val json = Json.obj(
              "form"        -> formWithErrors,
              "mode"        -> mode,
              "ref"         -> ref,
              "radios"      -> Radios.yesNo(formWithErrors("value")),
              "onSubmitUrl" -> routes.AddContainerController.onSubmit(ref, eventIndex, mode).url
            ) ++ Json.toJsObject {
              AddContainerViewModel(eventIndex, request.userAnswers, mode)
            }

            renderer.render("events/transhipments/addContainer.njk", json).map(BadRequest(_))
          },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AddContainerPage(eventIndex), value))
            } yield Redirect(navigator.nextPage(AddContainerPage(eventIndex), mode, updatedAnswers))
        )
  }
}
