#!/bin/bash

echo ""
echo "Applying migration PlaceOfNotification"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/placeOfNotification                        controllers.PlaceOfNotificationController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/placeOfNotification                        controllers.PlaceOfNotificationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changePlaceOfNotification                  controllers.PlaceOfNotificationController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changePlaceOfNotification                  controllers.PlaceOfNotificationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "placeOfNotification.title = placeOfNotification" >> ../conf/messages.en
echo "placeOfNotification.heading = placeOfNotification" >> ../conf/messages.en
echo "placeOfNotification.checkYourAnswersLabel = placeOfNotification" >> ../conf/messages.en
echo "placeOfNotification.error.required = Enter placeOfNotification" >> ../conf/messages.en
echo "placeOfNotification.error.length = PlaceOfNotification must be 35 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPlaceOfNotificationUserAnswersEntry: Arbitrary[(PlaceOfNotificationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[PlaceOfNotificationPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPlaceOfNotificationPage: Arbitrary[PlaceOfNotificationPage.type] =";\
    print "    Arbitrary(PlaceOfNotificationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(PlaceOfNotificationPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def placeOfNotification: Option[Row] = userAnswers.get(PlaceOfNotificationPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"placeOfNotification.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.PlaceOfNotificationController.onPageLoad(ref, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"placeOfNotification.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration PlaceOfNotification completed"
