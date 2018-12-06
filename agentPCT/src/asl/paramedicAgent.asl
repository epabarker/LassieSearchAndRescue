// ========================================================================
// Initial beliefs and rules
// ========================================================================
//***** get location from robot, store in



price(_Service,X) :- .random(R) & X = (10*R)+100.

//If all critical have been rescued, this will evaluate as true.
allCriticalRescued :- criticalCount(0).

//If all victim locations have been found, this will evaluate as true. 
foundAllVictims :- .count(foundV(_,_), 3).

//If all victims have been rescued, this will evaluate as true. 
rescuedAllVictims :- .count(rescued(_,_), 3).

plays(initiator,doctor).

location(r,1,2)[source(percept)].

// ========================================================================
// Plan Library
// ========================================================================
   
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
       !search;
       .print("Start the Resuce mission for ",C," critical and ",NC, " non-critical victims; Hospital is at (",X,",",Y,"), and we have ", Vcount, " victims and ", Ocount," known obstacles").
   
+startRescueMission(D,C,NC)
    <- .wait(2000);  				// wait for the beliefs to be obtained
       -+startRescueMission(D,C,NC).// replace the mental note
    
+toBeRescued(X,Y): plays(initiator,D)
	<- .print("Need to come back to",X,",",Y); addToBeRescued(X,Y).  
 
@r
+location(r,X,Y) : plays(initiator,D) & location(victim,X,Y)
    <-  addRobot(X,Y);
    	perceiveColour;															// TO SERVER
    	.wait(2000);
        !checkColour(X,Y).
@alr
+location(r,X,Y)[source(percept)]: plays(initiator,D)
	<- .print("Robot is at ",X,", ",Y); addRobot(X,Y).


@alv  
+location(victim,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Victim could be at ",X,", ",Y); addVictim(X,Y).
    
@rlv
-location(victim,X,Y): plays(initiator,D)
    <- .print("Victim could be at ",X,", ",Y); removeVictim(X,Y).

@alo
+location(obstacle,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Obstacle is at ",X,", ",Y); addObstacle(X,Y).

@alh
+location(hospital,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Hospital is at ",X,", ",Y); addHospital(X,Y).

@ac   
+critical(X,Y) : true
    <-  .print("The victim at ", X, ",", Y, " is critical").
    
@anc
+~critical(X,Y): true
    <-  .print("The victim at ", X, ",", Y, " is not critical").

//+colour(X,Y,C): true
//    <- !requestVictimStatus(doctor,X,Y,C).


        
      
@getScenario
+!getScenario(D) <- .send(D,askAll,location(_,_,_)).

@requestVictimStatus
+!requestVictimStatus(D,X,Y,C)
    <- 	.send(D, tell, requestVictimStatus(X,Y,C));
    	.wait(2000).
@search
+!search : not foundAllVictims
    <-  nextVictim;
    	.wait(2000);
        !search.
+!search : allCriticalRescued & not rescuedAllVictims
    <-  nextToBeRescued;
    	.wait(2000);
    	!search.
+!search : rescuedAllVictims
	<- 	goHome.                                                                 // TO SERVER

@checkColour
+!checkColour(X,Y) : colour(X,Y,burgandy)
    <-  .print("Colour recognised as victim");
    	+foundV(X,Y);
    	!requestVictimStatus(doctor,X,Y,burgandy);
        !intention(X,Y).
+!checkColour(X,Y) : colour(X,Y,cyan)
    <-  .print("Colour recognised as victim");
    	+foundV(X,Y);
    	!requestVictimStatus(doctor,X,Y,cyan);
        !intention(X,Y).
+!checkColour(X,Y) : not (colour(X,Y,burgandy) | colour(X,Y,cyan))
    <-  .print("Colour not recognised as victim");
    	-location(victim,X,Y)[source(doctor)].

// If the victim is critical:
@intention
+!intention(X,Y) : critical(X,Y)
    <-  !rescue(X,Y).       // Go to hospital.                                      
    
// If the victim is non-critical, and not all critical victims have been rescued:
+!intention(X,Y) : ~critical(X,Y) & not allCriticalRescued
    <-  +toBeRescued(X,Y);
    	-location(victim,X,Y)[source(doctor)];
    	nextVictim.      // Go to the next victim.                              // TO SERVER
    
// If the victim is non-critical, and all victims have been rescued:
+!intention(X,Y) : ~critical(X,Y) & allCriticalRescued
    <-  !rescue(X,Y).       // Go to hospital.                                  // TO SERVER
       
@rescue 
+!rescue(X,Y) : critical(X,Y) 
    <-  ?criticalCount(C);
    	NewCount = C - 1;
    	+criticalCount(NewCount);
    	-criticalCount(C);
    	takeVictim;                                                             // TO SERVER
        -location(victim,X,Y)[source(doctor)];                                                     
        goHospital;                                                            	// TO SERVER
        dropVictim;                                                            	// TO SERVER
        +rescued(X,Y).      // Add to the count of rescued victims.
+!rescue(X,Y) : true 
    <-  takeVictim;                                                              // TO SERVER
        -location(victim,X,Y)[source(doctor)];                                                     
        goHospital;                                                            // TO SERVER
        dropVictim;                                                              // TO SERVER
        +rescued(X,Y).      // Add to the count of rescued victims.
    
