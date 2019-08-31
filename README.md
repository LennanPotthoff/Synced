# Synced - A server based application for file transfer build for android.

## Description
Synced is an application build in Java (both the client and desktop server) that can be used to sync files from the connected PC, to the respective phone. This application uses the TCP protocol for the file transfer, and UDP for the "wake message" when the connection is started.

## Use
First download and install the "Synced" APK from either the Google Play Store, or alternatively from this repository. 
Google Play link here....

After starting up Synced on your android device, download and run the jar file "SyncedServer". The small desktop symbol will be displayed in your taskbar.

![Untitled](https://user-images.githubusercontent.com/47326518/64069272-24443800-cc3e-11e9-849f-48341f3b82ed.png)

Right click on the symbol to display your machine's local IP.

![Untitl7ed](https://user-images.githubusercontent.com/47326518/64069270-20181a80-cc3e-11e9-9549-7d313e42e858.png)

Use this IP to connect to your pc from your android and start tranferring files! To transfer a desired file, simply click on on it in the displayed listview and follow the displayed dialog's instructions.

![syncedhomescreen](https://user-images.githubusercontent.com/47326518/64069835-7ee39100-cc4a-11e9-9e19-c3e81c7ab079.png)

NOTE! - Currently concurrent transfers aren't supported. Thus, when clicking on an additional file whilst a tranfer is still ongoing, it will ask you if you wish to cancel the current one. Concurrent transfers is something I hope to add in the near future.

