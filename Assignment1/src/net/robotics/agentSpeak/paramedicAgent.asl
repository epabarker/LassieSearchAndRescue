
price(_Service,X) :- .random(R) & X = (10*R)+100.


plays(initiator,doctor).

    +atlocation(x,y)
    +colour(source[percept])
    +status(source[A])

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
       .print("Start the Resuce mission for ",C," critical and ",NC, " non-critical victims; Hospital is at (",X,",",Y,"), and we have ", Vcount, " victims and ", Ocount," known obstacles").
 


   
+startRescueMission(D,C,NC)
    <- .wait(2000);  				// wait for the beliefs to be obtained
       -+startRescueMission(D,C,NC).// replace the mental note
    		

+startSearch(victim,_,_) : .count(location(victim,_,_),Vcount)
    <- !check(cell);
       !requestVictimStatus;
       !victimStatus.

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

////////////////////////////////////////////////////////////////////////////////
+!requestVictimStatus(D,X,Y,C)
    <- .wait(2000);
     .send(D, tell, requestVictimStatus(X,Y,C)).
     if (critical(_,_)) {
        !at(+location(hospital,X,Y)[source(D)])
        }
        elif (~critical){
        if 
        }
////////////////////////////////////////////////////////////////////////////////

+!check(cell) : not victims(A)
   <- next(cell);
      !check(cell).
+!check(cell).

+victim(critical) : not .desire(takeVictim(critical,hospital))
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
+!ensure_pick(_).