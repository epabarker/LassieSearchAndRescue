// ========================================================================
// Initial beliefs and rules
// ========================================================================
//***** get location from robot, store in

price(_Service,X) :- .random(R) & X = (10*R)+100.

allCriticalRescued :- criticalCount(0).

foundAllVictims :- .count(foundV(_,_), 3).

rescuedAllVictims :- .count(rescued(_,_), 3).

plays(initiator,doctor4).

location(r,0,0)[source(percept)].

at :- location(r,X,Y) & target(X,Y).

// ========================================================================
// Plan Library
// ========================================================================


+target(X,Y)[source(percept)]: true
	<-	+target(X,Y).

@plays
+plays(initiator,In)
   :  .my_name(Me)
   <- .send(In,tell,introduction(participant,Me)).

// answer to Call For Proposal
@c1 +cfp(CNPId,Task,C,NC)[source(A)]
   :  plays(initiator,A) & price(Task,Offer)
   <- +proposal(CNPId,Task,C,NC,Offer);		// remember my proposal
      .send(A,tell,propose(CNPId,Offer)).

// Handling an Accept message
@r1 +accept_proposal(CNPId)[source(A)]
		: proposal(CNPId,Task,C,NC,Offer)
		<- !getScenario(A);
		    +startRescueMission(A,C,NC).

// Handling a Reject message
@r2 +reject_proposal(CNPId)
		<- .print("I lost CNP ",CNPId, ".");
		// clear memory
		-proposal(CNPId,_,_).

+startRescueMission(D,C,NC) : location(hospital,X,Y) &
							  location(victim,_,_) &
							  location(obstacle,_,_)
    <- .count(location(victim,_,_),Vcount);		// Determine the victims
       .count(location(obstacle,_,_),Ocount);	// Determine the obstacles
       +criticalCount(C);
       .print("Start the Resuce mission for ",C," critical and ",NC, " non-critical victims; Hospital is at (",X,",",Y,"), and we have ", Vcount, " victims and ", Ocount," known obstacles");
       !connection.

+startRescueMission(D,C,NC)
    <- .wait(2000);  				// wait for the beliefs to be obtained
       -+startRescueMission(D,C,NC).// replace the mental note

+toBeRescued(X,Y) : true
	<- .print("Need to come back to",X,",",Y); addToBeRescued(X,Y).
	
-toBeRescued(X,Y) : true
	<- .print("Removed victim at ",X,",",Y, " as it has been rescued"); removeToBeRescued(X,Y).

+atTarget : target(X,Y) & toBeRescued(X,Y) & not carrying(victim)
	<- 	.print("We are at our target, and there is a noncritical we havent rescued yet");
		!rescue(X,Y).
		
		

+atTarget : target(X,Y) & location(victim,X,Y) & not carrying(victim)
    <-  .print("We are at our target, and there may be a victim here");
    	perceiveColour;
        !checkColour(X,Y).
        

+atTarget : target(X,Y) & location(hospital,X,Y)
	<-	-atTarget;
		.print("At Hospital, Removed target ", X,",",Y);
		-target(X,Y).

+location(r,X,Y)[source(percept)]: true
	<- .print("Robot is at ",X,", ",Y); addRobot(X,Y).

+location(victim,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Victim could be at ",X,", ",Y); addVictim(X,Y).

-location(victim,X,Y): true
    <- .print("Removing victim at ",X,", ",Y); removeVictim(X,Y).

+location(obstacle,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Obstacle is at ",X,", ",Y); addObstacle(X,Y).

+location(hospital,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Hospital is at ",X,", ",Y); addHospital(X,Y).

+critical(X,Y) : true
    <-  .print("The victim at ", X, ",", Y, " is critical").

+~critical(X,Y): true
    <-  .print("The victim at ", X, ",", Y, " is not critical").

+carrying(victim): true
	<-	.print("Putting victim on stretcher");
		takeVictim.

-carrying(victim): true
	<-	.print("Taking victim off stretcher");
		dropVictim.


+!getScenario(D) <- .send(D,askAll,location(_,_,_)).

+!requestVictimStatus(D,X,Y,C)
    <- 	.send(D, tell, requestVictimStatus(X,Y,C));
    	.wait(2000).

+!search : not foundAllVictims & not at & not target(_,_)
    <-  nextVictim;
    	!at;
        !search.
+!search : allCriticalRescued & not rescuedAllVictims & not at & not target(_,_)
    <-  nextToBeRescued;
		!at;
    	!search.
+!search : rescuedAllVictims & not at & not target(_,_)
	<- 	goHome;
		//Add target percept from environment.
		!at.
+!search : true
	<- 	.wait(1000);
		!search.

+!checkColour(X,Y) : colour(X,Y,burgandy)
    <-  .print("Colour recognised as victim");
    	+foundV(X,Y);
    	!requestVictimStatus(doctor4,X,Y,burgandy);
        !intention(X,Y).
+!checkColour(X,Y) : colour(X,Y,cyan)
    <-  .print("Colour recognised as victim");
    	+foundV(X,Y);
    	!requestVictimStatus(doctor4,X,Y,cyan);
        !intention(X,Y).
+!checkColour(X,Y) : not (colour(X,Y,burgandy) | colour(X,Y,cyan))
    <-  .print("Colour not recognised as victim");
    	-atTarget;
        -target(X,Y);
    	-location(victim,X,Y)[source(doctor4)].

+!intention(X,Y) : critical(X,Y)
    <-  .print("Status: critical, intention: rescue");
    	!rescue(X,Y).

+!intention(X,Y) : ~critical(X,Y) & not allCriticalRescued
    <-  .print("Status: noncritical, not all critical rescued. Intention: will come back.");
    	+toBeRescued(X,Y);
    	-atTarget;
        -target(X,Y);
    	-location(victim,X,Y)[source(doctor4)].

+!intention(X,Y) : ~critical(X,Y) & allCriticalRescued
    <-  .print("Status: noncritical, all critical rescued. Intention: rescue");
    	!rescue(X,Y).

+!rescue(X,Y) : critical(X,Y)
    <-  ?criticalCount(C);
    	NewCount = C - 1;
    	-atTarget;
        -target(X,Y);
    	+criticalCount(NewCount);
    	-criticalCount(C);
    	+carrying(victim);
        -location(victim,X,Y)[source(doctor4)];
        goHospital;
        .print("Going to hospital");
        !at;
        -carrying(victim);
        +rescued(X,Y).
+!rescue(X,Y) : toBeRescued(X,Y)
    <-  +carrying(victim);
    	-atTarget;
        -target(X,Y);
        -toBeRescued(X,Y);
        goHospital;
        .print("Going to hospital");
        !at;
        -carrying(victim);
        +rescued(X,Y).
+!rescue(X,Y) : true
    <-  +carrying(victim);
    	-atTarget;
        -target(X,Y);
        -location(victim,X,Y)[source(doctor4)];
        goHospital;
        .print("Going to hospital");
        !at;
        -carrying(victim);
        +rescued(X,Y).

+!at : at
	<- .print("At target");
		+atTarget.

+!at : not at
	<-	checkLocation;
		.wait(2000);
		!at.
		
		
+!connection : connected(_)
	<-	!search.

+!connection : true
	<-	.print("CHECKING CONNECTION");
		isConnected;
		.wait(1000);
		!connection.

