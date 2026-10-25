package com.example.typingtime.data

data class SamplePassage(
    val title: String,
    val category: String,
    val text: String
)

object SamplePassages {

    val SAMPLES = listOf(
        SamplePassage(
            title = "Article Passage",
            category = "General",
            text = """The rapid evolution of modern technology has revolutionized the way human beings communicate, create, and collaborate across borders. Decades ago, sending written correspondence required handwritten manuscripts, postal logistics, and considerable patience. Today, ideas travel around the world in fractions of a second through fiber optic networks, mobile data, and satellite constellations.

Despite these technological leaps, the keyboard remains one of our primary gateways to digital creativity. Whether writing essays, composing professional correspondence, drafting software code, or taking lecture notes, typing fluency fundamentally shapes our daily productivity. Knowing how much time an assignment or report will require allows individuals to manage schedules with accuracy and peace of mind.

Estimated typing speed varies widely among individuals based on keyboard layout, posture, ergonomics, and text complexity. By measuring word cadence and text density systematically, writers can set realistic expectations and optimize their personal focus."""
        ),
        SamplePassage(
            title = "Quick Message",
            category = "Casual",
            text = "Good morning! Just confirming that our project sync is scheduled for 10:30 AM tomorrow. Please review the updated design document at https://example.com/brief and email any feedback to team@example.com."
        ),
        SamplePassage(
            title = "Assignment Essay",
            category = "Academic",
            text = """The preservation of biodiversity is vital for maintaining ecological equilibrium and ensuring sustainable development. Ecosystems provide essential provisioning services, including clean water, soil renewal, pollination of agricultural crops, and carbon sequestration. Human activities, particularly deforestation and industrial pollution, continue to place acute stress on vulnerable habitats.

Interdisciplinary conservation efforts combine field ecology, geographic information systems, and community-led reforestation initiatives. When researchers analyze environmental indicators, timely documentation and clear scientific writing are indispensable for influencing public policy and environmental legislation."""
        ),
        SamplePassage(
            title = "Software Code Snippet",
            category = "Technical",
            text = """fun calculateTypingTime(wordCount: Int, wpm: Int): Long {
    require(wpm > 0) { "WPM must be positive" }
    val minutes = wordCount.toDouble() / wpm.toDouble()
    return kotlin.math.round(minutes * 60.0).toLong()
}

val passage = "Hello world! Test version 2.5.1"
val count = countWords(passage)
println("Estimated seconds: " + calculateTypingTime(count, 45))"""
        ),
        SamplePassage(
            title = "Calibration Test (50 words)",
            category = "Calibration",
            text = "Clear and disciplined writing reflects sharp thinking. When you sit down to type, maintain a steady rhythm and focus on accuracy before speed. Smooth keystrokes create consistent velocity, turning complex paragraphs into effortless expression. Practice regularly to improve your natural dexterity and confidence behind the keyboard."
        ),
        SamplePassage(
            title = "Calibration Test (100 words)",
            category = "Calibration",
            text = "Every great journey begins with a deliberate first step, and mastery of typing is no exception. In the modern knowledge economy, fingers on keys translate thoughts into tangible reality. When writers develop muscle memory, their cognitive load shifts away from mechanics toward higher-order creativity, structural clarity, and rhetorical elegance. Speed is not merely about rapid finger movement; it is about cognitive flow, rhythm, and minimizing corrections. By pacing yourself and maintaining steady composure, you will find that typing transforms from a chore into a seamless extension of your consciousness and intellect."
        )
    )
}
