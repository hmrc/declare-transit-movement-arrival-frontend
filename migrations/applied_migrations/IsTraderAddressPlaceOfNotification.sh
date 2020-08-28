#!/bin/bash

echo ""
echo "Applying migration IsTraderAddressPlaceOfNotification"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/isTraderAddressPlaceOfNotification                        controllers.IsTraderAddressPlaceOfNotificationController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/isTraderAddressPlaceOfNotification                        controllers.IsTraderAddressPlaceOfNotificationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeIsTraderAddressPlaceOfNotification                  controllers.IsTraderAddressPlaceOfNotificationController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeIsTraderAddressPlaceOfNotification                  controllers.IsTraderAddressPlaceOfNotificationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "isTraderAddressPlaceOfNotification.title = isTraderAddressPlaceOfNotification" >> ../conf/messages.en
echo "isTraderAddressPlaceOfNotification.heading = isTraderAddressPlaceOfNotification" >> ../conf/messages.en
echo "isTraderAddressPlaceOfNotification.checkYourAnswersLabel = isTraderAddressPlaceOfNotification" >> ../conf/messages.en
echo "isTraderAddressPlaceOfNotification.error.required = Select yes if isTraderAddressPlaceOfNotification" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIsTraderAddressPlaceOfNotificationUserAnswersEntry: Arbitrary[(IsTraderAddressPlaceOfNotificationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[IsTraderAddressPlaceOfNotificationPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIsTraderAddressPlaceOfNotificationPage: Arbitrary[IsTraderAddressPlaceOfNotificationPage.type] =";\
    print "    Arbitrary(IsTraderAddressPlaceOfNotificationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(IsTraderAddressPlaceOfNotificationPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def isTraderAddressPlaceOfNotification: Option[Row] = userAnswers.get(IsTraderAddressPlaceOfNotificationPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"isTraderAddressPlaceOfNotification.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(yesOrNo(answer)),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.IsTraderAddressPlaceOfNotificationController.onPageLoad(ref, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"isTraderAddressPlaceOfNotification.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration IsTraderAddressPlaceOfNotification completed"
