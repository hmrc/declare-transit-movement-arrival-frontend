#!/bin/bash

echo ""
echo "Applying migration CustomsSubPlace"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/customsSubPlace                        controllers.CustomsSubPlaceController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/customsSubPlace                        controllers.CustomsSubPlaceController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeCustomsSubPlace                  controllers.CustomsSubPlaceController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeCustomsSubPlace                  controllers.CustomsSubPlaceController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "customsSubPlace.title = customsSubPlace" >> ../conf/messages.en
echo "customsSubPlace.heading = customsSubPlace" >> ../conf/messages.en
echo "customsSubPlace.checkYourAnswersLabel = customsSubPlace" >> ../conf/messages.en
echo "customsSubPlace.error.required = Enter customsSubPlace" >> ../conf/messages.en
echo "customsSubPlace.error.length = CustomsSubPlace must be 8 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCustomsSubPlaceUserAnswersEntry: Arbitrary[(CustomsSubPlacePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[CustomsSubPlacePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCustomsSubPlacePage: Arbitrary[CustomsSubPlacePage.type] =";\
    print "    Arbitrary(CustomsSubPlacePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(CustomsSubPlacePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def customsSubPlace: Option[Row] = userAnswers.get(CustomsSubPlacePage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"customsSubPlace.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.CustomsSubPlaceController.onPageLoad(ref, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"customsSubPlace.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration CustomsSubPlace completed"
