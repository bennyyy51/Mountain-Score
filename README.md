# Mountain-Score
provides a mountain score (mScore) based on a set provided coordinates

1: download the project, make sure Peak.java, Score.java, and Input.java are in the same package

2: run Input.java and input your numbered latitude/longitude coordinates

why use mScore? mScore is great for finding the caliber of mountains in a certain area. if you want to compare mountain ranges, see the highest mountains in your area, or find your next hike, mScore is for you!

mScore is found by taking both mountain proximity (to provided coordinates) and elevation into account

distance: is converted from lat/lon coordinates to km via the Haversine formula
then, it is raised to the power of (distW) which is 0.9

elevation: is taken and raised to the power of (eleW) which is 2.0

mScore = (ajusted ele / ajusted dist) / 100

tuning parameters (hours of testing has led to these parameters)

  cleaning parameters:
  
    (25.0) removePercentage is the bottom (percentage) of the total peak list to remove, this helps with cleaning tiny hills out of the data
    (33.0) exludePercentage is the top (percentage) of the current peak list that will be allowed to remove peaks from thier close surroundings (lower for more efficient) (only high peaks have sub peaks)
    (20.0) seachPercentage is the top (percentage) of the peak list that the higher peak will search though to exclude (lower for more efficient) (low elevation peaks are not close to high ones)
    (1700.0) distMax is the radius of a circle around a higher peak that will remove lower peaks to avoid irelevant peaks (lower for more efficient)

  visualization parameters:
  
    (100000) radius is the radius in meters from the coordinates that will look for mountains, lowest=10,000, mid=50,000, high=100,000, extreme=500,000
    (10.0) topMountains is the percentage of mountains used for the topmScore which shows the tallest mountains in the area
    (2.0) eleW is the weight used in boosting or decreasing elevation significance in the mScore (elevation^eleW)
    (0.9) distW is the weight used in boosting or decreasing distance significance in the mScore (distane^distW)
