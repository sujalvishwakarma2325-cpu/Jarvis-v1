package com.jarvis.v1.memory

class ConversationMemory(
    private val maxMessages: Int = 20
) {

    private val messages = mutableListOf<String>()

    fun addUserMessage(message: String) {
        messages.add("User: $message")
        trimMemory()
    }

    fun addAssistantMessage(message: String) {
        messages.add("JARVIS: $message")
        trimMemory()
    }

    fun getHistory(): String {
        return messages.joinToString("\n")
    }

    fun clear() {
        messages.clear()
    }

    fun isEmpty(): Boolean {
        return messages.isEmpty()
    }

    private fun trimMemory() {
        while (messages.size > maxMessages) {
            messages.removeAt(0)
        }
    }
}
