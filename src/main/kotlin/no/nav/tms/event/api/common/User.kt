package no.nav.tms.event.api.common

data class User(val fodselsnummer: String) {

    override fun toString(): String {
        return "User(fodselsnummer='***')"
    }
}
