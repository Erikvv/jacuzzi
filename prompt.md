Please write a program that turns on the jacuzzi heater when electricity prices are low.

Electricity prices
===

The electricity prices can be fetched from https://api.anwb.nl/energy/energy-services/v2/tarieven/electricity?startDate=2026-06-13T22:00:00.000Z&endDate=2026-06-15T08:12:45.088Z&interval=HOUR
The prices of the next day should be available after 13:00 local time (europe/amsterdam).

Heater
===

The heater has its own thermostat so we don't have to worry about overheating.

Enabling the heater is done by a fingerbot which presses a switch which toggles
the heater on or off.

We can't read the state of the heater directly.
A small file in this project should track the state of the heater.

This MQTT message makes the fingerbot press the switch: {"topic":"0xa4c138c7cb3eebc5/set","payload":{"state":"ON"}}
If it is successfull the following message is received back: topic 'zigbee2mqtt/jacuzzi_fingerbot', payload '{"arm_end_position":24,"arm_start_position":1,"auto_adjustment":"idle","battery":85,"button_hold_duration":0.3,"linkquality":193,"mode":"button","state":"ON","switch_states":"idle"}'

The MQTT broker is running at odroid.local. The user is jacuzzi and the password is in secrets.env.

The heater needs about 8 hours of uptime per day to keep the jacuzzi at temperature.

Implementation
===

The program should be written in Kotlin and reside in ./app. 
Use the built-in java http client.
Install an MQTT client using gradle

The program should fetch the prices once every day after at 14:00, 
or immediately when starting up.

It should create a schedule where the heater is enabled for the cheapest 10 hours.
It should log all relevant decisions.

I think the best way is to run the logic in a loop with a 10 minute sleep,
but you can deviate if you have a better idea.

You can run the code any time you want.


