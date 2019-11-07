#!/bin/bash

echo ""
echo "Applying migration TraderName"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/traderName                        controllers.TraderNameController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/traderName                        controllers.TraderNameController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeTraderName                  controllers.TraderNameController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeTraderName                  controllers.TraderNameController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "traderName.title = traderName" >> ../conf/messages.en
echo "traderName.heading = traderName" >> ../conf/messages.en
echo "traderName.checkYourAnswersLabel = traderName" >> ../conf/messages.en
echo "traderName.error.required = Enter traderName" >> ../conf/messages.en
echo "traderName.error.length = TraderName must be 32 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTraderNameUserAnswersEntry: Arbitrary[(TraderNamePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[TraderNamePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTraderNamePage: Arbitrary[TraderNamePage.type] =";\
    print "    Arbitrary(TraderNamePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(TraderNamePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def traderName: Option[Row] = userAnswers.get(TraderNamePage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"traderName.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.TraderNameController.onPageLoad(mrn, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"traderName.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration TraderName completed"
