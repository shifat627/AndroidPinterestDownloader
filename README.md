AI prompts
----------


You Are are Senior Android Developer
======================================

Consider Youself a senior android devloper.
Now Create A Navigation Controller with 4 routes.

1.Input Download Link Screen
2.Downloader Sceen
3.Settings Screen
4.List Completed Download List

Then Create a viewmodel , where it will hold list of all links and a downloader function (no need to code the downloader for now. I will tell you later how to code it later)

Code Screens and viewmodel in a seperate files

==========================================================

Now Create a navbar and bottom naviagtion bar with 3 item with your prefered icons.
Link them to DownloadListScreen,InputScreen,SettingsScreen
==================================================================
In Settings Path Create 3 text input , and 1 Directory and 1 save button
 selection.
one text input is video regex , which's default value is 
https:\/\/[\d\w.]+\/videos\/([\d\w]+\/)+[\d\w]+.mp4

One text input is Step 1 image regex,which is
"image":"(https:\/\/[\d\w.]+\/originals\/([\d\w]+\/)+[\d\w]+.[a-z]{3})"

one text imput is , Step 2 image regex
(https:\/\/[\d\w.]+\/originals\/([\d\w]+\/)+[\d\w]+.[a-z]{3})

Save button will Save them in shared preference
==========================================================================

In DownloadListScreen , a Link input field and a add button and a downlaod button required.Download button will  navigate to Downloader Screen.
 
When add button is clicked , it will add the link to viewmodel.
All links will be listed below the button.
double tapping on a link in list will remove the link from viewmodel
================================================================================
In Downloader Screen , It will download all the links one by one using the downloader function in viewmodel . For Each of the link,there will be a progess bar and progess bar in notification.

if any of the link's download failed move to next.


As for how The downloader will download,
first get content of the link.
then extract match with video regex
if match is found , use the matched link to download the video.
if not found , then use step 1 image regex to extract match
if match found , use the step 2 image regex to extract match
if found , use that link to download image
if not just say no link found

As for how downloaderScreen link will display all link in a list,
------------
Pinterest Link 1
file name(extract using regex) or No link found (if no match)
<Progress> or error (if occured)
----------------
Pinterest Link N
file name(extract using regex) or No link found (if no match)
<Progess> or error (if occured)

code progress bar and progress in notification yourself

=======================================================
The Downloaded files will saved to directory selected in settings.
Also I made a mistake.

In InputScreen , a Link input field and a add button and a downlaod button required.Download button will  navigate to Downloader Screen.
 
When add button is clicked , it will add the link to viewmodel.
All links will be listed below the button.
double tapping on a link in list will remove the link from viewmodel

In DownloadListScreen It Will list all files in select directory in settings.Double tapping any item of The list will open the item

=======================================================================

Now Style and beautify All the Material Componetents and add animiations , as your like

======================================================================
Can You Design Launcher Icon?

============================================================
Since It is a pinterest downloader , make a icon that will make people think it is a pinterest downloader

==============================================================
fixed some error
=============================================================
Create a Deeplink that use link taken from browser,
The deeplink will add the link to viewmodel , then navigate to downloaderScreen
===========================================================
In DownloaderScreen Add a button that will clear completed downloads and a add button that will return to InputScreen.
================================================================
Re-design the laucher icon . a 'P' letter with a tiny download icon at corner 
=================================================================
the icon is too large
=====================================================
<Some modification by me>
=========================================================
The Deeplink keeps creating new instance of app instead of using existing one
