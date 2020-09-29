#!/bin/bash

echo ""
echo "Applying migration ConfirmRemoveContainer"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/confirmRemoveContainer                        controllers.events.transhipments.ConfirmRemoveContainerController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/confirmRemoveContainer                        controllers.events.transhipments.ConfirmRemoveContainerController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeConfirmRemoveContainer                  controllers.events.transhipments.ConfirmRemoveContainerController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeConfirmRemoveContainer                  controllers.events.transhipments.ConfirmRemoveContainerController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "confirmRemoveContainer.title = confirmRemoveContainer" >> ../conf/messages.en
echo "confirmRemoveContainer.heading = confirmRemoveContainer" >> ../conf/messages.en
echo "confirmRemoveContainer.checkYourAnswersLabel = confirmRemoveContainer" >> ../conf/messages.en
echo "confirmRemoveContainer.error.required = Select yes if confirmRemoveContainer" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryConfirmRemoveContainerUserAnswersEntry: Arbitrary[(ConfirmRemoveContainerPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ConfirmRemoveContainerPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryConfirmRemoveContainerPage: Arbitrary[ConfirmRemoveContainerPage.type] =";\
    print "    Arbitrary(ConfirmRemoveContainerPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ConfirmRemoveContainerPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def confirmRemoveContainer: Option[Row] = userAnswers.get(ConfirmRemoveContainerPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"confirmRemoveContainer.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(yesOrNo(answer)),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.ConfirmRemoveContainerController.onPageLoad(ref, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"confirmRemoveContainer.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ConfirmRemoveContainer completed"
