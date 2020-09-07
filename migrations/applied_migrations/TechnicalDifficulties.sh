#!/bin/bash

echo ""
echo "Applying migration TechnicalDifficulties"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /:mrn/technicalDifficulties                       controllers.TechnicalDifficultiesController.onPageLoad(ref: MovementReferenceNumber)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "technicalDifficulties.title = technicalDifficulties" >> ../conf/messages.en
echo "technicalDifficulties.heading = technicalDifficulties" >> ../conf/messages.en

echo "Migration TechnicalDifficulties completed"
