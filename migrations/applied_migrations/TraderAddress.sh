#!/bin/bash

echo ""
echo "Applying migration TraderAddress"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/traderAddress                        controllers.TraderAddressController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/traderAddress                        controllers.TraderAddressController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeTraderAddress                  controllers.TraderAddressController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeTraderAddress                  controllers.TraderAddressController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "traderAddress.title = traderAddress" >> ../conf/messages.en
echo "traderAddress.heading = traderAddress" >> ../conf/messages.en
echo "traderAddress.buildingAndStreet = buildingAndStreet" >> ../conf/messages.en
echo "traderAddress.postcode = postcode" >> ../conf/messages.en
echo "traderAddress.checkYourAnswersLabel = traderAddress" >> ../conf/messages.en
echo "traderAddress.error.buildingAndStreet.required = Enter buildingAndStreet" >> ../conf/messages.en
echo "traderAddress.error.postcode.required = Enter postcode" >> ../conf/messages.en
echo "traderAddress.error.buildingAndStreet.length = buildingAndStreet must be 32 characters or less" >> ../conf/messages.en
echo "traderAddress.error.postcode.length = postcode must be 32 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTraderAddressUserAnswersEntry: Arbitrary[(TraderAddressPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[TraderAddressPage.type]";\
    print "        value <- arbitrary[TraderAddress].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTraderAddressPage: Arbitrary[TraderAddressPage.type] =";\
    print "    Arbitrary(TraderAddressPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTraderAddress: Arbitrary[TraderAddress] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        buildingAndStreet <- arbitrary[String]";\
    print "        postcode <- arbitrary[String]";\
    print "      } yield TraderAddress(buildingAndStreet, postcode)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(TraderAddressPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def traderAddress: Option[Row] = userAnswers.get(TraderAddressPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"traderAddress.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(lit\"${answer.buildingAndStreet} ${answer.postcode}\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.TraderAddressController.onPageLoad(ref, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"traderAddress.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration TraderAddress completed"
