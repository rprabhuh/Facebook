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

# Architecture
The program emulates the Facebook API. For this, the data structures and API that are used by Facebook were considered. The APIs that we were implemented are:
* *CreateProfile*
* *UpdateProfile*
* *DeleteProfile*
* *CreateStatus*
* *UpdateStatus*
* *DeleteStatus*
* *CreateAlbum*
* *UpdateAlbum*
* *DeleteAlbum*
* *CreatePhoto*
* *DeletePhoto*
* *CreateComment*
* *DeleteComment*
* *AddFriend*

The program simulates real-world Facebook behavior. The requests and the respective responses can be seen on the terminal. A sample run of the program gives the following:
```
[INFO] [11/30/2015 18:58:44.535] [facebook-akka.actor.default-dispatcher-2] [akka://facebook/user/handler] Building get route
User 4 creating a profile
User 0 creating a profile
User 7 creating a profile
User 8 creating a profile
User 3 creating a profile
User 1 creating a profile
User 9 creating a profile
User 5 creating a profile
User 6 creating a profile
User 2 creating a profile
Got a post for photos
User 10 creating a profile
User 13 creating a profile
Creating an Album..
Getting an Album..
User 14 creating a profile
User 15 creating a profile
User 17 creating a profile
User 18 creating a profile
User 19 creating a profile
User 11 creating a profile
Creating an Album..
User 12 creating a profile
Getting an Album..
User 16 creating a profile
User 0 commenting on Object 1
[INFO] [11/30/2015 18:58:45.768] [facebook-akka.actor.default-dispatcher-7] [akka://facebook/user/IO-HTTP/listener-0] Bound to localhost/127.0.0.1:8080
[INFO] [11/30/2015 18:58:45.774] [facebook-akka.actor.default-dispatcher-2] [akka://facebook/deadLetters] Message [akka.io.Tcp$Bound] from Actor[akka://facebook/user/IO-HTTP/listener-0#-784041573] to Actor[akka://facebook/deadLetters] was not delivered. [1] dead letters encountered. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
-> PROFILE created with id = 4
-> PROFILE created with id = 5
-> PROFILE created with id = 6
-> PROFILE created with id = 0
-> PROFILE created with id = 1
-> PROFILE created with id = 7
-> PROFILE created with id = 9
-> PROFILE created with id = 13
-> PROFILE created with id = 2
-> PROFILE created with id = 3
-> PROFILE created with id = 14
-> PROFILE created with id = 15
-> PROFILE created with id = 17
-> PROFILE created with id = 18
-> PROFILE created with id = 19
-> PROFILE created with id = 10
-> PROFILE created with id = 11
-> PROFILE created with id = 8
-> PROFILE created with id = 16
ALBUM: GET request received for id 189
ALBUM: GET request received for id 1
-> User 0: Album 1 added to objectCommentsMap
ERROR: The requested album cannot be found.
ERROR: The requested album cannot be found.
Getting a Photo..
-> User 0: Album created with ID = 1
-> User from: Page 1 added to objectCommentsMap
Photo with id 1 Uploaded!
-> PROFILE created with id = 12
-> User from: Page 2 added to objectCommentsMap
Photo with id 2 Uploaded!
-> User 10: Album 2 added to objectCommentsMap
-> User 10: Album created with ID = 2
0 is friends with 0
-> User from: Page 3 added to objectCommentsMap
Photo with id 3 Uploaded!
GET request received for id 1
id = 1
album = 1
created_time = 11/30/15 6:58 PM
from = from
link = link
name = 1.png
updated_time = 11/30/15 6:58 PM
place = place
OCid = 2


Getting a Photo..
GET request received for id 123
ERROR: The requested photo cannot be found.
Getting a Profile..
PROFILE: GET request received for id 10
id = 10
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

Getting a Profile..
PROFILE: GET request received for id 1023
ERROR: The requested profile was not found
```
