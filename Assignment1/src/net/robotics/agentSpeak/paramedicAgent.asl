// ========================================================================
// Initial beliefs and rules
// ========================================================================
//***** get location from robot, store in
// location(r, X,Y)

price(_Service,X) :- .random(R) & X = (10*R)+100.

allNonCriticalRescued :- .count(~critical(_,_), 0).

allCriticalRescued :- .count(critical(_,_), 0).

foundAllVictims :- .count(foundV(_,_), 3).

rescuedAllVictims :- .count(rescued(_,_), 3).

foundAllVictims :- nonCriticalRescued + criticalRescued. 

plays(initiator,doctor).

at(P) :- location(P,X,Y) & location(r,X,Y).
// ========================================================================
// Initial goals
// ========================================================================
// *****get our initial location from the robot*****

   //+atlocation(x,y)
   // +colour(source[percept])
   // +status(source[A])
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
       !startSearch;
       .print("Start the Resuce mission for ",C," critical and ",NC, " non-critical victims; Hospital is at (",X,",",Y,"), and we have ", Vcount, " victims and ", Ocount," known obstacles").
 


   
+startRescueMission(D,C,NC)
    <- .wait(2000);  				// wait for the beliefs to be obtained
       -+startRescueMission(D,C,NC).// replace the mental note
       
    		

+!startSearch
    <-	next(victim).
    
@lg[atomic] +victim(r) : not .desire(takeVictim(victim,hospital))
   <- .print("Critical victim!");
      !rescueVictim(victim,hospital).

+location(victim,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Victim could be at ",X,", ",Y); addVictim(X,Y).

+location(obstacle,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Obstacle is at ",X,", ",Y); addObstacle(X,Y).
    
+location(hospital,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Hospital is at ",X,", ",Y); addHospital(X,Y).
    

+critical(X,Y)
    <- .print("The victim at ", X, ",", Y, " is critical").

+~critical(X,Y)
    <- .print("The victim at ", X, ",", Y, " is not critical").


+!getScenario(D) <- .send(D,askAll,location(_,_,_)).

// Get closest victim from environment
// Are we at victim? If not, go to victim

+!startSearch
    <-  next(location)
    	?location(victim,X,Y)
     	!at(victim)
    	!check(X,Y);
       !requestVictimStatus;
       !victimStatus.


////////////////////////////////////////////////////////////////////////////////
+!requestVictimStatus(D,X,Y,C)
    <- .wait(2000);
     .send(D, tell, requestVictimStatus(X,Y,C)).
     
////////////////////////////////////////////////////////////////////////////////

/* +!check(cell) : not victims(A) //tell robot to check cell
   <- next(cell);
      !check(cell).
+!check(cell).

@lg[atomic] +victim(critical) : not .desire(takeVictim(critical,hospital))
   <- .print("Critical victim!");
      !rescueVictim(critical,hospital).

+!rescueVictim(Victim, doctor) : true 
   <- ?pos(hospital,X,Y); 
      -+pos(current,X,Y);
	  !take(victim, hospital);
	  !at(curr);
	  !!check(cell).

+!take(victim,hospital) : true
<- !ensure_pick(victim);
    !at(hospital);
    drop(victim).
	     
+!ensure_pick(victim) : victim
   <- .percieve(C);
      !ensure_pick(victim).
+!ensure_pick(_).*/

/////////////////////////////////////////////////////////////////////////////////////////////////

// we will have 3 plans : 1- for critical -> rescue
// 2- ~critical and the critical not rescued
// 3- rescuing ~critical when all criticals rescued

+!at(L) : at(L).
+!at(L) <- ?pos(L,X,Y);
           move_towards(X,Y);
           !at(L).



					  
					  
					  
					  
					  
					  
					  
					  