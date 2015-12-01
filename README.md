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

The program simulates real-world Facebook behavior. The requests and the respective responses can be seen on the terminal. A sample run of the program gives the following:
```

```

The program was run for a maximum of 100K users.