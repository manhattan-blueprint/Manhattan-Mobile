<img src="img/logo.png" width="100px">

 # Blueprint

 Blueprint was created as part of the COMS30400 Group Project unit at the University of Bristol.</br>
 This repository holds the *mobile* application used for playing Blueprint.

 ## Game Outline
 It is a game inspired by Minecraft and Pokémon Go, where the player finds themselves stranded on a distant planet, but discovers blueprints for building a communication beacon. 
 In order to return home safely, they must build a communication beacon and signal for help using components and machines built from the discovered blueprints. 
 However, there is a catch! The raw resources required to progress through the game cannot be collected on the desktop app - the player must log off and venture outside into the real world with the mobile app, and Hololens if available where they are presented with a map showing nearby raw resources the player can collect. 
 Once the player is close enough to a resource, it will enable an Augmented Reality game to collect some quantity of the resource, determined by the players performance in the game.
 Utilising the cloud, we synchronise the entire game remotely - allowing the player to seamlessly pick up where the left off, from any internet connected device.
 The end goal is simple: build each of the blueprints one by one until you have the resources to rebuild the communication beacon. 

 ## Technology Outline
 - A fully native Android application built using Java
 - Integration of MapBox SDK for displaying a custom map showing real-time user location
 - Utilisation of ARCore to provide an interactive mini-game when collecting resources, with support for older devices
 - The server for a bespoke communication pattern between the Microsoft Hololens and an internet-connected Android device.
 - Custom UI design with animations

 ## Screenshots
<img src="img/map.png" width="300px">
<img src="img/ar.png" width="300px">
<img src="img/non-ar.png" width="300px">