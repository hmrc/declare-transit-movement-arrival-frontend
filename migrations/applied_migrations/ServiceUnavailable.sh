#!/bin/bash

echo ""
echo "Applying migration ServiceUnavailable"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /:mrn/serviceUnavailable                       controllers.ServiceUnavailableController.onPageLoad(mrn: MovementReferenceNumber)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "serviceUnavailable.title = serviceUnavailable" >> ../conf/messages.en
echo "serviceUnavailable.heading = serviceUnavailable" >> ../conf/messages.en

echo "Migration ServiceUnavailable completed"
