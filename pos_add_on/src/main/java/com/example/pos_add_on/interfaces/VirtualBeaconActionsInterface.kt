package com.example.pos_add_on.interfaces

interface VirtualBeaconActionsInterface {
    fun virtualBeaconManagerDidEncounterError(title:String, message:String)
    fun virtualBeaconManagerDidStartAdvertising()
}