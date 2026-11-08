package app.teloleo.listener

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class SpeakerCommandsTest {
    @After
    fun resumeAfterEach() {
        SpeakerCommands.resume()
    }

    @Test
    fun requestSilenceSetsTheDeadlineFromNow() {
        SpeakerCommands.requestSilence(120_000L, now = 1_000L)
        assertEquals(121_000L, SpeakerCommands.silencedUntil.value)

        SpeakerCommands.requestSilence(-50L, now = 4_000L)
        assertEquals(4_000L, SpeakerCommands.silencedUntil.value)
    }

    @Test
    fun resumeClearsTheDeadline() {
        SpeakerCommands.requestSilence(SpeakerCommands.SHAKE_SILENCE_MILLIS, now = 5_000L)
        assertEquals(125_000L, SpeakerCommands.silencedUntil.value)

        SpeakerCommands.resume()
        assertEquals(0L, SpeakerCommands.silencedUntil.value)
    }
}
