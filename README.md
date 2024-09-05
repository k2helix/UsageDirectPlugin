## Usage Direct plugin

This is a plugin for [Smartspacer](https://github.com/KieronQuinn/Smartspacer/) that adds a target and a complication showcasing screen time provided by [usageDirect](https://codeberg.org/fynngodau/usageDirect)

### How it works

Under the hood, the plugin asks Smartspacer to create the widget usageDirect provides. It tries to get the correct dimensions for the widget to show the screen time completely.

Then it refreshes this widget every minute and updates the target/complication accordingly.

### Preview
Target:

![Target preview image](./screenshots/usage_direct_target.png)

Complication:

![Target preview image](./screenshots/usage_direct_complication.png)
