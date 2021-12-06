package com.example.marcadechoferes.network.signinResponse



data class SigninResponse(
    val colors: Colors,
    val images: Images,
    val lastVar: LastVar?,
    val msg: String?,
    val profile: Profile?,
    val states: List<State>?,
    val vehicles: List<Vehicle>?,
    val work: Work?
)