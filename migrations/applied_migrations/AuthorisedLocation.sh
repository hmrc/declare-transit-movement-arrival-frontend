#!/bin/bash

echo ""
echo "Applying migration AuthorisedLocation"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/authorisedLocation                        controllers.AuthorisedLocationController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/authorisedLocation                        controllers.AuthorisedLocationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeAuthorisedLocation                  controllers.AuthorisedLocationController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeAuthorisedLocation                  controllers.AuthorisedLocationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "authorisedLocation.title = authorisedLocation" >> ../conf/messages.en
echo "authorisedLocation.heading = authorisedLocation" >> ../conf/messages.en
echo "authorisedLocation.checkYourAnswersLabel = authorisedLocation" >> ../conf/messages.en
echo "authorisedLocation.error.required = Enter authorisedLocation" >> ../conf/messages.en
echo "authorisedLocation.error.length = AuthorisedLocation must be 8 characters or less" >> ../conf/messages.en
echo "authorisedLocation.error.invalid =  Authorised location code must only contain letters a to z or numbers 0 to 9" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorisedLocationUserAnswersEntry: Arbitrary[(AuthorisedLocationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AuthorisedLocationPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAuthorisedLocationPage: Arbitrary[AuthorisedLocationPage.type] =";\
    print "    Arbitrary(AuthorisedLocationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AuthorisedLocationPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def authorisedLocation: Option[Row] = userAnswers.get(AuthorisedLocationPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"authorisedLocation.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.AuthorisedLocationController.onPageLoad(ref, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"authorisedLocation.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration AuthorisedLocation completed"
