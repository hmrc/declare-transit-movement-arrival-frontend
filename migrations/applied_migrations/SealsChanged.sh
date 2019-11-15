#!/bin/bash

echo ""
echo "Applying migration SealsChanged"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/sealsChanged                        controllers.SealsChangedController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/sealsChanged                        controllers.SealsChangedController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeSealsChanged                  controllers.SealsChangedController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeSealsChanged                  controllers.SealsChangedController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "sealsChanged.title = sealsChanged" >> ../conf/messages.en
echo "sealsChanged.heading = sealsChanged" >> ../conf/messages.en
echo "sealsChanged.checkYourAnswersLabel = sealsChanged" >> ../conf/messages.en
echo "sealsChanged.error.required = Select yes if sealsChanged" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySealsChangedUserAnswersEntry: Arbitrary[(SealsChangedPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[SealsChangedPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySealsChangedPage: Arbitrary[SealsChangedPage.type] =";\
    print "    Arbitrary(SealsChangedPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(SealsChangedPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def sealsChanged: Option[Row] = userAnswers.get(SealsChangedPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"sealsChanged.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(yesOrNo(answer)),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.SealsChangedController.onPageLoad(mrn, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"sealsChanged.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration SealsChanged completed"
