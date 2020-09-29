#!/bin/bash

echo ""
echo "Applying migration HaveSealsChanged"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/haveSealsChanged                        controllers.events.seals.HaveSealsChangedController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/haveSealsChanged                        controllers.events.seals.HaveSealsChangedController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeHaveSealsChanged                  controllers.events.seals.HaveSealsChangedController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeHaveSealsChanged                  controllers.events.seals.HaveSealsChangedController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "haveSealsChanged.title = haveSealsChanged" >> ../conf/messages.en
echo "haveSealsChanged.heading = haveSealsChanged" >> ../conf/messages.en
echo "haveSealsChanged.checkYourAnswersLabel = haveSealsChanged" >> ../conf/messages.en
echo "haveSealsChanged.error.required = Select yes if haveSealsChanged" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHaveSealsChangedUserAnswersEntry: Arbitrary[(HaveSealsChangedPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[HaveSealsChangedPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHaveSealsChangedPage: Arbitrary[HaveSealsChangedPage.type] =";\
    print "    Arbitrary(HaveSealsChangedPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(HaveSealsChangedPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def haveSealsChanged: Option[Row] = userAnswers.get(HaveSealsChangedPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"haveSealsChanged.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(yesOrNo(answer)),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.HaveSealsChangedController.onPageLoad(ref, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"haveSealsChanged.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration HaveSealsChanged completed"
