# Team
Rahul Prabhu

Sanil Sinai Borkar


# Compiling
SBT is used to build the code. To compile, just run the following command from the project root direcory
```
$ sbt compile
```

# Running
To run the program, type 'sbt run' at the command prompt to run through the 'sbt' command-prompt.
```
$ sbt run <number_of_users>
```

# Command Line Arguments
There are no command-line arguments.

# API
The program emulates the Facebook API. For this, the data structures and API that are used by Facebook were considered. The APIs that we were implemented are:
* *CreateProfile* - Create a profile for the current user
* *GetProfile* - User can retrieve his own profile or that of another user
* *UpdateProfile* - Update the profile of the current user
* *DeleteProfile* - Delete the profile of the current user
* *CreateStatus* - User can post status
* *UpdateStatus* - User can update the status that he had already posted. If the status is not present, the error message "" is displayed to the user.
* *DeleteStatus* - User can delete the status that he had already posted. If the status is not present, the error message "" is displayed to the user.
* *CreateAlbum* - User can create an album
* *GetAlbum* - User can retrieve his own album or that of another user
* *UpdateAlbum* - User can update the details of an album that he had created.
* *DeleteAlbum* - User can delete an album
* *UploadPhoto* - User can upload a photo to an album that he had created. If the album does no belong to the user, the error message "*<user_id> is not allowed to post to album <album_id>*".
* *GetPhoto* - User can retrieve his own photo or that of another user
* *DeletePhoto* - User can delete a photo
* *CreateComment* - User can comment on any statuses, photos and albums owned by any user.
* *UpdateComment* - User can update an already posted comment
* *DeleteComment* - User can delete a comment
* *GetPage* - User can retrieve his own page or that of another user
* *UpdatePage* - User can update the details of a page that he had created.
* *DeletePage* - User can delete a page
* *AddFriend* - User can send a friend request to another user

The program simulates Facebook user behavior. The following gives the behavior of users in our Facebook simulator.
```
30% of users create albums
100% of all users update their statuses
20% of users upload photos
100% of all users send friend request to at least one other person
20% of users comment on stuff
10% of users comment on stuff
50% of users delete their own comments
1% of users delete their own comment
1% of all users see albums that do not exist
10% of all users see other's albums
2% of users see other's photos that do not exist
20% of users see other's photos
2% of all users see other's profiles that do not exist
10% of users see their own profiles
5% of users see other's profiles
10% of users update their profiles regularly
2% of users delete their profiles
100% of all users update their statuses
2% users delete invalid status
5% of users update their statuses 
5% of users create pages
100% of all users see pages
1% of users delete pages

```

The requests and the respective responses can be seen on the terminal. A sample run of the program with 10 users gives the following:
```
$ sbt "run 10"
[info] Running Main 10
[INFO] [11/30/2015 23:34:15.773] [facebook-akka.actor.default-dispatcher-4] [akka://facebook/user/handler] Building get route
Server starting up..
[INFO] [11/30/2015 23:34:16.369] [facebook-akka.actor.default-dispatcher-2] [akka://facebook/user/IO-HTTP/listener-0] Bound to localhost/127.0.0.1:8080
[INFO] [11/30/2015 23:34:16.371] [facebook-akka.actor.default-dispatcher-3] [akka://facebook/deadLetters] Message [akka.io.Tcp$Bound] from Actor[akka://facebook/user/IO-HTTP/listener-0#-886184272] to Actor[akka://facebook/deadLetters] was not delivered. [1] dead letters encountered. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
Server up!
User 9 creating a profile
User 6 creating a profile
User 1 creating a profile
User 5 creating a profile
User 2 creating a profile
User 3 creating a profile
User 0 creating a profile
User 8 creating a profile
User 7 creating a profile
User 4 creating a profile
-> PROFILE created with id = 1
-> PROFILE created with id = 2
-> PROFILE created with id = 7
-> PROFILE created with id = 0
-> PROFILE created with id = 4
-> PROFILE created with id = 5
-> PROFILE created with id = 8
-> PROFILE created with id = 6
-> PROFILE created with id = 9
-> PROFILE created with id = 3

Profiles created!
User 0 creating an Album..
User 6 creating an Album..
User 3 creating an Album..
User 9 creating an Album..
User 0 creating status "This is User 0's status"
User 0 uploading photo with 1.png to album 1
-> User 9: Album 1 added to objectCommentsMap
-> User 9: Album created with ID = 1
-> User 3: Album 2 added to objectCommentsMap
User 1 creating status "This is User 1's status"
User 2 creating status "This is User 2's status"
-> User 3: Album created with ID = 2
User 3 creating status "This is User 3's status"
User 6 creating status "This is User 6's status"
User 7 creating status "This is User 7's status"
User 4 creating status "This is User 4's status"
User 2 sending friend request to 1
User 1 sending friend request to 0
User 6 sending friend request to 5
User 4 sending friend request to 3
User 2 is changing his status to "User 2 is changing his status"
User 4 is changing his status to "User 4 is changing his status"
User 7 sending friend request to 6
-> User 6: Album 3 added to objectCommentsMap
User 5 creating status "This is User 5's status"
User 3 sending friend request to 2
User 5 uploading photo with 1.png to album 1
User 7 is changing his status to "User 7 is changing his status"
User 6 is changing his status to "User 6 is changing his status"
User 1 is changing his status to "User 1 is changing his status"
User 9 creating status "This is User 9's status"
User 8 creating status "This is User 8's status"
User 8 sending friend request to 7
User 3 is changing his status to "User 3 is changing his status"
User 8 is changing his status to "User 8 is changing his status"
-> User 6: Album created with ID = 3
-> User 0: Album 4 added to objectCommentsMap
-> User 0: Album created with ID = 4
User 9 sending friend request to 8
User 9 is changing his status to "User 9 is changing his status"
-> User 0: Status4 added to objectCommentsMap
-> Status0:Status Changed= This is User 0's status
-> User 1: Status4 added to objectCommentsMap
-> Status1:Status Changed= This is User 1's status
-> User 2: Status4 added to objectCommentsMap
-> Status2:Status Changed= This is User 2's status
-> User 6: Status4 added to objectCommentsMap
-> Status6:Status Changed= This is User 6's status
-> User 3: Status4 added to objectCommentsMap
-> Status3:Status Changed= This is User 3's status
-> User 4: Status4 added to objectCommentsMap
-> Status4:Status Changed= This is User 4's status
2 is friends with 1
6 is friends with 5
1 is friends with 0
4 is friends with 3
-> User 7: Status4 added to objectCommentsMap
-> Status7:Status Changed= This is User 7's status
2 is not allowed to update statuses of 0
4 is not allowed to update statuses of 0
7 is friends with 6
-> User 5: Status4 added to objectCommentsMap
-> Status5:Status Changed= This is User 5's status
3 is friends with 2
6 is not allowed to update statuses of 0
7 is not allowed to update statuses of 0
-> User 9: Status4 added to objectCommentsMap
-> Status9:Status Changed= This is User 9's status
1 is not allowed to update statuses of 0
-> User 8: Status4 added to objectCommentsMap
-> Status8:Status Changed= This is User 8's status
8 is friends with 7
3 is not allowed to update statuses of 0
8 is not allowed to update statuses of 0
9 is friends with 8
9 is not allowed to update statuses of 0
User 5 sending friend request to 4
User 5 commenting on Object 1
User 5 Getting a Photo with id = 1
User 0 uploading photo with 2.png to album 1
Got a post for photos
5 is not allowed to post photos to album1
5 is friends with 4
User 0 uploading photo with 3.png to album 1
Got a post for photos
0 is not allowed to post photos to album1
GET request received for id 1
The requested photo cannot be found.
The requested photo cannot be found.
User 5 is changing his status to "User 5 is changing his status"
Got a post for photos
0 is not allowed to post photos to album1
5 is not allowed to update statuses of 0
User 0 sending friend request to -1
User 0 commenting on Object 1
User 0 updating comment 1 on Object 1
User 0 deleting comment 2
User 0 deleting comment 1
User 0 Getting an Album with id = 189783748374389
The requested user was not found
Got a post for photos
0 is not allowed to post photos to album1
ALBUM: GET request received for id 189783748374389
The requested album cannot be found.
The requested album cannot be found.
User 0 Getting an Album with id = 1
ALBUM: GET request received for id 1
id = 1
count = 0
cover_photo = 33
created_time = 12:23:12
description = description: String
from = 9
link = link: String
location = location: String
name = name: String
place = place: String
privacy = privacy: String
updated_time = 11/30/15 11:34 PM
OCid = 1
Photos = 33	

User 0 Getting a Photo with id = 12379872834
GET request received for id 12379872834
The requested photo cannot be found.
The requested photo cannot be found.
User 0 Getting a Photo with id = 1
GET request received for id 1
The requested photo cannot be found.
The requested photo cannot be found.
User 0 Getting Profile = 1023789748
PROFILE: GET request received for id 1023789748
The requested profile was not found
The requested profile was not found
User 0 Getting Profile = 0
PROFILE: GET request received for id 0
id = 0
bio = bio
birthday = birthday
email = email
first_name = first_name
gender = gender
hometown = hometown
last_name = last_name
link = link
location = location
middle_name = middle_name
political = political
relationship_status = relationship_status
religion = religion
significant_other = significant_other
updated_time = updated_time
website = website
cover = cover

User 0 updating profile
User 0 deleting Profile
User 0 is changing his status to "User 0 is changing his status"
User 0 deleting status 1333987492
User 0 deleting status 1
User 0 deleting Page 0
Profile with id 0 updated!
Status Posted
USER: DELETE request received for id = 0
Profile with id = 0 was deleted!
Status: DELETE request received for del_id = 1333987492
Requested status not found
Status: DELETE request received for del_id = 1
Status 1 deleted
PAGE: DELETE request received for id = 0
Page with id = 0 was not found

```

The program was run for a maximum of 100K users.
