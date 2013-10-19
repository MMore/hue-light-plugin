# hue-light plugin for Jenkins CI


Use the awesome [Philips hue lights](https://www.meethue.com) to show the state of your builds.
The following states are implemented:

* building => blue
* success => green
* fatal errors => red
* no fatal errors ("unstable") => yellow


## Configuration

1. Create a new user (http://developers.meethue.com/gettingstarted.html)
2. Open Global Setting and set the
  * IP address of the hue bridge
  * Authorized username of the hue bridge
3. Create a new job or modify an existing job
  * Add post-build action **Colorize Hue-Light**
  * Set the id of the light you want to control


## License

This plugin has been released under the MIT License. It uses

Copyright (c) 2013 Mathias Nestler

Also included is a copy of the [Jue library](https://github.com/Q42/Jue), licensed under the MIT License too.