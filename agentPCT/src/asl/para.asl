// ========================================================================
// Initial beliefs and rules
// ========================================================================
//***** get location from robot, store in



price(_Service,X) :- .random(R) & X = (10*R)+100.

allCriticalRescued :- criticalCount(0).

foundAllVictims :- .count(foundV(_,_), 3).

rescuedAllVictims :- .count(rescued(_,_), 3).

plays(initiator,doctor).

location(r,1,2)[source(percept)].

at(P) :- location(P,X,Y) & location(r, X,Y).

// ========================================================================
// Plan Library
// ========================================================================

/*+target(X,Y)[source(percept)]: true
	<-	+target(X,Y).*/


////////////////////////////////////////////////////////////////////////////////////////////////   
@plays
+plays(initiator,In)
   :  .my_name(Me)
   <- .send(In,tell,introduction(participant,Me)).

// answer to Call For Proposal
@c1 +cfp(CNPId,Task,C,NC)[source(A)]
   :  plays(initiator,A) & price(Task,Offer)
   <- +proposal(CNPId,Task,C,NC,Offer);		// remember my proposal                                    //////////////////   CNP
      .send(A,tell,propose(CNPId,Offer)).                                                             //////////////////   CNP
                                                                                                     //////////////////   CNP
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

/////////////////////////////////////////////////////////////////////////////////////////////////
+startRescueMission(D,C,NC) : location(hospital,X,Y) & 
							  location(victim,_,_) &
							  location(obstacle,_,_)
    <- .count(location(victim,_,_),Vcount);		// Determine the victims
       .count(location(obstacle,_,_),Ocount);	// Determine the obstacles
       +criticalCount(C);
       .print("Start the Resuce mission for ",C," critical and ",NC, " non-critical victims; Hospital is at (",X,",",Y,"), and we have ", Vcount, " victims and ", Ocount," known obstacles");
       !search.
   
+startRescueMission(D,C,NC)
    <- .wait(2000);  				// wait for the beliefs to be obtained
       -+startRescueMission(D,C,NC).// replace the mental note

+toBeRescued(X,Y) : true
	<- .print("Need to come back to",X,",",Y); addToBeRescued(X,Y).  
    
/*+at : target(X,Y) & toBeRescued(X,Y) & not carrying(victim)
	<- 	.print("We are at our target, and there is a noncritical we havent rescued yet");
		!rescue(X,Y);
		-target(X,Y).
 
+at : target(X,Y) & location(victim,X,Y) & not carrying(victim)
    <-  .print("We are at our target, and there may be a victim here");  
    	perceiveColour;															
    	.wait(2000);
        !checkColour(X,Y);
        -target(X,Y).
        
+at : target(X,Y) & location(hospital,X,Y)
	<-	-target(X,Y).*/

+location(r,X,Y)[source(percept)]: true
	<- .print("Robot is at ",X,", ",Y); addRobot(X,Y).

+location(victim,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Victim could be at ",X,", ",Y); addVictim(X,Y).
    
-location(victim,X,Y): true
    <- .print("Victim could be at ",X,", ",Y); removeVictim(X,Y).

+location(obstacle,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Obstacle is at ",X,", ",Y); addObstacle(X,Y).

+location(hospital,X,Y)[source(D)]: plays(initiator,D)
    <- .print("Hospital is at ",X,", ",Y); addHospital(X,Y).

+critical(X,Y) : true
    <-  .print("The victim at ", X, ",", Y, " is critical").
    
+~critical(X,Y): true
    <-  .print("The victim at ", X, ",", Y, " is not critical").

+carrying(victim): true
	<-	takeVictim.
	
-carrying(victim): true
	<-	dropVictim.
        

+startRescueMission(D,C,NC) : location(hospital,X,Y) & 
							  location(victim,_,_) &
							  location(obstacle,_,_)
    <- .count(location(victim,_,_),Vcount);		// Determine the victims
       .count(location(obstacle,_,_),Ocount);	// Determine the obstacles
       +criticalCount(C);
       //.print("Start the Resuce mission for ",C," critical and ",NC, " non-critical victims; Hospital is at (",X,",",Y,"), and we have ", Vcount, " victims and ", Ocount," known obstacles");
       !search.
   
+startRescueMission(D,C,NC)
    <- .wait(2000);  				// wait for the beliefs to be obtained
       -+startRescueMission(D,C,NC).// replace the mental note

+!getScenario(D) <- .send(D,askAll,location(_,_,_)).

+!requestVictimStatus(D,X,Y,C)
    <- 	.send(D, tell, requestVictimStatus(X,Y,C));
    	.wait(2000).

////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////
// the search has 3 scenarios : 1- we have nound al victims and we dont have the belief that we are at the target
// 2- we've rescued criticals and but not ~criticals
//3- go back to hospital (which you dont need to seearch)
+!search : not foundAllVictims & not at
    <-  nextVictim;
    	!at;
        !search.
+!search : allCriticalRescued & not rescuedAllVictims & not at
    <-  nextToBeRescued;
		!at;
    	!search.

/*+!search : rescuedAllVictims & not at
	<- 	goHome;                                                          
		!at.*/
/*+!search : at
	<- 	.wait(1000);
		!search.*/
////////////////////////////////////////////////////////////////////////////////////////////////
/* this is the part of the code where certain improvements could added to the current code*/
check(L) : not(Vcount = 0)
        <- !goTo(L).
check(L) : not (C = 0) & not (NC = 0)
        <- !gohome.

check(L).

+!goto(L) : true
        <- ?at(L);
            moveTo(X,Y);
            !at(L);
            !requestVictimStatus(x, y, colour);
            
+!requestVictimStatus
        <- !rescue(x,y);

+!rescue(x,y) : critical(x,y)
        <- moveTo(0,0);
            -location(victim,_,_).

+!rescue(x, y) : ~critical(x, y) & C = 0
        <- moveTo(0,0);
            -location(victim,_,_).

+!rescue(x, y) : ~critical(x, y) & not (C = 0)
        <- +~critical(x, y);
            -location(victim,_,_);
            !rescue2(x,y).

+!rescue(x, y).

+!rescue2(x, y) : ~critical(x, y) & not (NC = 0)
        <- moveTo(0,0).
            -location(victim,_,_).

////////////////////////////////////////////////////////////////////////////////////////////////



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





+!intention(X,Y) : critical(X,Y)
    <-  .print("Status: critical, intention: rescue");
    	!rescue(X,Y).                             
    
+!intention(X,Y) : ~critical(X,Y) & not allCriticalRescued
    <-  .print("Status: noncritical, not all critical rescued. Intention: will come back.");
    	+toBeRescued(X,Y);
    	-location(victim,X,Y)[source(doctor)].
    
+!intention(X,Y) : ~critical(X,Y) & allCriticalRescued
    <-  .print("Status: noncritical, all critical rescued. Intention: rescue");
    	!rescue(X,Y).       





       
+!rescue(X,Y) : critical(X,Y) 
    <-  ?criticalCount(C);
    	NewCount = C - 1;
    	+criticalCount(NewCount);
    	-criticalCount(C);
    	+carrying(victim);                                                              
        -location(victim,X,Y)[source(doctor)];                                                     
        goHospital;
        // Add target percept  
        !at;                                                          	
        -carrying(victim);                                                          	
        +rescued(X,Y). 
+!rescue(X,Y) : toBeRescued(X,Y) 
    <-  +carrying(victim);                                                              
        -toBeRescued(X,Y);                                                     
        goHospital;      
        // Add target percept 
        !at;                                                     
        -carrying(victim);                                                              
        +rescued(X,Y).           
+!rescue(X,Y) : true 
    <-  +carrying(victim);                                                              
        -location(victim,X,Y)[source(doctor)];                                                     
        goHospital;      
        // Add target percept
        !at;                                                      
        -carrying(victim);                                                              
        +rescued(X,Y).      
        
+!at(L) : at(L).

+!at
	<-	?location(L, X, Y);
		moveTo(X, Y);
		!at(L).
    
