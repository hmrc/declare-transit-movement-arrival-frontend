#!/bin/bash

echo ""
echo "Applying migration UseDifferentService"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /:mrn/useDifferentService                       controllers.UseDifferentServiceController.onPageLoad(mrn: MovementReferenceNumber)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "useDifferentService.title = useDifferentService" >> ../conf/messages.en
echo "useDifferentService.heading = useDifferentService" >> ../conf/messages.en

echo "Migration UseDifferentService completed"
