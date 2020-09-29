#!/bin/bash

echo ""
echo "Applying migration ArrivalComplete"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /:mrn/arrivalComplete                       controllers.ConfirmationController.onPageLoad(ref: MovementReferenceNumber)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "arrivalComplete.title = arrivalComplete" >> ../conf/messages.en
echo "arrivalComplete.heading = arrivalComplete" >> ../conf/messages.en

echo "Migration ArrivalComplete completed"
