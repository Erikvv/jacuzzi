package nl.evanv.app

import kotlin.test.Test

class HeaterTogglerTest {
    @Test
    fun test() {
        val toggler = HeaterSwitch.create(MqttConfig())
//        toggler.switchOff()
    }
}
