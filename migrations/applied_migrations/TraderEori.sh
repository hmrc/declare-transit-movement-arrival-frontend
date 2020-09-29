#!/bin/bash

echo ""
echo "Applying migration TraderEori"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/traderEori                        controllers.TraderEoriController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/traderEori                        controllers.TraderEoriController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeTraderEori                  controllers.TraderEoriController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeTraderEori                  controllers.TraderEoriController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "traderEori.title = traderEori" >> ../conf/messages.en
echo "traderEori.heading = traderEori" >> ../conf/messages.en
echo "traderEori.checkYourAnswersLabel = traderEori" >> ../conf/messages.en
echo "traderEori.error.required = Enter traderEori" >> ../conf/messages.en
echo "traderEori.error.length = TraderEori must be 32 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTraderEoriUserAnswersEntry: Arbitrary[(TraderEoriPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[TraderEoriPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTraderEoriPage: Arbitrary[TraderEoriPage.type] =";\
    print "    Arbitrary(TraderEoriPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(TraderEoriPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def traderEori: Option[Row] = userAnswers.get(TraderEoriPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"traderEori.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.TraderEoriController.onPageLoad(ref, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"traderEori.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration TraderEori completed"
