#!/bin/bash

echo ""
echo "Applying migration ArrivalRejection"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /:mrn/arrivalRejection                       controllers.ArrivalRejectionController.onPageLoad(ref: MovementReferenceNumber)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "arrivalRejection.title = arrivalRejection" >> ../conf/messages.en
echo "arrivalRejection.heading = arrivalRejection" >> ../conf/messages.en

echo "Migration ArrivalRejection completed"
