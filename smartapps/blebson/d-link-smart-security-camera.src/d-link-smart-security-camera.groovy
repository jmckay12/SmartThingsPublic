/**
 *  D-Link Smart Security Camera
 *  Version 1.0.0
 *  Copyright 2016 BLebson
 *  Based on Photo Burst When... Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Photo Burst When...
 *
 *  Author: SmartThings
 *
 *  Date: 2013-09-30
 */

definition(
    name: "D-Link Smart Security Camera",
    namespace: "blebson",
    author: "Ben Lebson",
    description: "Move to preset position and take a burst of photos and send a push notification when...",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/photo-burst-when.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/photo-burst-when@2x.png"
)

preferences {
	section("Choose one or more, when..."){
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
	}
	section("Choose camera to use") {
		input "camera", "capability.imageCapture", description: "NOTE: Currently only compatable with DCS-5020L Device made by BLebson"		
	}
	section("Choose which preset camera position to move to"){
	  input "position", "string", defaultValue: 1 , required: true
	}
	section("Then send this message in a push notification"){
		input "messageText", "text", title: "Message Text"
	}
	section("And as text message to this number (optional)"){
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Phone Number", required: false
        }
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(contact, "contact.open", sendMessage)
	subscribe(acceleration, "acceleration.active", sendMessage)
	subscribe(motion, "motion.active", sendMessage)
	subscribe(mySwitch, "switch.on", sendMessage)
	subscribe(arrivalPresence, "presence.present", sendMessage)
	subscribe(departurePresence, "presence.not present", sendMessage)
}

def sendMessage(evt) {
	log.debug "$evt.name: $evt.value, $messageText"
    camera.deviceNotification(position)
  	takePicture()
    pause(5000)
  	takePicture()
	pause(5000)
    takePicture()

    if (location.contactBookEnabled) {
        sendNotificationToContacts(messageText, recipients)
    }
    else {
        sendPush(messageText)
        if (phone) {
            sendSms(phone, messageText)
        }
    }
}

def takePicture(){
	camera.take()
}