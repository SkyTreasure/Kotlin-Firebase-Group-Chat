// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });


// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database. 
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
 

exports.sendGroupCreatedNotification = functions.database.ref('/group/{groupId}').onCreate(event =>{
	 const snapshot = event.data;
	 const root = event.data.ref.root;
	 var groupID = event.params.groupId;

	 var memberPromises = [];
	 var tokensFCM=[];

	 var members = snapshot.val().members; 

	 Object.keys(members).forEach(function(key){
	 		//console.log('Keys-',key);
	 		var memberId=key; 
	 		var mPromise = root.child(`/users/${memberId}`).once('value');
	 		memberPromises.push(mPromise);
	 });
	 

	  var newP = Promise.all(memberPromises).then( results => {
	  		 /*console.log('Results Length> ',results.length);
	  		 console.log('Item1>',results[0].val().deviceIds);*/

	  		 var tokens = results.map(result=>{

				var dId=result.val().deviceIds;
				//console.log('Did',dId);

				Object.keys(dId).forEach(function(key){ 
					//console.log("DId value",dId[key]);
					tokensFCM.push(dId[key]);
					 
	  		 	});
	  		 });
 
	  		 return tokensFCM;
	  });

	  return newP.then(tokens =>{
	  		//console.log('Tokens',tokensFCM);
	  		sendGroupInitSilentNotification(tokensFCM, groupID);
	  });

});


exports.sendChatNotifications = functions.database.ref('/group/{groupId}/lastMessage').onWrite(event =>{
	 const snapshot = event.data;
 	 const root = event.data.ref.root;
	 var groupID = event.params.groupId;
	 var senderID = snapshot.val().sender_id;
	 var message = snapshot.val().message;

	 var senderPromise = root.child(`/users/${senderID}`).once('value');
	 var groupPromise = root.child(`/group/${groupID}`).once('value');

	 return newPromise = Promise.all([senderPromise,groupPromise]).then( results =>{
	 	var senderData=results[0].val();
	 	var groupData = results[1].val();
	 	console.log('Sender Data > ',senderData);
	 	console.log('Group Data > ',groupData);
	 	console.log('Message > ',message);
	 	sendChatFCMnotification(senderData,groupData,message);
	 });
});


function sendChatFCMnotification(sender,group,message){
	if(group.group){
		//Group chat
		console.log('Notification Type >','Group Chat');
		const payload = {
		  data:{
		  	title: group.name,
		  	image: group.image_url,
		  	type: 'group_chat',
		  	group_id: group.groupId,
		  	body : message,
		  	sender: sender.uid,
		  	sender_name : sender.name,
		  	sender_image : sender.image_url
		  }
		};
		var topic =group.groupId;
		console.log('Topic Name >',topic);

		admin.messaging().sendToTopic(topic, payload)
			  .then(function(response) { 
			    console.log("Successfully sent message to group : "+ group.name , response);
			  })
			  .catch(function(error) {
			    console.log("Error sending message to group : "+ group.name, error);
			  });


	}else{
		//One On One Chat
		console.log('Notification Type >','One on One Chat'); 
		const payload = {
		  data:{
		  	title: group.name,
		  	image: group.image_url,
		  	type: 'one_on_one_chat',
		  	group_id: group.groupId,
		  	body : message,
		  	sender: sender.uid,
		  	sender_name : sender.name,
		  	sender_image : sender.image_url
		  }
		};
		var topic =group.groupId;
		console.log('Topic Name >',topic);

		admin.messaging().sendToTopic(topic, payload)
			  .then(function(response) { 
			    console.log("Successfully sent message to OneOnOneChat : "+ group.groupId , response);
			  })
			  .catch(function(error) {
			    console.log("Error sending message to OneOnOneChat : "+ group.groupId, error);
			  });
	}
}

/**
* This function sends notification to all users in the group and app will make them subscribe
* to the group ID. This method will be triggered only when new group is created.
* 
*/
function sendGroupInitSilentNotification(tokens, groupid){
	console.log('groupid >',groupid);
	console.log('tokens >', tokens);

	const payload = {
	  data:{
	  	title: 'New Group has been created',
	  	type: 'follow_group',
	  	group_id: groupid,
	  	body : 'Tap to check'
	  }
	};

	admin.messaging().sendToDevice(tokens, payload).then(response => {
        // For each message check if there was an error.
        const tokensToRemove = [];
        console.log('FCM',response);
        response.results.forEach((result, index) => {
          const error = result.error;
          if (error) {
            console.error('Failure sending notification to', tokens[index], error);
            // Cleanup the tokens who are not registered anymore.
            if (error.code === 'messaging/invalid-registration-token' ||
                error.code === 'messaging/registration-token-not-registered') {
            	//TODO: Need to work on this
              //tokensToRemove.push(allTokens.ref.child(tokens[index]).remove());
            }
          }else{
          	/*console.log('FCM','Group Creation notification sent successfully');
          	console.log('FCM',result);*/
          }
          return Promise.all(tokensToRemove);
        });
	});
}





// Sends a notifications to all users when a new message is posted.
/*exports.sendGroupCreationNotification = functions.database.ref('/groups/{groupId}').onCreate(event => {
  const snapshot = event.data;


   

   for (var entry of snapshot.val().members.entries()) {
    	var memberId = entry[0],
        var member = entry[1];
    	
    	
    	return admin.database().ref('/users/'+memberId).once('value').then( member =>{
    		if(member.val()){
    			return member.val().name
    		}
    	});
	}	

  // Notification details.
  const text = snapshot.val().text;
  const payload = {
    notification: {
      title: `${snapshot.val().name} posted ${text ? 'a message' : 'an image'}`,
      body: text ? (text.length <= 100 ? text : text.substring(0, 97) + '...') : '',
      icon: snapshot.val().photoUrl || '/images/profile_placeholder.png',
      click_action: `https://${functions.config().firebase.authDomain}`
    }
  };

  // Get the list of device tokens.
  return admin.database().ref('fcmTokens').once('value').then(allTokens => {
    if (allTokens.val()) {
      // Listing all tokens.
      const tokens = Object.keys(allTokens.val());

      // Send notifications to all tokens.
      return admin.messaging().sendToDevice(tokens, payload).then(response => {
        // For each message check if there was an error.
        const tokensToRemove = [];
        response.results.forEach((result, index) => {
          const error = result.error;
          if (error) {
            console.error('Failure sending notification to', tokens[index], error);
            // Cleanup the tokens who are not registered anymore.
            if (error.code === 'messaging/invalid-registration-token' ||
                error.code === 'messaging/registration-token-not-registered') {
              tokensToRemove.push(allTokens.ref.child(tokens[index]).remove());
            }
          }
        });
        return Promise.all(tokensToRemove);
      });
    }
  });
});    */