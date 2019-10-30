#!/bin/bash

echo ""
echo "Applying migration IncidentOnRoute"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/incidentOnRoute                        controllers.IncidentOnRouteController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/incidentOnRoute                        controllers.IncidentOnRouteController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeIncidentOnRoute                  controllers.IncidentOnRouteController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeIncidentOnRoute                  controllers.IncidentOnRouteController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "incidentOnRoute.title = Has something happened on route?" >> ../conf/messages.en
echo "incidentOnRoute.heading = Has something happened on route?" >> ../conf/messages.en
echo "incidentOnRoute.yes = Yes" >> ../conf/messages.en
echo "incidentOnRoute.no = No" >> ../conf/messages.en
echo "incidentOnRoute.checkYourAnswersLabel = Has something happened on route?" >> ../conf/messages.en
echo "incidentOnRoute.error.required = Select incidentOnRoute" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIncidentOnRouteUserAnswersEntry: Arbitrary[(IncidentOnRoutePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[IncidentOnRoutePage.type]";\
    print "        value <- arbitrary[IncidentOnRoute].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIncidentOnRoutePage: Arbitrary[IncidentOnRoutePage.type] =";\
    print "    Arbitrary(IncidentOnRoutePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIncidentOnRoute: Arbitrary[IncidentOnRoute] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(IncidentOnRoute.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(IncidentOnRoutePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def incidentOnRoute: Option[Row] = userAnswers.get(IncidentOnRoutePage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"incidentOnRoute.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(msg\"incidentOnRoute.$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.IncidentOnRouteController.onPageLoad(mrn, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"incidentOnRoute.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration IncidentOnRoute completed"
