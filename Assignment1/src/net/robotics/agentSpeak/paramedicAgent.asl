// ========================================================================
// Initial beliefs and rules
// ========================================================================
//***** get location from robot, store in
// location(r, X,Y)


price(_Service,X) :- .random(R) & X = (10*R)+100.

//If all critical have been rescued, this will evaluate as true.
allCriticalRescued :- .count(cRescued(_,_), CRITICALCOUNT).

//If all victim locations have been found, this will evaluate as true. 
foundAllVictims :- .count(foundV(_,_), 3).

//If all victims have been rescued, this will evaluate as true. 
rescuedAllVictims :- .count(rescued(_,_), 3).

plays(initiator,doctor).


// ========================================================================
// Plan Library
// ========================================================================
   

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
       CRITICALCOUNT = C;
       !search;
       .print("Start the Resuce mission for ",C," critical and ",NC, " non-critical victims; Hospital is at (",X,",",Y,"), and we have ", Vcount, " victims and ", Ocount," known obstacles").
   
+startRescueMission(D,C,NC)
    <- .wait(2000);  				// wait for the beliefs to be obtained
       -+startRescueMission(D,C,NC).// replace the mental note
    
+location(victim,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Victim could be at ",X,", ",Y); addVictim(X,Y).

+location(obstacle,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Obstacle is at ",X,", ",Y); addObstacle(X,Y).
    
+location(hospital,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Hospital is at ",X,", ",Y); addHospital(X,Y).
    

+critical(X,Y)
    <-  .print("The victim at ", X, ",", Y, " is critical");

+~critical(X,Y)
    <-  .print("The victim at ", X, ",", Y, " is not critical");

+colour(X,Y,C)
    <- !requestVictimStatus(doctor,X,Y,C).

+location(r,X,Y) : location(victim,X,Y)
    <-  !perceive(colour);
        !checkColour(X,Y).

+!getScenario(D) <- .send(D,askAll,location(_,_,_)).


////////////////////////////////////////////////////////////////////////////////
+!requestVictimStatus(D,X,Y,C)
    <- .wait(2000);
     .send(D, tell, requestVictimStatus(X,Y,C)).
////////////////////////////////////////////////////////////////////////////////

+!search : not rescuedAllVictims
    <-  !next(victim);
        !search.
+!search : rescuedAllVictims
    <-  !go(home);                                                                  // TO SERVER

+!checkColour(X,Y) : colour(X,Y,burgandy) | colour(X,Y,cyan)
    <-  !requestVictimStatus(D,X,Y,C);
        !intention(X,Y).
+!checkColour(X,Y) : not (colour(X,Y,burgandy) | colour(X,Y,cyan))
    <-  -location(victim,X,Y).

// If the victim is critical:
+!intention(X,Y) : critical(X,Y)
    <-  !rescue(X,Y);       // Go to hospital.                                      // TO SERVER
// If the victim is non-critical, and not all critical victims have been rescued:
+!intention(X,Y) : ~critical(X,Y) & not allCriticalRescued
    <-  !next(victim).      // Go to the next victim.                               // TO SERVER
// If the victim is non-critical, and all victims have been rescued:
+!intention(X,Y) : ~critical(X,Y) & allCriticalRescued
    <-  !rescue(X,Y);       // Go to hospital.                                      // TO SERVER
        

+!rescue(X,Y) : true 
    <-  !take(victim);                                                              // TO SERVER
        -location(victim,X,Y);                                                      // TO SERVER
        !go(hospital);                                                              // TO SERVER
        !drop(victim);                                                              // TO SERVER
        +rescued(X,Y).      // Add to the count of rescued victims.