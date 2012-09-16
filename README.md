gps_tracker
===========

View GPS data: geo coordinates, altitude and accuracy. Can keep tracking of the done route for further analyze.

     This is an application for Android to watch and track GPS data. Initially
it was created to get the current altitude above sea level that is helpful during 
mountain trips. Next the program was extended to track the passed travel route for
furthen analyze. There is a way to insert the tracked data to Google Map to
see or share with friend the passed travel route. The current release shows 
the such information as: latitude, longitude, altitude and detected accuracy. 

Once the button "Set elevation start" has been clicked the program appends
GPS point to a file on SD card. The file name is depends on the current data-time 
and composed using a pattern: HH_mm_ss_yyyy_MM_dd.txt, where HH -hours, mm -minutes, 
ss-seconds, yyyy- year, MM-month and dd-day. For example: 16_29_26_2012_08_09.txt

As soon as new GPS data is available the program send them to the file in the 
such order:
Milli seconds timestamp; latitude; longitude; altitude; accuracy; bearing; speed;
i.e. 1344522949506;38.85288574;69.00157641;2363.10009765625;7.0;93.1;0.5;

There is also an information on the screen about the altitude of the GPS point 
when the button "Set elevation start" was clicked and the difference between 
the current point and initial point. I found it useful in mountaineering to know 
how many meters you get above the start point.
 
