# BlindAssist
This app is my team's submission for AngelHack 2016, created for blind individuals specifically, helping them in their daily lives.

Team
----
* [Adhiraj Singh Chauhan](https://github.com/adhirajsinghchauhan) (me): Android Developer & Designer
* [Bhargav Karanam](https://github.com/bhargavkaranam): Backend developer
* Anirudh Katoch: Android Developer & Designer

Introduction
------------
A blind person faces a lot of difficulties in today's world. One of the first difficulties that comes to our minds relates to traveling around without any help.
Sure, some have guide dogs, but do guide dogs always know where are they going? Can they read out important documents or recognize people?

That's exactly what our app is for.
Features
---------
* It helps them travel by recording the number of steps taken and the usual paths taken by the user.
	If the user moves away from the path he's traveling on, the app makes the phone vibrate, thus informing the user he needs to re-direction himself, using audio cues from individual speakers
* The user, who is wearing `Smart Glasses`, can recognize faces (compared against a continuously populated database), and get their names too.
	However, as a proof of concept, we're using the phone's camera, since we don't have access to a `Smart Glass`. In effect, the `Smart Glasses` would be connected to the phone, and the phone sends `API` calls in accordance to any image it receives from the `Smart Glass`.
* In addition to facial recognition, the user can take a picture of a document and the app reads out the text in it (using `OCR` and `Text to Speech APIs`)
* The user can also find out which company logo is he looking at.
	The use cases for this feature aren't limited to just blind people, as it could be used by just about anyone who wants to know which logo belongs to which company.

Notes
-----
* The app needs to access location information for the map
* Calling `APIs` require an active internet connection
* Any click or touch input will be accompanied by a vibrate too, to notify the user.
* This has been built and compiled for `Android SDK 23 (Marshmallow, 6.0.1)` **ONLY**. Installation of this app on `SDK` version <= 23 will fail.

Known Issues
------------
* App needs to be restarted when the activity goes into a paused state.
	This is because of unregistering the callbacks associated with `SurfaceView`
