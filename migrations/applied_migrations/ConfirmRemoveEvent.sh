#!/bin/bash

echo ""
echo "Applying migration ConfirmRemoveEvent"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/confirmRemoveEvent                        controllers.events.ConfirmRemoveEventController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/confirmRemoveEvent                        controllers.events.ConfirmRemoveEventController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeConfirmRemoveEvent                  controllers.events.ConfirmRemoveEventController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeConfirmRemoveEvent                  controllers.events.ConfirmRemoveEventController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "confirmRemoveEvent.title = confirmRemoveEvent" >> ../conf/messages.en
echo "confirmRemoveEvent.heading = confirmRemoveEvent" >> ../conf/messages.en
echo "confirmRemoveEvent.checkYourAnswersLabel = confirmRemoveEvent" >> ../conf/messages.en
echo "confirmRemoveEvent.error.required = Select yes if confirmRemoveEvent" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryConfirmRemoveEventUserAnswersEntry: Arbitrary[(ConfirmRemoveEventPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ConfirmRemoveEventPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryConfirmRemoveEventPage: Arbitrary[ConfirmRemoveEventPage.type] =";\
    print "    Arbitrary(ConfirmRemoveEventPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ConfirmRemoveEventPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def confirmRemoveEvent: Option[Row] = userAnswers.get(ConfirmRemoveEventPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"confirmRemoveEvent.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(yesOrNo(answer)),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.ConfirmRemoveEventController.onPageLoad(ref, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"confirmRemoveEvent.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ConfirmRemoveEvent completed"
