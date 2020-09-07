#!/bin/bash

echo ""
echo "Applying migration GoodsLocation"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /:mrn/goodsLocation                        controllers.GoodsLocationController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /:mrn/goodsLocation                        controllers.GoodsLocationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /:mrn/changeGoodsLocation                  controllers.GoodsLocationController.onPageLoad(ref: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /:mrn/changeGoodsLocation                  controllers.GoodsLocationController.onSubmit(mrn: MovementReferenceNumber, mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "goodsLocation.title = Where are the goods?" >> ../conf/messages.en
echo "goodsLocation.heading = Where are the goods?" >> ../conf/messages.en
echo "goodsLocation.borderForceOffice = Border Force office" >> ../conf/messages.en
echo "goodsLocation.authorisedConsigneesLocation = Authorised Consignee's location" >> ../conf/messages.en
echo "goodsLocation.checkYourAnswersLabel = Where are the goods?" >> ../conf/messages.en
echo "goodsLocation.error.required = Select goodsLocation" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryGoodsLocationUserAnswersEntry: Arbitrary[(GoodsLocationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[GoodsLocationPage.type]";\
    print "        value <- arbitrary[GoodsLocation].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryGoodsLocationPage: Arbitrary[GoodsLocationPage.type] =";\
    print "    Arbitrary(GoodsLocationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryGoodsLocation: Arbitrary[GoodsLocation] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(GoodsLocation.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(GoodsLocationPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class CheckYourAnswersHelper/ {\
     print;\
     print "";\
     print "  def goodsLocation: Option[Row] = userAnswers.get(GoodsLocationPage) map {";\
     print "    answer =>";\
     print "      Row(";\
     print "        key     = Key(msg\"goodsLocation.checkYourAnswersLabel\", classes = Seq(\"govuk-!-width-one-half\")),";\
     print "        value   = Value(msg\"goodsLocation.$answer\"),";\
     print "        actions = List(";\
     print "          Action(";\
     print "            content            = msg\"site.edit\",";\
     print "            href               = routes.GoodsLocationController.onPageLoad(ref, CheckMode).url,";\
     print "            visuallyHiddenText = Some(msg\"site.edit.hidden\".withArgs(msg\"goodsLocation.checkYourAnswersLabel\"))";\
     print "          )";\
     print "        )";\
     print "      )";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration GoodsLocation completed"
