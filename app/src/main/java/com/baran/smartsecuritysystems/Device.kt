package com.baran.smartsecuritysystems

data class Device(val id : String? = null, var type : Int? = 0, var cameras : Map<String,Camera>? = null,var token : String? = null)
