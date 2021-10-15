
# declare-transit-movement-arrival-frontend

This service allows a user to record a transit movement arrival at its destination.

Service manager port: 9483

### Testing

Run unit tests:
<pre>sbt test</pre>  
Run integration tests:  
<pre>sbt it:test</pre>  
or
<pre>sbt IntegrationTest/test</pre>  

### Running manually or for journey tests

<pre>sm --start CTC_TRADERS_ARRIVAL -r
sm --stop DECLARE_TRANSIT_MOVEMENT_ARRIVAL_FRONTEND
sbt run
</pre>

If you hit an entry point before running the journey tests, it gets the compile out of the way and can help keep the first tests from failing.  

e.g.: http://localhost:9483/manage-transit-movements-arrivals/movement-reference-number

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

