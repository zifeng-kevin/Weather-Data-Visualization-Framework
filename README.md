# ReadMe
## Overview
Our framework provides a weather data visualization framework. The visualization includes both the raw data from data plugin and more advanced indices. Indices (such as clothing index, sport index, etc.) are drived by the framework based on the raw data, which explains the raw data from different aspects. The display plugin can display any information included in the raw data and weather indices.

## Important Data Structures & Concepts
Here are some important data structures and comcepts.

### `WeatherRecord`	
It represents a collection of weather data at a point of time. 

### `CityInfo`
Object to represent information about a city.

### `RawWeatherData`
It represents all raw weather data of a city. It includes a `CityInfo` Object and many `WeatherRecord` Objects, where `WeatherRecord` Objects are sorted in chronological order, each of them has a corresponding timestamp.

###  `ProcessedWeatherData`
Object to represent the processed data. It includes raw weather data and the generated weather indices.

### Tool Object
#### `State`
Represent all states in U.S.A. This is useful to proved an tool to choose a state for a city.
#### `WeatherState`
Represent  more than 30 different weather states
#### `WeatherMetric`
Represent different weather metrics, such as wind speed, temperature, etc..
#### `InputEntry`	
Represent the user entry on GUI. Currently we extends 4 concrete entry class: 
- `TextInputEntry` allow plain text from user.
- `PathEntry` allow user to select a path in a prompt window.
- `SelectOneEntry` allow user to select one from plugin provided options.
- `SelectMultipleEntry` allow user to select multiple from 
plugin provided options.

### Plugin Interface
To implement a data or display plugin, developer needs to inplements the interface `DataPlugin` or `DisplayPlugin` respectively. There are some important methods need to be impelemented:

#### `DataPlugin`


- `getName()` should return the name of this data plugin as a string.
-  `getInputEntries()`. Called by the framework, so that the framework can know what input entries the plugin want. It returns a list contains all the input entries.
- (Optional)`onRegister(WeatherIndexFrameworkData framework)`. Gives information to plugin of the framework
- `loadData(List<InputEntry> inputEntries)`. This method is called by the framework to get the formatted data. And the user input information would be stored in `inputEntries` for the developer to parse and use it to generate data. The return type is a list of `RawWeatherData`, where each object in the list represents information for one city.

#### `DisplayPlugin`
- `getName()`, `getInputEntries()`, `onRegister(WeatherIndexFrameworkData framework)`. Same meaning as `DataPlugin`.
- `generateGraph(List<InputEntry> inputEntries, List<ProcessedWeatherData> data)`: This method is called by the framework to get the generated graph from given data and user input. data parameter is a list of `ProcessedWeatherData`, each of which represents data form one city.

## How to play?
- After downloading this tool, launch it with **gradle run** and you should see the following user interface:![enter image description here](https://s1.ax1x.com/2020/04/10/GoE7qO.png)

- Then **Select a Data plugin**:![enter image description here](https://s1.ax1x.com/2020/04/10/GoVAij.png)

- As an example, this shows the interface after selecting **12HourFuturePredict** as our data plugin:![enter image description here](https://s1.ax1x.com/2020/04/10/GoV0Te.png)
- If you put the mouse on this icon for about 1 second, the help message for this input field will show up.
![enter image description here](https://s1.ax1x.com/2020/04/10/GoVclt.png)
- After inputing and selecting all the required filed, you can click the Load Button to get the data.
![enter image description here](https://s1.ax1x.com/2020/04/10/GoVb60.png)

- After getting the data from the data plugin, you should then select a display plugin:
![enter image description here](https://s1.ax1x.com/2020/04/10/GoZmhd.png)

- For display plugin, you should follow the same procedures as for data plugin to input or select data. After choosing Generate button the graph would show in the middle.
![enter image description here](https://s1.ax1x.com/2020/04/10/Goe9Ug.png)

** Note**: because of the Mac/Windows platform issue, the Question mark icon is not displayed successfully: here is the interface on Windows  platform:
![enter image description here](https://s1.ax1x.com/2020/04/10/GoeF8s.png)



## How to add new plugin?
When the developer develops a new plugin. Add the plugin to the resources/META-INF.services, for example:
	
	edu.cmu.cs.cs214.hw5.plugin.StickChartDisplayPlugin

## Aside: how to get API key for `WebAPI12HFutureDataPlugin`?
- Create an accout on `[https://developer.accuweather.com/](https://developer.accuweather.com/)`.
- Create a new app and select free tier (which has 50 api requests per day).
- Put the api key in `WebAPI12HFutureDataPlugin.java`.
