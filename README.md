# finance-app

This project is android mobile app to track all your expensive. I done this project to learn how to use cloud databases and other stuff within app. 
App is implemented with firestore cloud system, so if you want to launch this app, yout have to configurate it [instructions from Firebase](https://firebase.google.com/docs/android/setup).

You have to setup security rules to make app work correctly. There are my security rules:
```markdown
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users {
      allow read: if request.auth!=null;
      allow create: if request.resource.__name__ == request.auth.uid;
      match /{email}{
      	allow read, update: if request.auth!=null;
      	allow write: if email == request.auth.token.email;
        match /expenses{
        	allow read: if email == request.auth.token.email || email in get(/databases/$(database)/documents/users/$(request.auth.token.email)).data.friendList;
          allow write: if email == request.auth.token.email;
          match /{document}{
          	allow read: if email == request.auth.token.email || email in get(/databases/$(database)/documents/users/$(request.auth.token.email)).data.friendList;
          	allow write: 	if email == request.auth.token.email;
          }
        }
      }
    }
  }
}
'''
