rentahome
=========

This is a demo application that illustrates use of Windows Azure Mobile Services on four mobile platforms: Android, iOS, Windows Phone 8, and Windows 8.

Rent-a-Home is an application that helps users find apartments for rent and insert new apartments. It can also display apartments on a map. The application supports login (authentication) with Twitter, so apartments can be associated with the user that added them.

Some additional references mentioned during the presentation:

* [Azure Mobile Services component](http://components.xamarin.com/view/azure-mobile-services/) in the Xamarin Store.
* [Parse Pricing Model](https://parse.com/plans).
* [Azure Service Bus Notification Hubs](http://msdn.microsoft.com/en-us/library/windowsazure/jj927170.aspx) -- scalable push notification support, coming soon to Azure Mobile Services as well.
* [Nadlandroid](https://code.google.com/p/nadlandroid/) -- an application illustrating some of the Parse features that are not available in Windows Azure Mobile Services.

Finally, to reproduce this demo with your own mobile service, you'll need two tables: `channels` and `apartment`, with the server scripts described in the server-scripts.md file.