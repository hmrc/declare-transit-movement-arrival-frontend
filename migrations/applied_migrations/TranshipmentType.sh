#!/bin/bash

echo ""
echo "Applying migration TranshipmentType"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/transhipmentType                        controllers.events.transhipments.TranshipmentTypeController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/transhipmentType                        controllers.events.transhipments.TranshipmentTypeController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeTranshipmentType                  controllers.events.transhipments.TranshipmentTypeController.onPageLoad(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeTranshipmentType                  controllers.events.transhipments.TranshipmentTypeController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "transhipmentType.title = What did the goods move to?" >> ../conf/messages.en
echo "transhipmentType.heading = What did the goods move to?" >> ../conf/messages.en
echo "transhipmentType.differentContainer = A different container" >> ../conf/messages.en
echo "transhipmentType.differentVehicle = A different vehicle" >> ../conf/messages.en
echo "transhipmentType.checkYourAnswersLabel = What did the goods move to?" >> ../conf/messages.en
echo "transhipmentType.error.required = Select transhipmentType" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/self: Generators =>/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTranshipmentTypeUserAnswersEntry: Arbitrary[(TranshipmentTypePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[TranshipmentTypePage.type]";\
    print "        value <- arbitrary[TranshipmentType].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTranshipmentTypePage: Arbitrary[TranshipmentTypePage.type] =";\
    print "    Arbitrary(TranshipmentTypePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTranshipmentType: Arbitrary[TranshipmentType] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(TranshipmentType.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(TranshipmentTypePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def transhipmentType: Option[Row] = userAnswers.get(TranshipmentTypePage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"transhipmentType.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(msg\"transhipmentType.$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.TranshipmentTypeController.onPageLoad(mrn, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"transhipmentType.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration TranshipmentType completed"
